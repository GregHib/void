package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import java.awt.GraphicsEnvironment
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.DigestInputStream
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat
import java.util.Properties
import java.util.zip.ZipInputStream
import javax.swing.JOptionPane
import kotlin.io.path.exists
import kotlin.io.path.name

/**
 * Downloads and installs the game cache files at startup when they are missing or out of date.
 *
 * Expects `<baseUrl>/version.json` containing `{"file": "cache.zip", "sha256": "<hex>"}` and the zip at `<baseUrl>/<file>`.
 * The zip is verified against the sha256 before any file is touched, then `main_file_cache.*` entries (at any depth) are
 * staged as `.part` files and atomically moved into place. The installed sha is recorded in [MARKER_FILE] so the on-disk
 * cache files themselves are never hashed (they are modified by tooling after install).
 */
class CacheDownloader(
    baseUrl: String,
    private val cachePath: Path,
    private val fetcher: Fetcher = HttpFetcher(),
    private val confirm: (String) -> Boolean = ::prompt,
) {

    private val baseUrl = baseUrl.trimEnd('/')

    fun interface Fetcher {
        @Throws(IOException::class)
        fun open(uri: URI): Download
    }

    class Download(val length: Long, val stream: InputStream) : Closeable {
        override fun close() = stream.close()
    }

    data class Version(val file: String, val sha256: String)

    /**
     * @return false if the server should not start; the cache is missing and couldn't be installed
     */
    fun ensure(): Boolean {
        val present = filesPresent()
        val version = fetchVersion()
        if (version == null) {
            if (present) {
                logger.warn { "Could not check for cache updates from '$baseUrl'; continuing with existing cache." }
                return true
            }
            logger.error { "No cache files found in '$cachePath' and cannot reach '$baseUrl' to download them." }
            return false
        }
        if (!present) {
            if (!confirm("No game cache files were found in '$cachePath'.\nDo you want to download and install the cache from $baseUrl?")) {
                logger.info { "Cache download declined." }
                return false
            }
            return install(version)
        }
        val installed = installedSha()
        if (installed == null) {
            writeMarker(version.sha256)
            logger.info { "Recorded installed cache version ${version.sha256}." }
            return true
        }
        if (installed == version.sha256) {
            logger.debug { "Cache is up to date." }
            return true
        }
        if (!confirm("A new game cache version is available.\nDo you want to download it and overwrite the files in '$cachePath'?")) {
            logger.warn { "Cache update declined; continuing with existing cache. You will be asked again on next start." }
            return true
        }
        return install(version)
    }

    private fun install(version: Version): Boolean {
        val zip = try {
            download(version)
        } catch (e: Exception) {
            logger.error(e) { "Failed to download cache '${version.file}' from '$baseUrl'." }
            return false
        }
        try {
            extract(zip)
        } catch (e: Exception) {
            logger.error(e) { "Failed to extract cache '${zip.name}'." }
            return false
        } finally {
            Files.deleteIfExists(zip)
        }
        writeMarker(version.sha256)
        logger.info { "Cache ${version.sha256} installed to '$cachePath'." }
        return true
    }

    internal fun filesPresent(): Boolean = REQUIRED_FILES.all { cachePath.resolve(it).exists() }

    internal fun fetchVersion(): Version? {
        val json = try {
            fetcher.open(URI.create("$baseUrl/$VERSION_FILE")).use { it.stream.readAllBytes().decodeToString() }
        } catch (e: Exception) {
            logger.debug(e) { "Unable to fetch '$baseUrl/$VERSION_FILE'." }
            return null
        }
        val version = parseVersion(json)
        if (version == null) {
            logger.warn { "Malformed '$baseUrl/$VERSION_FILE': expected {\"file\": \"...\", \"sha256\": \"...\"}." }
        }
        return version
    }

    /**
     * Downloads [Version.file] to [TEMP_FILE], verifying the sha256 while streaming.
     * @throws IOException if the download fails or the checksum doesn't match
     */
    internal fun download(version: Version): Path {
        require(FILE_NAME_PATTERN.matches(version.file)) { "Invalid cache file name '${version.file}'." }
        Files.createDirectories(cachePath)
        cleanStaged()
        val temp = cachePath.resolve(TEMP_FILE)
        val digest = MessageDigest.getInstance("SHA-256")
        val uri = URI.create("$baseUrl/${version.file}")
        logger.info { "Downloading cache from '$uri'." }
        try {
            fetcher.open(uri).use { download ->
                DigestInputStream(download.stream, digest).use { input ->
                    Files.newOutputStream(temp).use { output ->
                        copy(input, output, download.length)
                    }
                }
            }
            val actual = HexFormat.of().formatHex(digest.digest())
            if (actual != version.sha256) {
                throw IOException("Checksum mismatch for '${version.file}': expected ${version.sha256} but was $actual.")
            }
        } catch (e: Exception) {
            Files.deleteIfExists(temp)
            throw e
        }
        return temp
    }

    private fun copy(input: InputStream, output: java.io.OutputStream, length: Long) {
        val buffer = ByteArray(BUFFER_SIZE)
        var total = 0L
        var next = if (length > 0) length / 10 else UNKNOWN_LENGTH_STEP
        while (true) {
            val read = input.read(buffer)
            if (read == -1) {
                break
            }
            output.write(buffer, 0, read)
            total += read
            if (total >= next) {
                if (length > 0) {
                    logger.info { "Downloading cache: ${total * 100 / length}% (${total.megabytes}/${length.megabytes} MB)" }
                    next += length / 10
                } else {
                    logger.info { "Downloading cache: ${total.megabytes} MB" }
                    next += UNKNOWN_LENGTH_STEP
                }
            }
        }
        if (length > 0 && total != length) {
            throw IOException("Incomplete download: expected $length bytes but received $total.")
        }
    }

    /**
     * Extracts `main_file_cache.*` entries from [zip] into [cachePath], ignoring any directory prefix.
     * Files are staged as `.part` and only moved into place once all required files are present.
     */
    internal fun extract(zip: Path) {
        val staged = mutableListOf<Path>()
        try {
            ZipInputStream(Files.newInputStream(zip).buffered()).use { input ->
                var entry = input.nextEntry
                while (entry != null) {
                    val name = entry.name.substringAfterLast('/').substringAfterLast('\\')
                    if (!entry.isDirectory && name.startsWith(CACHE_FILE_PREFIX)) {
                        val target = cachePath.resolve("$name$STAGED_SUFFIX")
                        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
                        staged.add(target)
                    }
                    input.closeEntry()
                    entry = input.nextEntry
                }
            }
            val names = staged.map { it.name.removeSuffix(STAGED_SUFFIX) }
            val missing = REQUIRED_FILES.filterNot { it in names }
            if (missing.isNotEmpty()) {
                throw IOException("Cache archive is missing required files: $missing.")
            }
            for (path in staged) {
                Files.move(path, cachePath.resolve(path.name.removeSuffix(STAGED_SUFFIX)), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            }
        } catch (e: Exception) {
            for (path in staged) {
                Files.deleteIfExists(path)
            }
            throw e
        }
    }

    internal fun installedSha(): String? {
        val marker = cachePath.resolve(MARKER_FILE)
        if (!marker.exists()) {
            return null
        }
        val sha = Files.readString(marker).trim().lowercase()
        return if (SHA_PATTERN.matches(sha)) sha else null
    }

    internal fun writeMarker(sha256: String) {
        Files.createDirectories(cachePath)
        Files.writeString(cachePath.resolve(MARKER_FILE), "$sha256\n")
    }

    private fun cleanStaged() {
        if (!cachePath.exists()) {
            return
        }
        Files.newDirectoryStream(cachePath) { it.name.endsWith(STAGED_SUFFIX) }.use { stream ->
            for (path in stream) {
                Files.deleteIfExists(path)
            }
        }
    }

    private val Long.megabytes: Long
        get() = this / (1024 * 1024)

    private class HttpFetcher : Fetcher {
        private val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build()

        override fun open(uri: URI): Download {
            val request = HttpRequest.newBuilder(uri).GET().timeout(Duration.ofSeconds(30)).build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
            if (response.statusCode() != 200) {
                response.body().close()
                throw IOException("HTTP ${response.statusCode()} for '$uri'.")
            }
            val length = response.headers().firstValueAsLong("Content-Length").orElse(-1L)
            return Download(length, response.body())
        }
    }

    companion object {
        private val logger = InlineLogger()
        const val MARKER_FILE = "cache.sha256"
        const val TEMP_FILE = "cache.zip.part"
        const val VERSION_FILE = "version.json"
        private const val STAGED_SUFFIX = ".part"
        private const val CACHE_FILE_PREFIX = "${FileCache.CACHE_FILE_NAME}."
        private val REQUIRED_FILES = listOf("${FileCache.CACHE_FILE_NAME}.dat2", "${FileCache.CACHE_FILE_NAME}.idx255")
        private const val BUFFER_SIZE = 64 * 1024
        private const val UNKNOWN_LENGTH_STEP = 25L * 1024 * 1024
        private val FILE_NAME_PATTERN = Regex("[A-Za-z0-9_-][A-Za-z0-9._-]*")
        private val SHA_PATTERN = Regex("[0-9a-f]{64}")
        private val FILE_KEY = Regex("\"file\"\\s*:\\s*\"([^\"]+)\"")
        private val SHA_KEY = Regex("\"sha256\"\\s*:\\s*\"([0-9a-fA-F]{64})\"")

        /**
         * Reads `storage.cache.download.url` and `storage.cache.path` from [properties].
         * @return false if the server should not start
         */
        fun ensure(properties: Properties): Boolean {
            val url = properties.getProperty("storage.cache.download.url", "").trim()
            if (url.isBlank()) {
                return true
            }
            val path = Path.of(properties.getProperty("storage.cache.path", "./data/cache/"))
            return CacheDownloader(url, path).ensure()
        }

        internal fun parseVersion(json: String): Version? {
            val file = FILE_KEY.find(json)?.groupValues?.get(1) ?: return null
            val sha = SHA_KEY.find(json)?.groupValues?.get(1) ?: return null
            return Version(file, sha.lowercase())
        }

        internal fun prompt(message: String): Boolean {
            if (GraphicsEnvironment.isHeadless()) {
                logger.info { "No desktop environment detected; downloading cache automatically." }
                return true
            }
            val options = arrayOf("Yes", "No")
            val response = JOptionPane.showOptionDialog(null, message, "Cache Download", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0])
            return response == JOptionPane.YES_OPTION
        }
    }
}

package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import java.awt.GraphicsEnvironment
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.Properties
import java.util.zip.ZipInputStream
import javax.swing.JOptionPane
import kotlin.io.path.exists
import kotlin.io.path.name

/**
 * Downloads and installs the game cache files at startup when they are missing.
 *
 * No network requests are made when the cache files already exist.
 * `main_file_cache.*` entries (at any depth) in the zip at `url` are staged as `.part` files
 * and only moved into place once all required files have been extracted.
 */
class CacheDownloader(
    private val url: String,
    private val cachePath: Path,
    private val fetcher: Fetcher = HttpFetcher(),
    private val confirm: (String) -> Boolean = ::prompt,
) {

    fun interface Fetcher {
        @Throws(IOException::class)
        fun open(uri: URI): Download
    }

    class Download(val length: Long, val stream: InputStream) : Closeable {
        override fun close() = stream.close()
    }

    /**
     * @return false if the server should not start; the cache is missing and couldn't be installed
     */
    fun ensure(): Boolean {
        if (filesPresent(cachePath)) {
            return true
        }
        if (!confirm("No game cache files were found in '$cachePath'.\nDo you want to download and install the cache from $url?")) {
            logger.info { "Cache download declined." }
            return false
        }
        val zip = try {
            download()
        } catch (e: Exception) {
            logger.error(e) { "Failed to download cache from '$url'." }
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
        logger.info { "Cache installed to '$cachePath'." }
        return true
    }

    /**
     * Downloads the cache zip to [TEMP_FILE].
     * @throws IOException if the download fails or is incomplete
     */
    internal fun download(): Path {
        Files.createDirectories(cachePath)
        cleanStaged()
        val temp = cachePath.resolve(TEMP_FILE)
        logger.info { "Downloading cache from '$url'." }
        try {
            fetcher.open(URI.create(url)).use { download ->
                Files.newOutputStream(temp).use { output ->
                    copy(download.stream, output, download.length)
                }
            }
        } catch (e: Exception) {
            Files.deleteIfExists(temp)
            throw e
        }
        return temp
    }

    private fun copy(input: InputStream, output: OutputStream, length: Long) {
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

    private fun cleanStaged() {
        Files.newDirectoryStream(cachePath) { it.name.endsWith(STAGED_SUFFIX) }.use { stream ->
            for (path in stream) {
                Files.deleteIfExists(path)
            }
        }
    }

    private val Long.megabytes: Long
        get() = this / (1024 * 1024)

    private class HttpFetcher : Fetcher {
        override fun open(uri: URI): Download {
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build()
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
        const val TEMP_FILE = "cache.zip.part"
        private const val STAGED_SUFFIX = ".part"
        private const val CACHE_FILE_PREFIX = "${FileCache.CACHE_FILE_NAME}."
        private val REQUIRED_FILES = listOf("${FileCache.CACHE_FILE_NAME}.dat2", "${FileCache.CACHE_FILE_NAME}.idx255")
        private const val BUFFER_SIZE = 64 * 1024
        private const val UNKNOWN_LENGTH_STEP = 25L * 1024 * 1024

        /**
         * Reads `storage.cache.download.url` and `storage.cache.path` from [properties].
         * @return false if the server should not start
         */
        fun ensure(properties: Properties): Boolean {
            val path = Path.of(properties.getProperty("storage.cache.path", "./data/cache/"))
            if (filesPresent(path)) {
                return true
            }
            val url = properties.getProperty("storage.cache.download.url", "").trim()
            if (url.isBlank()) {
                return true
            }
            return CacheDownloader(url, path).ensure()
        }

        internal fun filesPresent(path: Path): Boolean = REQUIRED_FILES.all { path.resolve(it).exists() }

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

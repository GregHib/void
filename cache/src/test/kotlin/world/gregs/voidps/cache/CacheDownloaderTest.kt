package world.gregs.voidps.cache

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.HexFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

internal class CacheDownloaderTest {

    @TempDir
    lateinit var dir: Path

    private lateinit var cachePath: Path
    private lateinit var fetcher: FakeFetcher
    private val prompts = mutableListOf<String>()
    private var answer = true

    private class FakeFetcher : CacheDownloader.Fetcher {
        val responses = mutableMapOf<String, ByteArray>()
        val requested = mutableListOf<String>()
        var truncate = false

        override fun open(uri: URI): CacheDownloader.Download {
            requested.add(uri.path)
            val bytes = responses[uri.path] ?: throw IOException("HTTP 404 for '$uri'.")
            val stream: InputStream = if (truncate && uri.path != "/version.json") ByteArrayInputStream(bytes, 0, bytes.size / 2) else ByteArrayInputStream(bytes)
            return CacheDownloader.Download(bytes.size.toLong(), stream)
        }
    }

    @BeforeEach
    fun setup() {
        cachePath = dir.resolve("cache")
        fetcher = FakeFetcher()
        prompts.clear()
        answer = true
    }

    private fun downloader(url: String = "https://example.test/") = CacheDownloader(url, cachePath, fetcher) {
        prompts.add(it)
        answer
    }

    private fun zip(vararg entries: Pair<String, ByteArray>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            for ((name, data) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(data)
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }

    private fun sha256(bytes: ByteArray) = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))

    private fun host(zip: ByteArray, sha: String = sha256(zip), file: String = "cache.zip"): String {
        fetcher.responses["/version.json"] = """{ "file": "$file", "sha256": "$sha" }""".toByteArray()
        fetcher.responses["/$file"] = zip
        return sha
    }

    private fun existingCache(sha: String? = null) {
        Files.createDirectories(cachePath)
        cachePath.resolve("main_file_cache.dat2").writeBytes(byteArrayOf(9, 9, 9))
        cachePath.resolve("main_file_cache.idx255").writeBytes(byteArrayOf(8, 8))
        if (sha != null) {
            cachePath.resolve(CacheDownloader.MARKER_FILE).writeText("$sha\n")
        }
    }

    private val validZip = zip(
        "main_file_cache.dat2" to byteArrayOf(1, 2, 3),
        "main_file_cache.idx0" to byteArrayOf(4),
        "main_file_cache.idx255" to byteArrayOf(5, 6),
    )

    @Test
    fun `Blank url skips download and returns true`() {
        val properties = java.util.Properties()
        properties.setProperty("storage.cache.download.url", " ")
        properties.setProperty("storage.cache.path", cachePath.toString())
        assertTrue(CacheDownloader.ensure(properties))
        assertFalse(cachePath.exists())
    }

    @Test
    fun `Missing cache files downloads extracts and writes marker`() {
        val sha = host(validZip)

        assertTrue(downloader().ensure())

        assertArrayEquals(byteArrayOf(1, 2, 3), cachePath.resolve("main_file_cache.dat2").readBytes())
        assertArrayEquals(byteArrayOf(4), cachePath.resolve("main_file_cache.idx0").readBytes())
        assertArrayEquals(byteArrayOf(5, 6), cachePath.resolve("main_file_cache.idx255").readBytes())
        assertEquals("$sha\n", cachePath.resolve(CacheDownloader.MARKER_FILE).readText())
        assertTrue(cachePath.listDirectoryEntries().none { it.name.endsWith(".part") })
        assertEquals(1, prompts.size)
        assertTrue(prompts[0].contains("install"))
        assertEquals(listOf("/version.json", "/cache.zip"), fetcher.requested)
    }

    @Test
    fun `Zip entries with folder prefix are flattened into cache path`() {
        host(
            zip(
                "cache/main_file_cache.dat2" to byteArrayOf(1),
                "cache/nested/main_file_cache.idx255" to byteArrayOf(2),
                "cache/readme.txt" to byteArrayOf(3),
                "../main_file_cache.idx1" to byteArrayOf(4),
                "cache/" to byteArrayOf(),
            ),
        )

        assertTrue(downloader().ensure())

        assertArrayEquals(byteArrayOf(1), cachePath.resolve("main_file_cache.dat2").readBytes())
        assertArrayEquals(byteArrayOf(2), cachePath.resolve("main_file_cache.idx255").readBytes())
        assertArrayEquals(byteArrayOf(4), cachePath.resolve("main_file_cache.idx1").readBytes())
        assertFalse(dir.resolve("main_file_cache.idx1").exists())
        assertFalse(cachePath.resolve("readme.txt").exists())
        assertFalse(cachePath.resolve("cache").exists())
    }

    @Test
    fun `Sha mismatch deletes temp file and returns false`() {
        host(validZip, sha = "0".repeat(64))

        assertFalse(downloader().ensure())

        assertFalse(cachePath.resolve("main_file_cache.dat2").exists())
        assertFalse(cachePath.resolve(CacheDownloader.MARKER_FILE).exists())
        assertFalse(cachePath.resolve(CacheDownloader.TEMP_FILE).exists())
    }

    @Test
    fun `Archive missing required files is rejected`() {
        host(zip("main_file_cache.idx0" to byteArrayOf(1)))

        assertFalse(downloader().ensure())

        assertTrue(cachePath.listDirectoryEntries().none { it.name.startsWith("main_file_cache") })
        assertFalse(cachePath.resolve(CacheDownloader.MARKER_FILE).exists())
    }

    @Test
    fun `Existing files without marker adopt remote sha without downloading`() {
        existingCache()
        val sha = host(validZip)

        assertTrue(downloader().ensure())

        assertEquals(listOf("/version.json"), fetcher.requested)
        assertEquals("$sha\n", cachePath.resolve(CacheDownloader.MARKER_FILE).readText())
        assertArrayEquals(byteArrayOf(9, 9, 9), cachePath.resolve("main_file_cache.dat2").readBytes())
        assertTrue(prompts.isEmpty())
    }

    @Test
    fun `Matching marker skips download`() {
        val sha = host(validZip)
        existingCache(sha)

        assertTrue(downloader().ensure())

        assertEquals(listOf("/version.json"), fetcher.requested)
        assertArrayEquals(byteArrayOf(9, 9, 9), cachePath.resolve("main_file_cache.dat2").readBytes())
        assertTrue(prompts.isEmpty())
    }

    @Test
    fun `Different marker downloads update and overwrites files`() {
        val sha = host(validZip)
        existingCache("f".repeat(64))

        assertTrue(downloader().ensure())

        assertEquals(1, prompts.size)
        assertTrue(prompts[0].contains("overwrite"))
        assertArrayEquals(byteArrayOf(1, 2, 3), cachePath.resolve("main_file_cache.dat2").readBytes())
        assertEquals("$sha\n", cachePath.resolve(CacheDownloader.MARKER_FILE).readText())
    }

    @Test
    fun `Declined install returns false`() {
        host(validZip)
        answer = false

        assertFalse(downloader().ensure())

        assertEquals(listOf("/version.json"), fetcher.requested)
        assertFalse(cachePath.resolve("main_file_cache.dat2").exists())
    }

    @Test
    fun `Declined update keeps existing cache`() {
        host(validZip)
        existingCache("f".repeat(64))
        answer = false

        assertTrue(downloader().ensure())

        assertEquals(listOf("/version.json"), fetcher.requested)
        assertArrayEquals(byteArrayOf(9, 9, 9), cachePath.resolve("main_file_cache.dat2").readBytes())
        assertEquals("${"f".repeat(64)}\n", cachePath.resolve(CacheDownloader.MARKER_FILE).readText())
    }

    @Test
    fun `Unreachable remote with existing files returns true without marker`() {
        existingCache()

        assertTrue(downloader().ensure())

        assertFalse(cachePath.resolve(CacheDownloader.MARKER_FILE).exists())
        assertTrue(prompts.isEmpty())
    }

    @Test
    fun `Unreachable remote without files returns false`() {
        assertFalse(downloader().ensure())
        assertTrue(prompts.isEmpty())
    }

    @Test
    fun `Malformed version json is treated as unreachable`() {
        fetcher.responses["/version.json"] = """{ "file": "cache.zip" }""".toByteArray()

        assertFalse(downloader().ensure())

        existingCache()
        assertTrue(downloader().ensure())
        assertTrue(prompts.isEmpty())
    }

    @Test
    fun `Stale part files from previous run are removed`() {
        host(validZip)
        Files.createDirectories(cachePath)
        cachePath.resolve(CacheDownloader.TEMP_FILE).writeBytes(byteArrayOf(1))
        cachePath.resolve("main_file_cache.idx7.part").writeBytes(byteArrayOf(1))

        assertTrue(downloader().ensure())

        assertTrue(cachePath.listDirectoryEntries().none { it.name.endsWith(".part") })
        assertFalse(cachePath.resolve("main_file_cache.idx7").exists())
    }

    @Test
    fun `Failed download leaves existing files and marker untouched`() {
        host(validZip)
        existingCache("f".repeat(64))
        fetcher.truncate = true

        assertFalse(downloader().ensure())

        assertArrayEquals(byteArrayOf(9, 9, 9), cachePath.resolve("main_file_cache.dat2").readBytes())
        assertEquals("${"f".repeat(64)}\n", cachePath.resolve(CacheDownloader.MARKER_FILE).readText())
        assertTrue(cachePath.listDirectoryEntries().none { it.name.endsWith(".part") })
    }

    @Test
    fun `Invalid file name in version json is rejected`() {
        host(validZip, file = "../cache.zip")

        assertFalse(downloader().ensure())

        assertEquals(listOf("/version.json"), fetcher.requested)
    }

    @Test
    fun `Trailing slash in url is trimmed`() {
        host(validZip)
        assertTrue(downloader("https://example.test///").ensure())
        assertEquals(listOf("/version.json", "/cache.zip"), fetcher.requested)
    }

    @Test
    fun `Parse version handles whitespace and uppercase`() {
        val version = CacheDownloader.parseVersion(
            """
            {
                "file" :   "cache.zip",
                "sha256":"${"A".repeat(64)}"
            }
            """.trimIndent(),
        )
        assertEquals(CacheDownloader.Version("cache.zip", "a".repeat(64)), version)
    }

    @Test
    fun `Parse version rejects missing or invalid keys`() {
        assertNull(CacheDownloader.parseVersion("""{ "sha256": "${"a".repeat(64)}" }"""))
        assertNull(CacheDownloader.parseVersion("""{ "file": "cache.zip" }"""))
        assertNull(CacheDownloader.parseVersion("""{ "file": "cache.zip", "sha256": "${"a".repeat(63)}" }"""))
        assertNull(CacheDownloader.parseVersion("not json"))
    }
}

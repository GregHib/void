package world.gregs.voidps.cache

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

internal class CacheDownloaderTest {

    @TempDir
    lateinit var dir: Path

    private lateinit var cachePath: Path
    private lateinit var fetcher: FakeFetcher
    private val prompts = mutableListOf<String>()
    private var answer = true

    private class FakeFetcher : CacheDownloader.Fetcher {
        var response: ByteArray? = null
        val requested = mutableListOf<String>()
        var truncate = false

        override fun open(uri: URI): CacheDownloader.Download {
            requested.add(uri.toString())
            val bytes = response ?: throw IOException("HTTP 404 for '$uri'.")
            val stream: InputStream = if (truncate) ByteArrayInputStream(bytes, 0, bytes.size / 2) else ByteArrayInputStream(bytes)
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

    private fun downloader() = CacheDownloader(URL, cachePath, fetcher) {
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

    private fun existingCache() {
        Files.createDirectories(cachePath)
        cachePath.resolve("main_file_cache.dat2").writeBytes(byteArrayOf(9, 9, 9))
        cachePath.resolve("main_file_cache.idx255").writeBytes(byteArrayOf(8, 8))
    }

    private fun properties(url: String) = Properties().apply {
        setProperty("storage.cache.download.url", url)
        setProperty("storage.cache.path", cachePath.toString())
    }

    private val validZip = zip(
        "main_file_cache.dat2" to byteArrayOf(1, 2, 3),
        "main_file_cache.idx0" to byteArrayOf(4),
        "main_file_cache.idx255" to byteArrayOf(5, 6),
    )

    @Test
    fun `Blank url skips download and returns true`() {
        assertTrue(CacheDownloader.ensure(properties(" ")))
        assertFalse(cachePath.exists())
    }

    @Test
    fun `Existing cache files skip download without any requests`() {
        existingCache()

        assertTrue(CacheDownloader.ensure(properties("http://127.0.0.1:1/cache.zip")))
        assertTrue(downloader().ensure())

        assertTrue(fetcher.requested.isEmpty())
        assertTrue(prompts.isEmpty())
        assertArrayEquals(byteArrayOf(9, 9, 9), cachePath.resolve("main_file_cache.dat2").readBytes())
    }

    @Test
    fun `Missing cache files downloads and extracts`() {
        fetcher.response = validZip

        assertTrue(downloader().ensure())

        assertArrayEquals(byteArrayOf(1, 2, 3), cachePath.resolve("main_file_cache.dat2").readBytes())
        assertArrayEquals(byteArrayOf(4), cachePath.resolve("main_file_cache.idx0").readBytes())
        assertArrayEquals(byteArrayOf(5, 6), cachePath.resolve("main_file_cache.idx255").readBytes())
        assertTrue(cachePath.listDirectoryEntries().none { it.name.endsWith(".part") })
        assertEquals(1, prompts.size)
        assertEquals(listOf(URL), fetcher.requested)
    }

    @Test
    fun `Zip entries with folder prefix are flattened into cache path`() {
        fetcher.response = zip(
            "cache/main_file_cache.dat2" to byteArrayOf(1),
            "cache/nested/main_file_cache.idx255" to byteArrayOf(2),
            "cache/readme.txt" to byteArrayOf(3),
            "../main_file_cache.idx1" to byteArrayOf(4),
            "cache/" to byteArrayOf(),
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
    fun `Archive missing required files is rejected`() {
        fetcher.response = zip("main_file_cache.idx0" to byteArrayOf(1))

        assertFalse(downloader().ensure())

        assertTrue(cachePath.listDirectoryEntries().none { it.name.startsWith("main_file_cache") })
    }

    @Test
    fun `Declined install returns false`() {
        fetcher.response = validZip
        answer = false

        assertFalse(downloader().ensure())

        assertTrue(fetcher.requested.isEmpty())
        assertFalse(cachePath.resolve("main_file_cache.dat2").exists())
    }

    @Test
    fun `Unreachable remote returns false`() {
        assertFalse(downloader().ensure())
        assertFalse(cachePath.resolve(CacheDownloader.TEMP_FILE).exists())
    }

    @Test
    fun `Incomplete download deletes temp file and returns false`() {
        fetcher.response = validZip
        fetcher.truncate = true

        assertFalse(downloader().ensure())

        assertFalse(cachePath.resolve("main_file_cache.dat2").exists())
        assertTrue(cachePath.listDirectoryEntries().none { it.name.endsWith(".part") })
    }

    @Test
    fun `Stale part files from previous run are removed`() {
        fetcher.response = validZip
        Files.createDirectories(cachePath)
        cachePath.resolve(CacheDownloader.TEMP_FILE).writeBytes(byteArrayOf(1))
        cachePath.resolve("main_file_cache.idx7.part").writeBytes(byteArrayOf(1))

        assertTrue(downloader().ensure())

        assertTrue(cachePath.listDirectoryEntries().none { it.name.endsWith(".part") })
        assertFalse(cachePath.resolve("main_file_cache.idx7").exists())
    }

    companion object {
        private const val URL = "https://example.test/cache.zip"
    }
}

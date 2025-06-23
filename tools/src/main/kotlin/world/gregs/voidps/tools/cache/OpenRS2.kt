package world.gregs.voidps.tools.cache

import org.jsoup.Jsoup
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

object OpenRS2 {

    fun downloadKeys(directory: File, target: Int): File {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = directory.resolve("$target-keys.json")
        if (!file.exists()) {
            val text = Jsoup.connect("https://archive.openrs2.org/caches/runescape/$target/keys.json")
                .ignoreContentType(true)
                .get()
                .body()
                .ownText()
            file.writeText(text)
        }
        return file
    }

    fun getKeys(target: Int, directory: File = File("./temp/cache/xteas/")): Xteas {
        val file = downloadKeys(directory, target)
        return Xteas(Xteas.loadJson(file.readText(), value = "key").toMutableMap())
    }

    fun downloadCache(directory: File, number: Int): File {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = directory.resolve("main_file_cache.dat2")
        if (!file.exists()) {
            downloadZip("https://archive.openrs2.org/caches/runescape/$number/disk.zip", directory.path)
        }
        return directory
    }

    private fun downloadZip(url: String, destinationDirectory: String) {
        val zipInputStream = ZipInputStream(BufferedInputStream(URL(url).openStream()))
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            if (!entry.isDirectory && entry.name.startsWith(ZIP_DIRECTORY)) {
                val relativePath = entry.name.substring(ZIP_DIRECTORY.length)
                val filePath = File(destinationDirectory, relativePath)
                filePath.parentFile.mkdirs()
                FileOutputStream(filePath).use { output ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (true) {
                        bytesRead = zipInputStream.read(buffer)
                        if (bytesRead == -1) {
                            break
                        }

                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        printProgress(totalBytesRead)
                    }
                }
            }

            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
    }

    private fun printProgress(totalBytesRead: Long) {
        val totalMegabytes = totalBytesRead.toDouble() / (1024 * 1024)
        print("\rTotal Downloaded: %.2f MB".format(totalMegabytes))
    }

    private const val ZIP_DIRECTORY = "cache"
}

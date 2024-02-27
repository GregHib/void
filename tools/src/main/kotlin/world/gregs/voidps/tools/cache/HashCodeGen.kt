package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.CacheDelegate
import java.io.File

object HashCodeGen {

    @JvmStatic
    fun main(args: Array<String>) {
        val output = File("./temp/hashes/hashes.tsv")
        val library = CacheLibrary("${System.getProperty("user.home")}\\Downloads\\osrs-215-cache\\")
        val cache = CacheDelegate(library)
        val list = mutableListOf<Array<String>>()
        for (index in cache.indices()) {
            for (archive in cache.archives(index)) {
                val archiveHash = library.index(index).archive(archive)!!.hashName
                if (archiveHash != 0) {
                    list.add(arrayOf(index.toString(), archive.toString(), "0", archiveHash.toString(), ""))
                }
                for (file in cache.files(index, archive)) {
                    val fileHash = library.index(index).archive(archive)!!.file(file)!!.hashName
                    if (fileHash != 0) {
                        list.add(arrayOf(index.toString(), archive.toString(), file.toString(), fileHash.toString(), ""))
                    }
                }
            }
        }
        for (array in list) {
            output.appendText(array.joinToString("\t", postfix = "\n"))
        }
    }
}
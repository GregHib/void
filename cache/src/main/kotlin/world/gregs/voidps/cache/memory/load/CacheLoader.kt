package world.gregs.voidps.cache.memory.load

import world.gregs.voidps.cache.Cache
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile

interface CacheLoader {

    fun load(path: String, xteas: Map<Int, IntArray>? = null, threadUsage: Double = 1.0): Cache {
        val mainFile = File(path, "${Archive.CACHE_FILE_NAME}.dat2")
        if (!mainFile.exists()) {
            throw FileNotFoundException("Main file not found at '${mainFile.absolutePath}'.")
        }
        val main = RandomAccessFile(mainFile, "r")
        val index255File = File(path, "${Archive.CACHE_FILE_NAME}.idx255")
        if (!index255File.exists()) {
            throw FileNotFoundException("Checksum file not found at '${index255File.absolutePath}'.")
        }
        val index255 = RandomAccessFile(index255File, "r")
        val indexCount = index255.length().toInt() / Archive.INDEX_SIZE
        return load(path, mainFile, main, index255File, index255, indexCount, xteas, threadUsage)
    }

    fun load(path: String, mainFile: File, main: RandomAccessFile, index255File: File, index255: RandomAccessFile, indexCount: Int, xteas: Map<Int, IntArray>? = null, threadUsage: Double = 1.0): Cache
}
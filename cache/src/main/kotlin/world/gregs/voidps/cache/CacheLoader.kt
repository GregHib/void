package world.gregs.voidps.cache

import world.gregs.voidps.cache.secure.VersionTableBuilder
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.math.BigInteger
import java.util.*

interface CacheLoader {

    fun load(properties: Properties, xteas: Map<Int, IntArray>? = null): Cache {
        val fileModulus = BigInteger(properties.getProperty("security.file.modulus"), 16)
        val filePrivate = BigInteger(properties.getProperty("security.file.private"), 16)
        val cachePath = properties.getProperty("storage.cache.path")
        val threadUsage = properties.getProperty("threadUsage", "1.0").toDouble()
        return load(cachePath, filePrivate, fileModulus, threadUsage = threadUsage)
    }

    fun load(path: String, exponent: BigInteger? = null, modulus: BigInteger? = null, xteas: Map<Int, IntArray>? = null, threadUsage: Double = 1.0): Cache {
        val mainFile = File(path, "${FileCache.CACHE_FILE_NAME}.dat2")
        if (!mainFile.exists()) {
            throw FileNotFoundException("Main file not found at '${mainFile.absolutePath}'.")
        }
        val main = RandomAccessFile(mainFile, "r")
        val index255File = File(path, "${FileCache.CACHE_FILE_NAME}.idx255")
        if (!index255File.exists()) {
            throw FileNotFoundException("Checksum file not found at '${index255File.absolutePath}'.")
        }
        val index255 = RandomAccessFile(index255File, "r")
        val indexCount = index255.length().toInt() / ReadOnlyCache.INDEX_SIZE
        val versionTable = if (exponent != null && modulus != null) VersionTableBuilder(exponent, modulus, indexCount) else null
        return load(path, mainFile, main, index255File, index255, indexCount, versionTable, xteas, threadUsage)
    }

    fun load(
        path: String,
        mainFile: File,
        main: RandomAccessFile,
        index255File: File,
        index255: RandomAccessFile,
        indexCount: Int,
        versionTable: VersionTableBuilder? = null,
        xteas: Map<Int, IntArray>? = null,
        threadUsage: Double = 1.0
    ): Cache
}
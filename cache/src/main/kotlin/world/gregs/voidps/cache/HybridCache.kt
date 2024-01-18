package world.gregs.voidps.cache

import world.gregs.voidps.cache.compress.DecompressionContext
import world.gregs.voidps.cache.secure.VersionTableBuilder
import java.io.File
import java.io.RandomAccessFile
import java.math.BigInteger

/**
 * [Cache] which reads data from file by sectors from memory
 * Average data read speeds, fast sector read speeds, average loading and memory usage.
 */
class HybridCache(
    main: RandomAccessFile,
    index255: RandomAccessFile,
    indexes: Array<RandomAccessFile?>,
    indexCount: Int,
    xteas: Map<Int, IntArray>?
) : FileCache(main, index255, indexes, indexCount, xteas) {

    val sectors: Array<Array<ByteArray?>?> = arrayOfNulls(indexCount)
    val index255: Array<ByteArray?> = arrayOfNulls(indexCount)

    override fun sector(index: Int, archive: Int): ByteArray? {
        if (index == 255) {
            return index255[archive]
        }
        return sectors.getOrNull(index)?.getOrNull(archive)
    }

    companion object : CacheLoader {

        operator fun invoke(path: String, exponent: BigInteger? = null, modulus: BigInteger? = null, xteas: Map<Int, IntArray>? = null): Cache {
            return load(path, exponent, modulus, xteas)
        }

        /**
         * Create [RandomAccessFile]'s for each index file, load only the archive data into memory
         */
        override fun load(
            path: String,
            mainFile: File,
            main: RandomAccessFile,
            index255File: File,
            index255: RandomAccessFile,
            indexCount: Int,
            versionTable: VersionTableBuilder?,
            xteas: Map<Int, IntArray>?,
            threadUsage: Double
        ): Cache {
            val length = mainFile.length()
            val context = DecompressionContext()
            val indices = Array(indexCount) { indexId ->
                val file = File(path, "${CACHE_FILE_NAME}.idx$indexId")
                if (file.exists()) RandomAccessFile(file, "r") else null
            }
            val cache = HybridCache(main, index255, indices, indexCount, xteas)
            for (indexId in 0 until indexCount) {
                cache.archiveData(context, main, length, index255, indexId, versionTable, cache.index255)
                cache.sectors[indexId] = arrayOfNulls(cache.lastArchiveId(indexId) + 1)
                for (archiveId in cache.archives(indexId)) {
                    val indexRaf = indices[indexId] ?: continue
                    val sectorData = readSector(main, length, indexRaf, indexId, archiveId) ?: continue
                    cache.sectors[indexId]!![archiveId] = sectorData
                }
            }
            cache.versionTable = versionTable?.build() ?: ByteArray(0)
            return cache
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val path = "./data/cache/"

            val start = System.currentTimeMillis()
            val cache = load(path)
            println("Loaded cache in ${System.currentTimeMillis() - start}ms")
        }
    }
}
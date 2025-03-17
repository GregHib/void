package world.gregs.voidps.cache

import world.gregs.voidps.cache.compress.DecompressionContext
import world.gregs.voidps.cache.secure.CRC
import world.gregs.voidps.cache.secure.VersionTableBuilder
import world.gregs.voidps.cache.secure.Whirlpool
import java.io.File
import java.io.RandomAccessFile
import java.math.BigInteger

/**
 * [Cache] which reads data directly from file
 * Average read speeds, fast loading and low but variable memory usage.
 */
class FileCache(
    private val main: RandomAccessFile,
    private val index255: RandomAccessFile,
    private val indexes: Array<RandomAccessFile?>,
    indexCount: Int,
    val xteas: Map<Int, IntArray>?
) : ReadOnlyCache(indexCount) {

    private val dataCache = object : LinkedHashMap<Int, Array<ByteArray?>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Array<ByteArray?>>): Boolean {
            return size > 12
        }
    }
    private val sectorCache = object : LinkedHashMap<Int, ByteArray?>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, ByteArray?>): Boolean {
            return size > 12
        }
    }
    private val length = main.length()
    private val context = DecompressionContext()
    private val indexCrcs by lazy {
        indices.map {
            val data = sector(255, it) ?: return@map 0
            CRC.calculate(data, 0, data.size)
        }.toIntArray()
    }

    override fun indexCrcs() = indexCrcs

    override fun sector(index: Int, archive: Int): ByteArray? {
        val indexRaf = if (index == 255) index255 else indexes[index] ?: return null
        return sectorCache.getOrPut(index + (archive shl 6)) {
            readSector(main, length, indexRaf, index, archive)
        }
    }

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        if (index >= files.size) {
            return null
        }
        val archives = files[index]
        if (archives == null || archive >= archives.size) {
            return null
        }
        val files = archives[archive] ?: return null
        val matchingIndex = files.indexOf(file)
        if (matchingIndex == -1) {
            return null
        }
        val hash = index + (archive shl 6)
        val data = dataCache.getOrPut(hash) {
            val indexRaf = indexes[index] ?: return null
            fileData(context, main, length, indexRaf, index, archive, xteas) ?: return null
        }
        return data[matchingIndex]
    }

    override fun close() {
        main.close()
        for (file in indexes) {
            file?.close()
        }
    }

    companion object : CacheLoader {
        const val CACHE_FILE_NAME = "main_file_cache"

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
            val whirlpool = Whirlpool()
            val cache = FileCache(main, index255, indices, indexCount, xteas)
            for (indexId in 0 until indexCount) {
                cache.archiveData(context, main, length, index255, indexId, versionTable, whirlpool)
            }
            cache.versionTable = versionTable?.build(whirlpool) ?: ByteArray(0)
            return cache
        }
    }
}
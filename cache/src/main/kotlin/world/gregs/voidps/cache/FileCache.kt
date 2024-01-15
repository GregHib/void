package world.gregs.voidps.cache

import world.gregs.voidps.cache.compress.DecompressionContext
import java.io.File
import java.io.RandomAccessFile

/**
 * [Cache] which reads data directly from file
 * Average read speeds, fast loading and low but variable memory usage.
 */
class FileCache(
    private val main: RandomAccessFile,
    private val indexes: Array<RandomAccessFile?>,
    indexCount: Int,
    val xteas: Map<Int, IntArray>?
) : ReadOnlyCache(indexCount) {

    private val dataCache = object : LinkedHashMap<Int, Array<ByteArray?>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Array<ByteArray?>>?): Boolean {
            return size > 12
        }
    }
    private val length = main.length()
    private val context = DecompressionContext()

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        val matchingIndex = files.getOrNull(index)?.getOrNull(archive)?.indexOf(file) ?: -1
        if (matchingIndex == -1) {
            return null
        }
        val hash = index + (archive shl 6)
        val files = dataCache.getOrPut(hash) {
            val indexRaf = indexes[index] ?: return null
            readFileData(context, main, length, indexRaf, index, archive, xteas) ?: return null
        }
        return files[matchingIndex]
    }

    override fun close() {
        main.close()
        for (file in indexes) {
            file?.close()
        }
    }

    companion object : CacheLoader {
        const val CACHE_FILE_NAME = "main_file_cache"

        operator fun invoke(path: String, xteas: Map<Int, IntArray>? = null): Cache {
            return load(path, xteas)
        }

        /**
         * Create [RandomAccessFile]'s for each index file, load only the archive data into memory
         */
        override fun load(path: String, mainFile: File, main: RandomAccessFile, index255File: File, index255: RandomAccessFile, indexCount: Int, xteas: Map<Int, IntArray>?, threadUsage: Double): Cache {
            val length = mainFile.length()
            val context = DecompressionContext()
            val indices = Array(indexCount) { indexId ->
                val file = File(path, "${CACHE_FILE_NAME}.idx$indexId")
                if (file.exists()) RandomAccessFile(file, "r") else null
            }
            val cache = FileCache(main, indices, indexCount, xteas)
            for (indexId in 0 until indexCount) {
                cache.readArchiveData(context, main, length, index255, indexId)
            }
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
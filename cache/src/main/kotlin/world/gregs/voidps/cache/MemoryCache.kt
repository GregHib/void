package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import world.gregs.voidps.cache.compress.DecompressionContext
import java.io.File
import java.io.RandomAccessFile

/**
 * [Cache] that holds all data in memory
 * Read speeds are as fast, loading is slow and memory usage is high but stable.
 * Loading is done in parallel as it is much slower to load than [FileCache]
 */
class MemoryCache(indexCount: Int) : ReadOnlyCache(indexCount) {

    val data: Array<Array<Array<ByteArray?>?>?> = arrayOfNulls(indexCount)

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        return data.getOrNull(index)?.getOrNull(archive)?.getOrNull(file)
    }

    companion object : CacheLoader {
        private val logger = InlineLogger()

        operator fun invoke(path: String, threadUsage: Double = 1.0, xteas: Map<Int, IntArray>? = null): Cache {
            return load(path, xteas, threadUsage)
        }

        /**
         * Load each index in parallel using a percentage of cpu cores
         */
        @OptIn(DelicateCoroutinesApi::class)
        override fun load(path: String, mainFile: File, main: RandomAccessFile, index255File: File, index255: RandomAccessFile, indexCount: Int, xteas: Map<Int, IntArray>?, threadUsage: Double): Cache {
            val cache = MemoryCache(indexCount)
            val processors = (Runtime.getRuntime().availableProcessors() * threadUsage).toInt().coerceAtLeast(1)
            newFixedThreadPoolContext(processors, "cache-loader").use { dispatcher ->
                runBlocking(dispatcher) {
                    supervisorScope {
                        val fileLength = mainFile.length()
                        for (indexId in 0 until indexCount) {
                            launch {
                                loadIndex(path, indexId, mainFile, fileLength, index255File, xteas, processors, cache)
                            }
                        }
                    }
                }
            }
            return cache
        }

        /**
         * Reads an indexes archive information before reading each archive in parallel
         */
        private suspend fun loadIndex(
            path: String,
            indexId: Int,
            mainFile: File,
            mainFileLength: Long,
            index255File: File,
            xteas: Map<Int, IntArray>?,
            processors: Int,
            cache: MemoryCache
        ) {
            val file = File(path, "${FileCache.CACHE_FILE_NAME}.idx$indexId")
            if (!file.exists()) {
                logger.trace { "No index $indexId file found." }
                return
            }
            try {
                val start = System.currentTimeMillis()
                val main = withContext(Dispatchers.IO) {
                    RandomAccessFile(mainFile, "r")
                }
                val index255 = withContext(Dispatchers.IO) {
                    RandomAccessFile(index255File, "r")
                }
                val context = DecompressionContext()
                val highest = cache.readArchiveData(context, main, mainFileLength, index255, indexId)
                if (highest == -1) {
                    return
                }
                cache.data[indexId] = arrayOfNulls<Array<ByteArray?>?>(highest + 1)
                coroutineScope {
                    if (processors in 2 until highest) {
                        for (list in (0..highest).chunked(highest / processors)) {
                            launch {
                                loadArchives(cache, list, file, mainFile, mainFileLength, indexId, xteas)
                            }
                        }
                    } else {
                        loadArchives(cache, (0..highest).toList(), file, mainFile, mainFileLength, indexId, xteas)
                    }
                }
                logger.trace { "Loaded ${cache.data[indexId]!!.size} index $indexId archives in ${System.currentTimeMillis() - start}ms." }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to load index $indexId." }
            }
            return
        }

        /**
         * Reads data for every file in each [archives] into the [cache]
         */
        private fun loadArchives(
            cache: MemoryCache,
            archives: List<Int>,
            file: File,
            mainFile: File,
            mainFileLength: Long,
            indexId: Int,
            xteas: Map<Int, IntArray>?
        ) {
            val context = DecompressionContext()
            val raf = RandomAccessFile(file, "r")
            val main = RandomAccessFile(mainFile, "r")
            for (archiveId in archives) {
                val archiveFiles = cache.readFileData(context, main, mainFileLength, raf, indexId, archiveId, xteas) ?: continue
                val archiveFileIds = cache.files[indexId]?.get(archiveId) ?: continue
                val fileId = archiveFileIds.last()
                val fileCount = cache.fileCounts[indexId]?.getOrNull(archiveId) ?: continue
                val archiveData: Array<ByteArray?> = arrayOfNulls(fileId + 1)
                for (fileIndex in 0 until fileCount) {
                    val data = archiveFiles[fileIndex]
                    archiveData[archiveFileIds[fileIndex]] = data
                }
                cache.data[indexId]!![archiveId] = archiveData
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val path = "./data/cache/"
            var start = System.currentTimeMillis()
            val cache = load(path, null)
            println("Loaded cache in ${System.currentTimeMillis() - start}ms")
        }
    }
}
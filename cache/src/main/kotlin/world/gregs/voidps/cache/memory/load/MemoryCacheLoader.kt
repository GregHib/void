package world.gregs.voidps.cache.memory.load

import com.displee.cache.CacheLibrary
import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.memory.cache.MemoryCache
import world.gregs.voidps.cache.memory.cache.ReadOnlyCache
import java.io.File
import java.io.RandomAccessFile

class MemoryCacheLoader : CacheLoader {

    override fun load(path: String, mainFile: File, main: RandomAccessFile, index255File: File, index255: RandomAccessFile, indexCount: Int, xteas: Map<Int, IntArray>?, threadUsage: Double): Cache {
        val length = mainFile.length()
        val cache = MemoryCache(indexCount)
        val processors = (Runtime.getRuntime().availableProcessors() * threadUsage).toInt().coerceAtLeast(1)
        val dispatcher = newFixedThreadPoolContext(processors, "cache-loader")
        cache.data = runBlocking(dispatcher) {
            val data = (0 until indexCount).map { indexId ->
                async {
                    load(path, indexId, mainFile, length, index255File, xteas, dispatcher, processors, cache)
                }
            }
            data.awaitAll().toTypedArray()
        }
        dispatcher.close()
        return cache
    }

    suspend fun load(
        path: String,
        indexId: Int,
        mainFile: File,
        mainFileLength: Long,
        index255File: File,
        xteas: Map<Int, IntArray>?,
        dispatcher: ExecutorCoroutineDispatcher,
        processors: Int,
        cache: ReadOnlyCache
    ): Array<Array<ByteArray?>?>? {
        val file = File(path, "${CacheLibrary.CACHE_FILE_NAME}.idx$indexId")
        if (!file.exists()) {
            logger.trace { "No index $indexId file found." }
            return null
        }
        try {
            val start = System.currentTimeMillis()
            val main = withContext(Dispatchers.IO) {
                RandomAccessFile(mainFile, "r")
            }
            val index255 = withContext(Dispatchers.IO) {
                RandomAccessFile(index255File, "r")
            }
            val context = ThreadContext()
            val highest = cache.readSectorFiles(main, mainFileLength, index255, indexId, context)
            if (highest == -1) {
                return null
            }
            val archiveArray = arrayOfNulls<Array<ByteArray?>?>(highest + 1)
            withContext(dispatcher) {
                if (processors in 2 until highest) {
                    (0..highest).chunked(highest / processors).map { list ->
                        async {
                            loadArchives(file, list, mainFile, mainFileLength, indexId, xteas, archiveArray, cache)
                        }
                    }.awaitAll()
                } else {
                    loadArchives(file, (0..highest).toList(), mainFile, mainFileLength, indexId, xteas, archiveArray, cache)
                }
            }
            logger.trace { "Loaded ${archiveArray.size} index $indexId archives in ${System.currentTimeMillis() - start}ms." }
            return archiveArray
        } catch (e: Exception) {
            logger.warn(e) { "Failed to load index $indexId." }
        }
        return null
    }

    private fun loadArchives(
        file: File,
        list: List<Int>,
        mainFile: File,
        mainFileLength: Long,
        indexId: Int,
        xteas: Map<Int, IntArray>?,
        output: Array<Array<ByteArray?>?>,
        cache: ReadOnlyCache
    ) {
        val context = ThreadContext()
        val raf = RandomAccessFile(file, "r")
        val main = RandomAccessFile(mainFile, "r")
        for (archiveId in list) {
            val indexFiles = cache.files[indexId]!!
            val fileCounts = cache.fileCounts[indexId]!!
            val archiveFiles = cache.readFileData(fileCounts, indexFiles, indexId, archiveId, main, mainFileLength, raf, xteas, context) ?: continue
            val archiveFileIds = indexFiles[archiveId] ?: continue
            val fileId = archiveFileIds.last()
            val fileCount = fileCounts.getOrNull(archiveId) ?:continue
            val archiveData: Array<ByteArray?> = arrayOfNulls(fileId + 1)
            for (fileIndex in 0 until fileCount) {
                val data = archiveFiles[fileIndex]
                archiveData[archiveFileIds[fileIndex]] = data
            }
            output[archiveId] = archiveData
        }
    }

    companion object {

        private val logger = InlineLogger()
        const val CACHE_FILE_NAME = "main_file_cache"
        private const val INDEX_SIZE = 6

        @JvmStatic
        fun main(args: Array<String>) {
            val path = "./data/cache/"
            val memoryCacheLoader = MemoryCacheLoader()
            var start = System.currentTimeMillis()
            val cache = memoryCacheLoader.load(path, null)
            println("Loaded cache in ${System.currentTimeMillis() - start}ms")
        }
    }
}
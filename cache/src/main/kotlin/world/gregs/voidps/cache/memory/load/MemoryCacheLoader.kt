package world.gregs.voidps.cache.memory.load

import com.displee.cache.CacheLibrary
import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.memory.cache.MemoryCache
import world.gregs.voidps.cache.memory.cache.ReadOnlyCache
import java.io.File
import java.io.RandomAccessFile

object MemoryCacheLoader : CacheLoader {

    private val logger = InlineLogger()

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
        val file = File(path, "${CacheLibrary.CACHE_FILE_NAME}.idx$indexId")
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
            val context = ThreadContext()
            val highest = cache.readArchiveData(context, main, mainFileLength, index255, indexId)
            if (highest == -1) {
                return
            }
            val archiveArray = arrayOfNulls<Array<ByteArray?>?>(highest + 1)
            coroutineScope {
                if (processors in 2 until highest) {
                    for (list in (0..highest).chunked(highest / processors)) {
                        launch {
                            loadArchives(file, list, mainFile, mainFileLength, indexId, xteas, archiveArray, cache)
                        }
                    }
                } else {
                    loadArchives(file, (0..highest).toList(), mainFile, mainFileLength, indexId, xteas, archiveArray, cache)
                }
            }
            logger.trace { "Loaded ${archiveArray.size} index $indexId archives in ${System.currentTimeMillis() - start}ms." }
            cache.data[indexId] = archiveArray
        } catch (e: Exception) {
            logger.warn(e) { "Failed to load index $indexId." }
        }
        return
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
            val archiveFiles = cache.readFileData(context, main, mainFileLength, raf, indexId, archiveId, xteas) ?: continue
            val archiveFileIds = cache.files[indexId]?.get(archiveId) ?: continue
            val fileId = archiveFileIds.last()
            val fileCount = cache.fileCounts[indexId]?.getOrNull(archiveId) ?: continue
            val archiveData: Array<ByteArray?> = arrayOfNulls(fileId + 1)
            for (fileIndex in 0 until fileCount) {
                val data = archiveFiles[fileIndex]
                archiveData[archiveFileIds[fileIndex]] = data
            }
            output[archiveId] = archiveData
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
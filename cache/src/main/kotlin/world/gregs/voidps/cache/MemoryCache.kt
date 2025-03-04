package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import world.gregs.voidps.cache.compress.DecompressionContext
import world.gregs.voidps.cache.secure.VersionTableBuilder
import world.gregs.voidps.cache.secure.Whirlpool
import java.io.File
import java.io.RandomAccessFile
import java.math.BigInteger

/**
 * [Cache] that holds all data in memory
 * Read speeds are as fast, loading is slow and memory usage is high but stable.
 * Loading is done in parallel as it is much slower to load than [FileCache]
 *
 * Not much benefit of using this in the live game as file providers cache the
 * sector data independently for the file server; so the only use after startup is
 * reading dynamic map regions in MapDefinitions.
 * It is however very useful for integration tests to speed world resetting.
 */
class MemoryCache(indexCount: Int) : ReadOnlyCache(indexCount) {

    val data: Array<Array<Array<ByteArray?>?>?> = arrayOfNulls(indexCount)
    val sectors: Array<Array<ByteArray?>?> = arrayOfNulls(indexCount)
    val index255: Array<ByteArray?> = arrayOfNulls(indexCount)

    override fun sector(index: Int, archive: Int): ByteArray? {
        if (index == 255) {
            if (archive >= index255.size) {
                return null
            }
            return index255[archive]
        }
        if (index >= sectors.size) {
            return null
        }
        val archives = sectors[index]
        if (archives == null || archive >= archives.size) {
            return null
        }
        return archives[archive]
    }

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        if (index >= data.size) {
            return null
        }
        val archives = data[index]
        if (archives == null || archive >= archives.size || archive < 0) {
            return null
        }
        val files = archives[archive]
        if (files == null || file >= files.size || file < 0) {
            return null
        }
        return files[file]
    }

    companion object : CacheLoader {
        private val logger = InlineLogger()

        operator fun invoke(path: String, threadUsage: Double = 1.0, exponent: BigInteger? = null, modulus: BigInteger? = null, xteas: Map<Int, IntArray>? = null): Cache {
            return load(path, exponent, modulus, xteas, threadUsage) as ReadOnlyCache
        }

        /**
         * Load each index in parallel using a percentage of cpu cores
         */
        @OptIn(DelicateCoroutinesApi::class)
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
            val cache = MemoryCache(indexCount)
            val processors = (Runtime.getRuntime().availableProcessors() * threadUsage).toInt().coerceAtLeast(1)
            newFixedThreadPoolContext(processors, "cache-loader").use { dispatcher ->
                runBlocking(dispatcher) {
                    supervisorScope {
                        val fileLength = mainFile.length()
                        for (indexId in 0 until indexCount) {
                            launch {
                                loadIndex(path, indexId, mainFile, fileLength, index255File, xteas, processors, cache, versionTable)
                            }
                        }
                    }
                }
            }
            cache.versionTable = versionTable?.build() ?: ByteArray(0)
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
            cache: MemoryCache,
            versionTable: VersionTableBuilder?
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
                val whirlpool = Whirlpool()
                val highest = cache.archiveData(context, main, mainFileLength, index255, indexId, versionTable, whirlpool, cache.index255)
                if (highest == -1) {
                    return
                }
                cache.sectors[indexId] = arrayOfNulls(highest + 1)
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
                val archiveFiles = cache.fileData(
                    context, main, mainFileLength, raf, indexId, archiveId, xteas, cache.sectors
                ) ?: continue
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
    }
}
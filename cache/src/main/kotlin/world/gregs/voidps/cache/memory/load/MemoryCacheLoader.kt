package world.gregs.voidps.cache.memory.load

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index
import com.displee.cache.index.ReferenceTable
import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import kotlinx.coroutines.*
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.memory.InMemory
import world.gregs.voidps.cache.memory.cache.MemoryCache
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile

class MemoryCacheLoader : CacheLoader {
    private val logger = InlineLogger()

    override fun load(path: String, xteas: Map<Int, IntArray>?): Cache {
        val mainFile = File(path, "$CACHE_FILE_NAME.dat2")
        if (!mainFile.exists()) {
            throw FileNotFoundException("Main file not found at '${mainFile.absolutePath}'.")
        }
        val index255File = File(path, "$CACHE_FILE_NAME.idx255")
        if (!index255File.exists()) {
            throw FileNotFoundException("Checksum file not found at '${index255File.absolutePath}'.")
        }
        val mainFileLength = mainFile.length()
        val index255 = RandomAccessFile(index255File, "r")
        val indexCount = index255.length().toInt() / INDEX_SIZE

        val indices = (0 until indexCount).toList()
        val processors = 10//Runtime.getRuntime().availableProcessors()
        val dispatcher = newFixedThreadPoolContext(processors, "cache-loader")
        val archives: Array<IntArray?> = arrayOfNulls(indexCount)
        val fileCounts: Array<IntArray?> = arrayOfNulls(indexCount)
        val files: Array<Array<IntArray?>?> = arrayOfNulls(indexCount)
        val hashNames = Int2IntOpenHashMap(16384)
        val data = runBlocking(dispatcher) {
            val data = (0 until indexCount).map { indexId ->
                async {
                    load(path, indexId, mainFile, mainFileLength, index255File, xteas, dispatcher, processors, archives, fileCounts, files, hashNames)
                }
            }
            data.awaitAll().toTypedArray()
        }
        dispatcher.close()
        return MemoryCache(data, indices.toIntArray(), archives, fileCounts, files, hashNames)
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
        archives: Array<IntArray?>,
        fileCounts: Array<IntArray?>,
        files: Array<Array<IntArray?>?>,
        hashNames: Int2IntOpenHashMap
    ): Array<Array<ByteArray?>?>? {
        val file = File(path, "${CacheLibrary.CACHE_FILE_NAME}.idx$indexId")
        if (!file.exists()) {
            logger.warn { "No index $indexId file found." }
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
            val archiveSector = readArchiveSector(main, mainFileLength, index255, 255, indexId)
            if (archiveSector == null) {
                logger.debug { "Loaded index $indexId. 0" }
                return null
            }
            val context = ThreadContext()
            val decompressed = context.decompress(context, archiveSector, null) ?: return null
            val tableBuffer = BufferReader(decompressed)
            val version = tableBuffer.readUnsignedByte()
            if (version < 5 || version >= 7) {
                throw RuntimeException("Unknown version: $version")
            }
            if (version >= 6) {
                tableBuffer.skip(4) // Revision
            }
            val flags = tableBuffer.readByte()
            val archiveCount = tableBuffer.readUnsignedShort()
            var previous = 0
            var highest = 0
            val archiveIds = IntArray(archiveCount)
            for (i in 0 until archiveCount) {
                val archiveId = tableBuffer.readUnsignedShort() + previous
                previous = archiveId
                archiveIds[i] = archiveId
                if (archiveId > highest) {
                    highest = archiveId
                }
            }
            archives[indexId] = archiveIds
            if (flags and ReferenceTable.FLAG_NAME != 0) {
                for (i in 0 until archiveCount) {
                    val archiveId = archiveIds[i]
                    hashNames[tableBuffer.readInt()] = archiveId
                }
            }
            if (flags and ReferenceTable.FLAG_WHIRLPOOL != 0) {
                tableBuffer.skip(archiveCount * Index.WHIRLPOOL_SIZE)
            }
            tableBuffer.skip(archiveCount * 8) // Crc & revisions
            val archiveIdSizes = IntArray(highest + 1)
            for (i in 0 until archiveCount) {
                val id = archiveIds[i]
                archiveIdSizes[id] = tableBuffer.readUnsignedShort()
            }
            fileCounts[indexId] = archiveIdSizes
            val fileIds = arrayOfNulls<IntArray>(highest + 1)
            for (i in 0 until archiveCount) {
                val archiveId = archiveIds[i]
                val fileCount = archiveIdSizes[archiveId]
                var fileId = 0
                fileIds[archiveId] = IntArray(fileCount) {
                    fileId += tableBuffer.readUnsignedShort()
                    fileId
                }
            }
            files[indexId] = fileIds
            val archiveArray = arrayOfNulls<Array<ByteArray?>?>(highest + 1)
            withContext(dispatcher) {
                if (processors in 2 until highest) {
                    (0..highest).chunked(highest / processors).map { list ->
                        async {
                            loadArchives(file, list, fileIds, mainFile, mainFileLength, indexId, archiveIdSizes, xteas, archiveArray)
                        }
                    }.awaitAll()
                } else {
                    loadArchives(file, (0..highest).toList(), fileIds, mainFile, mainFileLength, indexId, archiveIdSizes, xteas, archiveArray)
                }
            }
            logger.debug { "Loaded ${archiveArray.size} index $indexId archives in ${System.currentTimeMillis() - start}ms." }
            return archiveArray
        } catch (e: Exception) {
            logger.warn(e) { "Failed to load index $indexId." }
        }
        return null
    }

    private fun loadArchives(
        file: File,
        list: List<Int>,
        fileIds: Array<IntArray?>,
        mainFile: File,
        mainFileLength: Long,
        indexId: Int,
        archiveIdSizes: IntArray,
        xteas: Map<Int, IntArray>?,
        output: Array<Array<ByteArray?>?>
    ) {
        val context = ThreadContext()
        val raf = RandomAccessFile(file, "r")
        val main = RandomAccessFile(mainFile, "r")
        for (archiveId in list) {
            val fileIds = fileIds[archiveId] ?: continue
            val sectorData = readArchiveSector(main, mainFileLength, raf, indexId, archiveId) ?: continue
            val fileCount = archiveIdSizes[archiveId]
            val keys = if (indexId == world.gregs.voidps.cache.Index.MAPS) xteas?.get(archiveId) else null
            val decompressed = context.decompress(context, sectorData, keys) ?: continue
            val fileId = fileIds.last()

            if (fileCount == 1) {
                output[archiveId] = Array(fileId + 1) {
                    if (it == fileId) decompressed else null
                }
                continue
            }
            val indexBuffer = BufferReader(decompressed)
            val rawArray = indexBuffer.array()
            var fileDataSizesOffset = decompressed.size
            val chunkSize: Int = rawArray[--fileDataSizesOffset].toInt() and 0xFF
            fileDataSizesOffset -= chunkSize * (fileCount * 4)
            val offsets = IntArray(fileCount)
            indexBuffer.position(fileDataSizesOffset)
            for (i in 0 until chunkSize) {
                var previousLength = 0
                for (fileIndex in 0 until fileCount) {
                    previousLength += indexBuffer.readInt()
                    offsets[fileIndex] += previousLength
                }
            }
            indexBuffer.position(fileDataSizesOffset)
            val archiveFiles: Array<ByteArray> = Array(fileCount) { index ->
                val array = ByteArray(offsets[index])
                offsets[index] = 0
                array
            }
            var offset = 0
            for (i in 0 until chunkSize) {
                var length = 0
                for (fileIndex in 0 until fileCount) {
                    val read = indexBuffer.readInt()
                    val fileData = archiveFiles[fileIndex]
                    length += read
                    System.arraycopy(rawArray, offset, fileData, offsets[fileIndex], length)
                    offset += length
                    offsets[fileIndex] += length
                }
            }
            val archiveData: Array<ByteArray?> = arrayOfNulls(fileId + 1)
            for (fileIndex in 0 until fileCount) {
                val data = archiveFiles[fileIndex]
                archiveData[fileIds[fileIndex]] = data
            }
            output[archiveId] = archiveData
        }
    }

    fun readArchiveSector(mainFile: RandomAccessFile, length: Long, raf: RandomAccessFile, indexId: Int, sectorId: Int): ByteArray? {
        if (length < Index.INDEX_SIZE * sectorId + Index.INDEX_SIZE) {
            return null
        }
        raf.seek(sectorId.toLong() * Index.INDEX_SIZE)
        val sectorData = ByteArray(Index.SECTOR_SIZE)
        raf.read(sectorData, 0, Index.INDEX_SIZE)
        val bigSector = sectorId > 65535
        val buffer = BufferReader(sectorData)
        val sectorSize = buffer.readUnsignedMedium()
        var sectorPosition = buffer.readUnsignedMedium()
        if (sectorSize < 0 || sectorPosition <= 0 || sectorPosition > mainFile.length() / Index.SECTOR_SIZE) {
            return null
        }
        var read = 0
        var chunk = 0
        val sectorHeaderSize = if (bigSector) Index.SECTOR_HEADER_SIZE_BIG else Index.SECTOR_HEADER_SIZE_SMALL
        val sectorDataSize = if (bigSector) Index.SECTOR_DATA_SIZE_BIG else Index.SECTOR_DATA_SIZE_SMALL
        val output = ByteArray(sectorSize)
        while (read < sectorSize) {
            if (sectorPosition == 0) {
                return null
            }
            var requiredToRead = sectorSize - read
            if (requiredToRead > sectorDataSize) {
                requiredToRead = sectorDataSize
            }
            mainFile.seek(sectorPosition.toLong() * Index.SECTOR_SIZE)
            mainFile.read(sectorData, 0, requiredToRead + sectorHeaderSize)
            buffer.position(0)
            val id = if (bigSector) buffer.readInt() else buffer.readUnsignedShort()
            val sectorChunk = buffer.readUnsignedShort()
            val sectorNextPosition = buffer.readUnsignedMedium()
            val sectorIndex = buffer.readUnsignedByte()
            if (sectorIndex != indexId || id != sectorId || sectorChunk != chunk) {
                return null
            } else if (sectorNextPosition < 0 || sectorNextPosition > mainFile.length() / Index.SECTOR_SIZE) {
                return null
            }
            System.arraycopy(sectorData, sectorHeaderSize, output, read, requiredToRead)
            read += requiredToRead
            sectorPosition = sectorNextPosition
            chunk++
        }
        return output
    }

    companion object {
        private const val NAME_FLAG = 0x1
        private const val WHIRLPOOL_FLAG = 0x2
        const val CACHE_FILE_NAME = "main_file_cache"
        private const val INDEX_SIZE = 6

        @JvmStatic
        fun main(args: Array<String>) {
            val path = "./data/cache/test/"
            val xteas = InMemory.loadBinary(File("./data/xteas.dat"))
            var start = System.currentTimeMillis()
            val cache = MemoryCacheLoader().load(path, xteas)
            println("Loaded cache in ${System.currentTimeMillis() - start}ms")
            start = System.currentTimeMillis()
            var count = 0
            println("Loaded $count maps in ${System.currentTimeMillis() - start}ms")
        }
    }
}
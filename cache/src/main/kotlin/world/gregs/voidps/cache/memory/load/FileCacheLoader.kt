package world.gregs.voidps.cache.memory.load

import com.displee.cache.index.Index
import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.memory.cache.FileCache
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile

class FileCacheLoader : CacheLoader {

    override fun load(path: String, xteas: Map<Int, IntArray>?): Cache {
        val mainFile = File(path, "$CACHE_FILE_NAME.dat2")
        if (!mainFile.exists()) {
            throw FileNotFoundException("Main file not found at '${mainFile.absolutePath}'.")
        }
        val main = RandomAccessFile(mainFile, "r")
        val index255File = File(path, "$CACHE_FILE_NAME.idx255")
        if (!index255File.exists()) {
            throw FileNotFoundException("Checksum file not found at '${index255File.absolutePath}'.")
        }
        val index255 = RandomAccessFile(index255File, "r")
        val indexCount = index255.length().toInt() / INDEX_SIZE
        val indices = Array(indexCount) { indexId ->
            val file = File(path, "$CACHE_FILE_NAME.idx$indexId")
            if (file.exists()) RandomAccessFile(file, "r") else null
        }
        val archives: Array<IntArray?> = arrayOfNulls(indexCount)
        val fileCounts: Array<IntArray?> = arrayOfNulls(indexCount)
        val files: Array<Array<IntArray?>?> = arrayOfNulls(indexCount)
        val length = mainFile.length()
        val hashes = Int2IntOpenHashMap(16384)
        val context = ThreadContext()
        for (indexId in 0 until indexCount) {
            loadSectorIntoFiles(main, length, index255, indexId, archives, hashes, fileCounts, files, context)
        }
        return FileCache(main, indices, (0 until indexCount).toList().toIntArray(), archives, fileCounts, files, hashes, xteas)
    }

    companion object {
        private val logger = InlineLogger()

        private fun BufferReader.readSmart(version: Int) = if (version >= 7) readBigSmart() else readUnsignedShort()

        fun loadSectorIntoFiles(
            main: RandomAccessFile,
            length: Long,
            index255: RandomAccessFile,
            indexId: Int,
            archives: Array<IntArray?>,
            hashes: Int2IntOpenHashMap,
            fileCounts: Array<IntArray?>,
            files: Array<Array<IntArray?>?>,
            context: ThreadContext
        ): Int {
            val archiveSector = readArchiveSector(main, length, index255, 255, indexId)
            if (archiveSector == null) {
                logger.trace { "Empty index $indexId." }
                return -1
            }
            val decompressed = context.decompress(archiveSector, null) ?: return -1
            val tableBuffer = BufferReader(decompressed)
            val version = tableBuffer.readUnsignedByte()
            if (version < 5 || version > 7) {
                throw RuntimeException("Unknown version: $version")
            }
            if (version >= 6) {
                tableBuffer.skip(4) // revision
            }
            val flags = tableBuffer.readByte()
            val archiveCount = tableBuffer.readSmart(version)
            var previous = 0
            var highest = 0
            val archiveIds = IntArray(archiveCount) {
                val archiveId = tableBuffer.readSmart(version) + previous
                previous = archiveId
                if (archiveId > highest) {
                    highest = archiveId
                }
                archiveId
            }
            archives[indexId] = archiveIds
            if (flags and NAME_FLAG != 0) {
                for (i in 0 until archiveCount) {
                    val archiveId = archiveIds[i]
                    hashes[tableBuffer.readInt()] = archiveId
                }
            }
            if (flags and WHIRLPOOL_FLAG != 0) {
                tableBuffer.skip(archiveCount * Index.WHIRLPOOL_SIZE)
            }
            tableBuffer.skip(archiveCount * 8) // Crc & revisions
            val archiveSizes = IntArray(highest + 1)
            for (i in 0 until archiveCount) {
                val id = archiveIds[i]
                val size = tableBuffer.readSmart(version)
                archiveSizes[id] = size
            }
            fileCounts[indexId] = archiveSizes
            val fileIds = arrayOfNulls<IntArray>(highest + 1)
            files[indexId] = fileIds
            for (i in 0 until archiveCount) {
                var fileId = 0
                val archiveId = archiveIds[i]
                val fileCount = archiveSizes[archiveId]
                fileIds[archiveId] = IntArray(fileCount) {
                    fileId += tableBuffer.readSmart(version)
                    fileId
                }
            }
            return highest
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
        private const val NAME_FLAG = 0x1
        private const val WHIRLPOOL_FLAG = 0x2
        const val CACHE_FILE_NAME = "main_file_cache"
        private const val INDEX_SIZE = 6

        @JvmStatic
        fun main(args: Array<String>) {
            val path = "./data/cache/"

            val memCache = MemoryCacheLoader().load(path)
            val start = System.currentTimeMillis()
            val cache = FileCacheLoader().load(path)
            println("Loaded cache in ${System.currentTimeMillis() - start}ms")

            var count = 0
            for (index in 0 until cache.indexes()) {
                val archives = cache.archives(index) ?: continue
                for (archive in archives) {
                    val files = cache.files(index, archive) ?: continue
                    for (file in files) {
                        val expected = memCache.data(index, archive, file)
                        val actual = cache.data(index, archive, file)
                        if (!expected.contentEquals(actual)) {
                            println("Mismatch $index $archive $file")
                            println("${expected?.take(10)} ${actual?.take(10)}")
                        }
                        count++
                    }
                }
            }
            println("Loaded $count files in ${System.currentTimeMillis() - start}ms")
        }
    }
}
package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.compress.DecompressionContext
import java.io.RandomAccessFile

/**
 * [Cache] which efficiently stores information about its indexes, archives and files.
 */
open class ReadOnlyCache(
    val indices: IntArray,
    val archives: Array<IntArray?>,
    val fileCounts: Array<IntArray?>,
    val files: Array<Array<IntArray?>?>,
    private val hashes: MutableMap<Int, Int>
) : Cache {

    constructor(indexCount: Int) : this(IntArray(indexCount) { it }, arrayOfNulls(indexCount), arrayOfNulls(indexCount), arrayOfNulls(indexCount), Int2IntOpenHashMap(16384))

    @Suppress("UNCHECKED_CAST")
    internal fun readFileData(
        context: DecompressionContext,
        main: RandomAccessFile,
        mainLength: Long,
        indexRaf: RandomAccessFile,
        indexId: Int,
        archiveId: Int,
        xteas: Map<Int, IntArray>?
    ): Array<ByteArray?>? {
        val fileCounts = fileCounts[indexId] ?: return null
        val fileIds = files[indexId] ?: return null
        val fileCount = fileCounts.getOrNull(archiveId) ?: return null
        val sectorData = readSector(main, mainLength, indexRaf, indexId, archiveId) ?: return null
        val keys = if (xteas != null && indexId == Index.MAPS) xteas[archiveId] else null
        val decompressed = context.decompress(sectorData, keys) ?: return null

        if (fileCount == 1) {
            val fileId = fileIds[archiveId]?.last() ?: return null
            return Array(fileId + 1) {
                if (it == fileId) decompressed else null
            }
        }

        val reader = BufferReader(decompressed)
        val rawArray = reader.array()
        var fileDataSizesOffset = decompressed.size
        val chunkSize: Int = rawArray[--fileDataSizesOffset].toInt() and 0xFF
        fileDataSizesOffset -= chunkSize * (fileCount * 4)
        val offsets = IntArray(fileCount)
        reader.position(fileDataSizesOffset)
        for (i in 0 until chunkSize) {
            var previousLength = 0
            for (fileIndex in 0 until fileCount) {
                previousLength += reader.readInt()
                offsets[fileIndex] += previousLength
            }
        }
        val archiveFiles = Array(fileCount) { index ->
            val array = ByteArray(offsets[index])
            offsets[index] = 0
            array
        }
        var offset = 0
        reader.position(fileDataSizesOffset)
        for (i in 0 until chunkSize) {
            var length = 0
            for (fileIndex in 0 until fileCount) {
                val read = reader.readInt()
                val fileData = archiveFiles[fileIndex]
                length += read
                System.arraycopy(rawArray, offset, fileData, offsets[fileIndex], length)
                offset += length
                offsets[fileIndex] += length
            }
        }
        return archiveFiles as Array<ByteArray?>
    }

    internal fun readArchiveData(
        context: DecompressionContext,
        main: RandomAccessFile,
        length: Long,
        index255: RandomAccessFile,
        indexId: Int
    ): Int {
        val archiveSector = readSector(main, length, index255, 255, indexId)
        if (archiveSector == null) {
            logger.trace { "Empty index $indexId." }
            return -1
        }
        val decompressed = context.decompress(archiveSector) ?: return -1
        val reader = BufferReader(decompressed)
        val version = reader.readUnsignedByte()
        if (version < 5 || version > 7) {
            throw RuntimeException("Unknown version: $version")
        }
        if (version >= 6) {
            reader.skip(4) // revision
        }
        val flags = reader.readByte()
        val archiveCount = reader.readSmart(version)
        var previous = 0
        var highest = 0
        val archiveIds = IntArray(archiveCount) {
            val archiveId = reader.readSmart(version) + previous
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
                hashes[reader.readInt()] = archiveId
            }
        }
        if (flags and WHIRLPOOL_FLAG != 0) {
            reader.skip(archiveCount * WHIRLPOOL_SIZE)
        }
        reader.skip(archiveCount * 8) // Crc & revisions
        val archiveSizes = IntArray(highest + 1)
        for (i in 0 until archiveCount) {
            val id = archiveIds[i]
            val size = reader.readSmart(version)
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
                fileId += reader.readSmart(version)
                fileId
            }
        }
        return highest
    }

    override fun files(index: Int, archive: Int): IntArray? {
        return files.getOrNull(index)?.getOrNull(archive)
    }

    override fun archives(index: Int): IntArray? {
        return archives.getOrNull(index)
    }

    override fun indexes(): Int {
        return indices.size
    }

    override fun indices(): IntArray {
        return indices
    }

    override fun archiveCount(indexId: Int, archiveId: Int): Int {
        return fileCounts.getOrNull(indexId)?.getOrNull(archiveId) ?: 0
    }

    override fun lastFileId(indexId: Int, archive: Int): Int {
        return files.getOrNull(indexId)?.getOrNull(archive)?.last() ?: -1
    }

    override fun lastArchiveId(indexId: Int): Int {
        return archives.getOrNull(indexId)?.last() ?: -1
    }

    override fun archiveId(name: String): Int {
        return hashes[name.hashCode()] ?: -1
    }

    override fun getFile(index: Int, name: String, xtea: IntArray?): ByteArray? {
        return data(index, archiveId(name), 0, xtea)
    }

    override fun getFile(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        return data(index, archive, file, xtea)
    }

    override fun close() {
    }

    override fun getArchiveId(index: Int, name: String): Int {
        return archiveId(name)
    }

    override fun getIndexCrc(indexId: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getArchiveId(index: Int, archive: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getArchives(index: Int): IntArray {
        return archives(index) ?: IntArray(0)
    }

    override fun write(index: Int, archive: Int, file: Int, data: ByteArray, xteas: IntArray?) {
        TODO("Not yet implemented")
    }

    override fun write(index: Int, archive: String, data: ByteArray, xteas: IntArray?) {
        TODO("Not yet implemented")
    }

    override fun update(): Boolean {
        return false
    }

    override fun getArchiveData(index: Int, archive: Int): Map<Int, ByteArray?>? {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = InlineLogger()
        private const val NAME_FLAG = 0x1
        private const val WHIRLPOOL_FLAG = 0x2

        const val INDEX_SIZE = 6
        private const val WHIRLPOOL_SIZE = 64
        private const val SECTOR_SIZE = 520
        private const val SECTOR_HEADER_SIZE_SMALL = 8
        private const val SECTOR_DATA_SIZE_SMALL = 512
        private const val SECTOR_HEADER_SIZE_BIG = 10
        private const val SECTOR_DATA_SIZE_BIG = 510

        private fun BufferReader.readSmart(version: Int) = if (version >= 7) readBigSmart() else readUnsignedShort()

        /**
         * Reads a section of a cache's archive
         */
        private fun readSector(mainFile: RandomAccessFile, length: Long, raf: RandomAccessFile, indexId: Int, sectorId: Int): ByteArray? {
            if (length < INDEX_SIZE * sectorId + INDEX_SIZE) {
                return null
            }
            raf.seek(sectorId.toLong() * INDEX_SIZE)
            val sectorData = ByteArray(SECTOR_SIZE)
            raf.read(sectorData, 0, INDEX_SIZE)
            val bigSector = sectorId > 65535
            val buffer = BufferReader(sectorData)
            val sectorSize = buffer.readUnsignedMedium()
            var sectorPosition = buffer.readUnsignedMedium()
            if (sectorSize < 0 || sectorPosition <= 0 || sectorPosition > mainFile.length() / SECTOR_SIZE) {
                return null
            }
            var read = 0
            var chunk = 0
            val sectorHeaderSize = if (bigSector) SECTOR_HEADER_SIZE_BIG else SECTOR_HEADER_SIZE_SMALL
            val sectorDataSize = if (bigSector) SECTOR_DATA_SIZE_BIG else SECTOR_DATA_SIZE_SMALL
            val output = ByteArray(sectorSize)
            while (read < sectorSize) {
                if (sectorPosition == 0) {
                    return null
                }
                var requiredToRead = sectorSize - read
                if (requiredToRead > sectorDataSize) {
                    requiredToRead = sectorDataSize
                }
                mainFile.seek(sectorPosition.toLong() * SECTOR_SIZE)
                mainFile.read(sectorData, 0, requiredToRead + sectorHeaderSize)
                buffer.position(0)
                val id = if (bigSector) buffer.readInt() else buffer.readUnsignedShort()
                val sectorChunk = buffer.readUnsignedShort()
                val sectorNextPosition = buffer.readUnsignedMedium()
                val sectorIndex = buffer.readUnsignedByte()
                if (sectorIndex != indexId || id != sectorId || sectorChunk != chunk) {
                    return null
                } else if (sectorNextPosition < 0 || sectorNextPosition > mainFile.length() / SECTOR_SIZE) {
                    return null
                }
                System.arraycopy(sectorData, sectorHeaderSize, output, read, requiredToRead)
                read += requiredToRead
                sectorPosition = sectorNextPosition
                chunk++
            }
            return output
        }
    }

}
package world.gregs.voidps.cache.memory.cache

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.memory.load.Archive
import world.gregs.voidps.cache.memory.load.ThreadContext
import java.io.RandomAccessFile

open class ReadOnlyCache(
    val indices: IntArray,
    val archives: Array<IntArray?>,
    val fileCounts: Array<IntArray?>,
    val files: Array<Array<IntArray?>?>,
    private val hashes: MutableMap<Int, Int>
) : Cache {

    constructor(indexCount: Int) : this(IntArray(indexCount) { it }, arrayOfNulls(indexCount), arrayOfNulls(indexCount), arrayOfNulls(indexCount), Int2IntOpenHashMap(16384))

    @Suppress("UNCHECKED_CAST")
    fun readFileData(
        context: ThreadContext,
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
        val sectorData = Archive.readSector(main, mainLength, indexRaf, indexId, archiveId) ?: return null
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

    fun readArchiveData(
        context: ThreadContext,
        main: RandomAccessFile,
        length: Long,
        index255: RandomAccessFile,
        indexId: Int
    ): Int {
        val archiveSector = Archive.readSector(main, length, index255, 255, indexId)
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
        private const val WHIRLPOOL_SIZE = 64

        private fun BufferReader.readSmart(version: Int) = if (version >= 7) readBigSmart() else readUnsignedShort()
    }

}
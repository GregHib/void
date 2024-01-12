package world.gregs.voidps.cache.memory.cache

import com.displee.cache.index.Index
import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.memory.load.Archive
import world.gregs.voidps.cache.memory.load.Archive.readArchiveSector
import world.gregs.voidps.cache.memory.load.ThreadContext
import java.io.RandomAccessFile

open class ReadOnlyCache(
    val indices: IntArray,
    val archives: Array<IntArray?>,
    val fileCounts: Array<IntArray?>,
    val files: Array<Array<IntArray?>?>,
    val hashes: MutableMap<Int, Int>
) : Cache {

    constructor(indexCount: Int) : this(IntArray(indexCount) { it }, arrayOfNulls(indexCount), arrayOfNulls(indexCount), arrayOfNulls(indexCount), Int2IntOpenHashMap(16384))

    fun readFileData(
        fileCounts: IntArray,
        fileIds: Array<IntArray?>,
        index: Int,
        archive: Int,
        main: RandomAccessFile,
        length: Long,
        indexRaf: RandomAccessFile,
        xteas: Map<Int, IntArray>?,
        context: ThreadContext
    ): Array<ByteArray?>? {
        val fileCount = fileCounts.getOrNull(archive) ?: return null
        val sectorData = readArchiveSector(main, length, indexRaf, index, archive) ?: return null
        val keys = if (xteas != null && index == world.gregs.voidps.cache.Index.MAPS) xteas[archive] else null
        val decompressed = context.decompress(sectorData, keys) ?: return null

        if (fileCount == 1) {
            val fileId = fileIds[archive]?.last() ?: return null
            return Array(fileId + 1) {
                if (it == fileId) decompressed else null
            }
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
        val archiveFiles: Array<ByteArray?> = Array(fileCount) { index ->
            val array = ByteArray(offsets[index])
            offsets[index] = 0
            array
        }
        var offset = 0
        indexBuffer.position(fileDataSizesOffset)
        for (i in 0 until chunkSize) {
            var length = 0
            for (fileIndex in 0 until fileCount) {
                val read = indexBuffer.readInt()
                val fileData = archiveFiles[fileIndex]!!
                length += read
                System.arraycopy(rawArray, offset, fileData, offsets[fileIndex], length)
                offset += length
                offsets[fileIndex] += length
            }
        }
        return archiveFiles
    }
    fun readSectorFiles(
        main: RandomAccessFile,
        length: Long,
        index255: RandomAccessFile,
        indexId: Int,
        context: ThreadContext
    ): Int {
        val archiveSector = Archive.readArchiveSector(main, length, index255, 255, indexId)
        if (archiveSector == null) {
            logger.trace { "Empty index $indexId." }
            return -1
        }
        val decompressed = context.decompress(archiveSector) ?: return -1
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

    companion object {
        private val logger = InlineLogger()
        private fun BufferReader.readSmart(version: Int) = if (version >= 7) readBigSmart() else readUnsignedShort()
        private const val NAME_FLAG = 0x1
        private const val WHIRLPOOL_FLAG = 0x2
        const val INDEX_SIZE = 6
        const val CACHE_FILE_NAME = "main_file_cache"
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
        return hashes?.get(name.hashCode()) ?: -1
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

}
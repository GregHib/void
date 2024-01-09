package world.gregs.voidps.cache.memory

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import java.io.RandomAccessFile

class FileCache(
    private val main: RandomAccessFile,
    private val indices: Array<RandomAccessFile>,
    private val archives: Array<IntArray?>,
    private val fileCounts: Array<IntArray?>,
    private val files: Array<Array<IntArray?>?>,
    private val xteas: Map<Int, IntArray>?
) : Cache {
    private val dataCache = object : LinkedHashMap<Int, Array<ByteArray?>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Array<ByteArray?>>?): Boolean {
            return size > 10
        }
    }
    private val length = main.length()
    private val sectorReader = ArchiveSectorReader()
    private val decompressor = Decompressor(4_702_000)

    private fun hash(index: Int, archive: Int) = index + (archive shl 4)

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        val matchingIndex = files[index]?.get(archive)?.indexOf(file) ?: -1
        if (matchingIndex == -1) {
            return null
        }
        val files = dataCache.getOrPut(hash(index, archive)) {
            val indexRaf = indices[index]
            val sectorSize = sectorReader.read(main, length, indexRaf, index, archive)
            if (sectorSize == 0) {
                return null
            }
            val keys = if (index == Index.MAPS) xteas?.get(archive) else null
            val decompressedSize = decompressor.decompress(sectorReader.data, sectorSize, keys)
            if (decompressedSize == 0) {
                return null
            }

            val fileCount = fileCounts[index]!![archive]
            if (fileCount == 1) {
                return decompressor.data.copyOf(decompressedSize)
            }

            val indexBuffer = BufferReader(decompressor.data)
            val rawArray = indexBuffer.array()
            var fileDataSizesOffset = decompressedSize
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
            archiveFiles
        }
        return files[matchingIndex]
    }

    override fun files(index: Int, archive: Int): IntArray? {
        return files[index]?.get(archive)
    }

    override fun archives(index: Int): IntArray? {
        return archives[index]
    }

    override fun indexes(): Int {
        return indices.size
    }

    override fun getFile(index: Int, name: String, xtea: IntArray?): ByteArray? {
        return getFile(index, name.hashCode(), 0, xtea)
    }


    override fun getFile(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun close() {
    }

    override fun getIndexCrc(indexId: Int): Int {
        TODO("Not yet implemented")
    }

    override fun archiveCount(indexId: Int, archiveId: Int): Int {
        return fileCounts[indexId]?.get(archiveId) ?: 0
    }

    override fun lastFileId(indexId: Int, archive: Int): Int {
        return files[indexId]?.get(archive)?.last() ?: -1
    }

    override fun lastArchiveId(indexId: Int): Int {
        return archives[indexId]?.last() ?: -1
    }

    override fun getArchiveId(index: Int, name: String): Int {
        TODO("Not yet implemented")
    }

    override fun getArchiveId(index: Int, archive: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getArchives(index: Int): IntArray {
        TODO("Not yet implemented")
    }

    override fun write(index: Int, archive: Int, file: Int, data: ByteArray, xteas: IntArray?) {
        TODO("Not yet implemented")
    }

    override fun write(index: Int, archive: String, data: ByteArray, xteas: IntArray?) {
        TODO("Not yet implemented")
    }

    override fun update(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getArchiveData(index: Int, archive: Int): Map<Int, ByteArray?>? {
        TODO("Not yet implemented")
    }

}
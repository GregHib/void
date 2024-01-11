package world.gregs.voidps.cache.memory.cache

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.memory.ArchiveSectorReader
import world.gregs.voidps.cache.memory.Decompressor
import java.io.RandomAccessFile

class FileCache(
    private val main: RandomAccessFile,
    private val indexes: Array<RandomAccessFile>,
    indices: IntArray,
    archives: Array<IntArray?>,
    fileCounts: Array<IntArray?>,
    files: Array<Array<IntArray?>?>,
    hashes: Map<Int, Int>?,
    val xteas: Map<Int, IntArray>?
) : ReadOnlyCache(indices, archives, fileCounts, files, hashes) {
    private val dataCache = object : LinkedHashMap<Int, Array<ByteArray?>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Array<ByteArray?>>?): Boolean {
            return size > 12
        }
    }
    private val length = main.length()
    private val sectorReader = ArchiveSectorReader()
    private val decompressor = Decompressor(5_000_000)

    private fun hash(index: Int, archive: Int) = index + (archive shl 4)

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        val matchingIndex = files[index]?.get(archive)?.indexOf(file) ?: -1
        if (matchingIndex == -1) {
            return null
        }
        val files = dataCache.getOrPut(hash(index, archive)) {
            val indexRaf = indexes[index]
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
}
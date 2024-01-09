package world.gregs.voidps.cache.memory

import com.displee.cache.index.Index
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile

class FileCacheLoader : CacheLoader {
    private val sectorReader = ArchiveSectorReader()
    private val decompressor = Decompressor()

    override fun load(path: String, xteas: Map<Int, IntArray>?): Cache {
        val mainFile = File(path, "${CACHE_FILE_NAME}.dat2")
        if (!mainFile.exists()) {
            throw FileNotFoundException("Main file not found at '${mainFile.absolutePath}'.")
        }
        val main = RandomAccessFile(mainFile, "r")
        val index255File = File(path, "${CACHE_FILE_NAME}.idx255")
        if (!index255File.exists()) {
            throw FileNotFoundException("Checksum file not found at '${index255File.absolutePath}'.")
        }
        val index255 = RandomAccessFile(index255File, "r")
        val indexCount = index255.length().toInt() / INDEX_SIZE
        val indices = Array(indexCount) { indexId ->
            val file = File(path, "${CACHE_FILE_NAME}.idx$indexId")
            RandomAccessFile(file, "r")
        }
        val archives: Array<IntArray?> = arrayOfNulls(indexCount)
        val fileCounts: Array<IntArray?> = arrayOfNulls(indexCount)
        val files: Array<Array<IntArray?>?> = arrayOfNulls(indexCount)
        val length = mainFile.length()
        for (indexId in 0 until indexCount) {
            val compressedSize = sectorReader.read(main, length, index255, indexId = 255, sectorId = indexId)
            val decompressedSize = decompressor.decompress(sectorReader.data, compressedSize)
            val tableBuffer = BufferReader(decompressor.data)
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
            files[indexId] = arrayOfNulls(highest + 1)
            if (flags and NAME_FLAG != 0) {
                tableBuffer.skip(archiveCount * 4)
            }
            if (flags and WHIRLPOOL_FLAG != 0) {
                tableBuffer.skip(archiveCount * Index.WHIRLPOOL_SIZE)
            }
            tableBuffer.skip(archiveCount * 8) // Crc & revisions
            val archiveSizes = IntArray(highest + 1)
            for(i in 0 until archiveCount) {
                val id = archiveIds[i]
                val size = tableBuffer.readSmart(version)
                archiveSizes[id] = size
            }
            fileCounts[indexId] = archiveSizes
            for (i in 0 until archiveCount) {
                var fileId = 0
                val archiveId = archiveIds[i]
                val fileCount = archiveSizes[archiveId]
                files[indexId]!![archiveId] = IntArray(fileCount) {
                    fileId += tableBuffer.readSmart(version)
                    fileId
                }
            }
        }
        return FileCache(main, indices, archives, fileCounts, files, xteas)
    }

    private fun BufferReader.readSmart(version: Int) = if (version >= 7) readBigSmart() else readUnsignedShort()

    companion object {
        private const val NAME_FLAG = 0x1
        private const val WHIRLPOOL_FLAG = 0x2
        const val CACHE_FILE_NAME = "main_file_cache"
        private const val INDEX_SIZE = 6
    }
}
package world.gregs.voidps.cache.memory

import com.displee.cache.CacheLibrary
import com.displee.cache.compress.type.BZIP2Compressor
import com.displee.cache.index.Index
import com.displee.cache.index.ReferenceTable
import com.displee.compress.type.LZMACompressor
import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.secure.Xtea
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.util.*
import java.util.zip.Inflater
import kotlin.system.exitProcess

class InMemory {

    fun load(path: String): Map<Int, Map<Int, Map<Int, ByteArray?>>> {
        val main = File(path, "${CacheLibrary.CACHE_FILE_NAME}.dat2")
        val mainFile = if (main.exists()) {
            RandomAccessFile(main, "rw")
        } else {
            throw FileNotFoundException("Main file not found at '${main.absolutePath}'.")
        }
        val mainFileLength = mainFile.length()
        val index255File = File(path, "${CacheLibrary.CACHE_FILE_NAME}.idx255")
        if (!index255File.exists()) {
            throw FileNotFoundException("Checksum file not found at '${index255File.absolutePath}'.")
        }
        val index255Raf = RandomAccessFile(index255File, "rw")
        logger.trace { "Reading indices..." }
        val indicesLength = index255Raf.length().toInt() / Index.INDEX_SIZE
        val output = mutableMapOf<Int, Map<Int, Map<Int, ByteArray?>>>()
        for (indexId in 0 until indicesLength) {
            val file = File(path, "${CacheLibrary.CACHE_FILE_NAME}.idx$indexId")
            if (!file.exists()) {
                logger.warn { "No index $indexId file found." }
                continue
            }
            try {
                val archives = TreeMap<Int, MutableMap<Int, ByteArray?>>()
                val archiveSectorData = readArchiveSector(mainFile, mainFileLength, index255Raf, 255, indexId)
                if (archiveSectorData == null) {
                    output[indexId] = archives
                    logger.debug { "Loaded index $indexId. 0" }
                    continue
                }
                val tableDecompressed = decompress(archiveSectorData)
                val tableBuffer = BufferReader(tableDecompressed)
                val version = tableBuffer.readUnsignedByte()
                if (version < 5 || version > 7) {
                    throw RuntimeException("Unknown version: $version")
                }
                val revision = if (version >= 6) tableBuffer.readInt() else 0
                val flags = tableBuffer.readByte()
                val named = flags and ReferenceTable.FLAG_NAME != 0
                val hasWhirlPool = flags and ReferenceTable.FLAG_WHIRLPOOL != 0

                val archiveCount = readValue(version, tableBuffer)
                var previous = 0
                val archiveIds = IntArray(archiveCount) {
                    val archiveId = readValue(version, tableBuffer) + previous
                    previous = archiveId
                    archiveId
                }
                if (named) {
                    tableBuffer.skip(archiveCount * 4)
                }
                if (hasWhirlPool) {
                    tableBuffer.skip(archiveCount * Index.WHIRLPOOL_SIZE)
                }
                tableBuffer.skip(archiveCount * 4) // Crc
                tableBuffer.skip(archiveCount * 4) // Revision
                val archiveFileSizes = IntArray(archiveCount) { readValue(version, tableBuffer) }

                val raf = RandomAccessFile(file, "r")
                for (archiveIndex in 0 until archiveCount) {
                    val archiveId = archiveIds[archiveIndex]
                    val fileCount = archiveFileSizes[archiveIndex]
                    val sector = readArchiveSector(mainFile, mainFileLength, raf, indexId, archiveId) ?: continue
                    var fileId = 0
                    val archiveFiles = archives.getOrPut(archiveId) { mutableMapOf() }
                    val fileIds = IntArray(fileCount) {
                        fileId += readValue(version, tableBuffer)
                        fileId
                    }
                    val indexDecompressed = decompress(sector, null)
                    if (indexDecompressed.isEmpty()) {
                        // TODO we don't really need this
                        if (fileCount == 1) {
                            archiveFiles[fileId] = null
                        } else {
                            for (fileId in fileIds) {
                                archiveFiles[fileId] = null
                            }
                        }
                        continue
                    }
                    if (fileCount == 1) {
                        archiveFiles[fileId] = indexDecompressed
                        continue
                    }
                    /*
                        [chunk 0, file 0] // The same file
                        [chunk 0, file 1]
                        [chunk 0, file 2]
                        [chunk 2, file 0] // The same file
                        [chunk 3, file 0]
                        [chunk 3, file 1]
                        Offset
                     */
                    val indexBuffer = BufferReader(indexDecompressed)
                    val rawArray = indexBuffer.array()
                    var fileDataSizesOffset = rawArray.size
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
                    var offset = 0
                    for (i in 0 until chunkSize) {
                        var length = 0
                        for (fileIndex in 0 until fileCount) {
                            val read = indexBuffer.readInt()
                            val id = fileIds[fileIndex]
                            var fileData = archiveFiles[id]
                            if (fileData == null) {
                                fileData = ByteArray(offsets[fileIndex])
                                archiveFiles[id] = fileData
                                offsets[fileIndex] = 0
                            }
                            length += read
                            System.arraycopy(rawArray, offset, fileData, offsets[fileIndex], length)
                            offset += length
                            offsets[fileIndex] += length
                        }
                    }
                }
                output[indexId] = archives
                logger.debug { "Loaded index $indexId. ${archives.size}" }
            } catch (e: Exception) {
                logger.warn(e) { "Failed to load index $indexId." }
            }
        }
        return output
    }

    fun decompress(data: ByteArray, keys: IntArray? = null): ByteArray {
        if (keys != null && (keys[0] != 0 || keys[1] != 0 || keys[2] != 0 || 0 != keys[3])) {
            logger.info { "Deciphering xteas: ${keys.contentToString()}" }
            Xtea.decipher(data, keys, 5)
        }
        val buffer = BufferReader(data)
        val type = buffer.readUnsignedByte()
        val compressedSize = buffer.readInt() and 0xFFFFFF
        var decompressedSize = 0
        if (type != 0) {
            decompressedSize = buffer.readInt() and 0xFFFFFF
        }
        var decompressed = ByteArray(decompressedSize)
        when (type) {
            0 -> decompressed = ByteArray(compressedSize).apply { buffer.readBytes(this) }
            1 -> BZIP2Compressor.decompress(decompressed, decompressed.size, data, compressedSize, 9)
            2 -> if (!inflate(buffer, decompressed)) return byteArrayOf()
            3 -> {
                val output = BufferWriter(buffer.remaining)
                output.writeBytes(buffer.array(), buffer.position(), buffer.remaining)
                decompressed = LZMACompressor.decompress(output.toArray(), decompressedSize)
            }

        }
        return decompressed
    }

    private val inflater = Inflater(true)

    fun inflate(buffer: BufferReader, data: ByteArray): Boolean {
        val bytes = buffer.array()
        val offset = buffer.position()
        if (bytes[offset].toInt() != 31 || bytes[offset + 1].toInt() != -117) {
            return false
        }
        val inflater = this.inflater
        try {
            inflater.setInput(bytes, offset + 10, bytes.size - (offset + 18))
            inflater.inflate(data)
        } catch (exception: Exception) {
            return false
        } finally {
            inflater.reset()
        }
        return true
    }

    val sectorData = ByteArray(Index.SECTOR_SIZE)

    fun readArchiveSector(mainFile: RandomAccessFile, length: Long, raf: RandomAccessFile, indexId: Int, sectorId: Int): ByteArray? {
        if (length < Index.INDEX_SIZE * sectorId + Index.INDEX_SIZE) {
            return null
        }
        raf.seek(sectorId.toLong() * Index.INDEX_SIZE)
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
        val data = ByteArray(sectorSize)
        while (read < sectorSize) {
            if (sectorPosition == 0) {
                return null
            }
            var requiredToRead = sectorSize - read
            if (requiredToRead > sectorDataSize) {
                requiredToRead = sectorDataSize
            }
            mainFile.seek(sectorPosition.toLong() * Index.SECTOR_SIZE)
            mainFile.read(buffer.array(), 0, requiredToRead + sectorHeaderSize)
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
            val bufferData = buffer.array()
            for (i in 0 until requiredToRead) {
                data[read++] = bufferData[i + sectorHeaderSize]
            }
            sectorPosition = sectorNextPosition
            chunk++
        }
        return data
    }

    private fun readValue(version: Int, buffer: BufferReader) = if (version >= 7) buffer.readBigSmart() else buffer.readUnsignedShort()

    data class File(val id: Int, var data: ByteArray? = null)

    companion object {
        private val logger = InlineLogger()

        @JvmStatic
        fun main(args: Array<String>) {
            val memory = InMemory()
            val path = "./data/cache/"
            val start = System.currentTimeMillis()
            val indices = memory.load(path)
            println("Loaded cache in ${System.currentTimeMillis() - start}ms")

            val lib = CacheLibrary(path)
            for (index in lib.indices()) {
                if (index.archives().size != indices.getValue(index.id).size) {
                    println("Different archive size")
                }
                for (archive in index.archives()) {
                    if (archive.files.size != indices.getValue(index.id).getValue(archive.id).size) {
                        println("Different archive size: ${index.id} ${archive.id} ${archive.files.size} != ${indices.getValue(index.id).getValue(archive.id).size}")
                    }
                    for (file in archive.files()) {
                        val expected = lib.data(index.id, archive.id, file.id)
                        val actual = indices.getValue(index.id).getValue(archive.id)[file.id]
                        if (!expected.contentEquals(actual)) {
                            println("Mismatch ${index.id} ${archive.id} ${file.id} ${expected?.take(10)} ${actual?.take(10)}")
                            exitProcess(0)
                        }
                    }
                }
            }

            /*
                BZIP2 1772 files 1.193s
                GZIP 95267 files 1.184s
                NONE 4201 files 0.011s
             */
        }
    }
}
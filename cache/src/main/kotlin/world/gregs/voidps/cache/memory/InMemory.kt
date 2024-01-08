package world.gregs.voidps.cache.memory

import com.displee.cache.CacheLibrary
import com.displee.cache.compress.type.BZIP2Compressor
import com.displee.cache.index.Index
import com.displee.cache.index.ReferenceTable
import com.displee.compress.CompressionType
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

    fun load(path: String): Map<Int, Map<Int, Archive>> {
        val main = File(path, "${CacheLibrary.CACHE_FILE_NAME}.dat2")
        val mainFile = if (main.exists()) {
            RandomAccessFile(main, "rw")
        } else {
            throw FileNotFoundException("Main file not found at '${main.absolutePath}'.")
        }
        val index255File = File(path, "${CacheLibrary.CACHE_FILE_NAME}.idx255")
        if (!index255File.exists()) {
            throw FileNotFoundException("Checksum file not found at '${index255File.absolutePath}'.")
        }
        val index255Raf = RandomAccessFile(index255File, "rw")
        logger.trace { "Reading indices..." }
        val indicesLength = index255Raf.length().toInt() / Index.INDEX_SIZE
        val output = mutableMapOf<Int, Map<Int, Archive>>()
        for (indexId in 0 until indicesLength) {
            val file = File(path, "${CacheLibrary.CACHE_FILE_NAME}.idx$indexId")
            if (!file.exists()) {
                logger.warn { "No index $indexId file found." }
                continue
            }
            try {
                val archives = TreeMap<Int, Archive>()
                val archiveSectorData = readArchiveSector(mainFile, index255Raf, 255, indexId)
                if (archiveSectorData != null) {
                    val decompressed = decompress(archiveSectorData)
                    val buffer = BufferReader(decompressed)
                    val version = buffer.readUnsignedByte()
                    if (version < 5 || version > 7) {
                        throw RuntimeException("Unknown version: $version")
                    }
                    val revision = if (version >= 6) buffer.readInt() else 0
                    val flags = buffer.readByte()
                    val named = flags and ReferenceTable.FLAG_NAME != 0
                    val hasWhirlPool = flags and ReferenceTable.FLAG_WHIRLPOOL != 0

                    val archiveIds = IntArray(readValue(version, buffer))
                    for (i in archiveIds.indices) {
                        val archiveId = readValue(version, buffer) + if (i == 0) 0 else archiveIds[i - 1]
                        archiveIds[i] = archiveId.also { archives[it] = Archive(it) }
                    }
                    val archiveArray = archives.values.toTypedArray()
                    if (named) {
                        buffer.skip(archiveArray.size * 4)
                    }
                    if (hasWhirlPool) {
                        buffer.skip(archiveArray.size * Index.WHIRLPOOL_SIZE)
                    }
                    buffer.skip(archiveArray.size * 4) // Crc
                    buffer.skip(archiveArray.size * 4) // Revision
                    var totalSize = 0
                    val archiveFileSizes = IntArray(archiveArray.size) {
                        val size = readValue(version, buffer)
                        totalSize += size
                        size
                    }
                    for (i in archiveArray.indices) {
                        val archive = archiveArray[i]
                        var fileId = 0
                        for (fileIndex in 0 until archiveFileSizes[i]) {
                            fileId += readValue(version, buffer)
                            archive.files[fileId] = File(fileId)
                        }
                    }
                    if (named) {
                        buffer.skip(totalSize * 4)
                    }
                    val raf = RandomAccessFile(File(path, "${CacheLibrary.CACHE_FILE_NAME}.idx${indexId}"), "r")
                    for ((archiveId, archive) in archives) {
                        val sector = readArchiveSector(mainFile, raf, indexId, archiveId)
                        if (sector == null) {
                            archive.files.clear()
                            continue
                        } else {
                            val decompressed = decompress(sector, null)
                            val files: Map<Int, File>? = if (decompressed.isNotEmpty()) {
                                val buffer = BufferReader(decompressed)
                                val files = archive.files
                                val rawArray = buffer.array()
                                if (files.size == 1) {
                                    files.values.first().data = rawArray
                                    null
                                } else {
                                    val fileIds = files.keys.toIntArray()
                                    var fileDataSizesOffset = rawArray.size
                                    val chunkSize: Int = rawArray[--fileDataSizesOffset].toInt() and 0xFF
                                    fileDataSizesOffset -= chunkSize * (fileIds.size * 4)
                                    val fileDataSizes = IntArray(fileIds.size)
                                    buffer.position(fileDataSizesOffset)
                                    for (i in 0 until chunkSize) {
                                        var offset = 0
                                        for (fileIndex in fileIds.indices) {
                                            offset += buffer.readInt()
                                            fileDataSizes[fileIndex] += offset
                                        }
                                    }
                                    val filesData = arrayOfNulls<ByteArray>(fileIds.size)
                                    for (i in fileIds.indices) {
                                        filesData[i] = ByteArray(fileDataSizes[i])
                                        fileDataSizes[i] = 0
                                    }
                                    buffer.position(fileDataSizesOffset)
                                    var offset = 0
                                    for (i in 0 until chunkSize) {
                                        var read = 0
                                        for (j in fileIds.indices) {
                                            read += buffer.readInt()
                                            System.arraycopy(rawArray, offset, filesData[j], fileDataSizes[j], read)
                                            offset += read
                                            fileDataSizes[j] += read
                                        }
                                    }
                                    for (i in fileIds.indices) {
                                        files[fileIds[i]]?.data = filesData[i]
                                    }
                                    files
                                }
                            } else null
                            val sectorBuffer = BufferReader(sector)
                            sectorBuffer.position(1)
                            val remaining: Int = sector.size - (sectorBuffer.readInt() + sectorBuffer.position())
                            if (remaining >= 2) {
                                sectorBuffer.position(sector.size - 2)
                                archive.revision = sectorBuffer.readUnsignedShort()
                            }
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
        val compressionType = CompressionType.compressionTypes[type]
        val compressedSize = buffer.readInt() and 0xFFFFFF
        var decompressedSize = 0
        if (compressionType != CompressionType.NONE) {
            decompressedSize = buffer.readInt() and 0xFFFFFF
        }
        var decompressed = ByteArray(decompressedSize)
        when (compressionType) {
            CompressionType.NONE -> decompressed = ByteArray(compressedSize).apply { buffer.readBytes(this) }
            CompressionType.BZIP2 -> BZIP2Compressor.decompress(decompressed, decompressed.size, data, compressedSize, 9)
            CompressionType.GZIP -> if (!inflate(buffer, decompressed)) return byteArrayOf()
            CompressionType.LZMA -> {
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
            inflater.setInput(bytes, offset + 10, bytes.size - (10 + offset + 8))
            inflater.inflate(data)
        } catch (exception: Exception) {
            inflater.reset()
            return false
        }
        inflater.reset()
        return true
    }

    fun readArchiveSector(mainFile: RandomAccessFile, raf: RandomAccessFile, index: Int, id: Int): ByteArray? {
        if (mainFile.length() < Index.INDEX_SIZE * id + Index.INDEX_SIZE) {
            return null
        }
        val sectorData = ByteArray(Index.SECTOR_SIZE)// 0, 106, -100, 0, 0, 1
        raf.seek(id.toLong() * Index.INDEX_SIZE)
        raf.read(sectorData, 0, Index.INDEX_SIZE)
        val bigSector = id > 65535
        val buffer = BufferReader(sectorData)
        val sectorSize = buffer.readUnsignedMedium() // 27282
        var sectorPosition = buffer.readUnsignedMedium() // 1
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
            val sectorId = if (bigSector) {
                buffer.readInt()
            } else {
                buffer.readUnsignedShort()
            }
            val sectorChunk = buffer.readUnsignedShort()
            val sectorNextPosition = buffer.readUnsignedMedium()
            val sectorIndex = buffer.readUnsignedByte()
            if (index != sectorIndex || id != sectorId || chunk != sectorChunk) {
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

    class Archive(val id: Int) {
        var whirlpool: ByteArray? = null
        var revision = 0
        val files: SortedMap<Int, File> = TreeMap()
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
                    if (archive.files.size != indices.getValue(index.id).getValue(archive.id).files.size) {
                        println("Different archive size")
                    }
                    for (file in archive.files()) {
                        val expected = lib.data(index.id, archive.id, file.id)
                        val actual = indices.getValue(index.id).getValue(archive.id).files.getValue(file.id)
                        if (!expected.contentEquals(actual.data)) {
                            println("Mismatch ${index.id} ${archive.id} ${file.id} ${actual.id} ${expected?.take(10)} ${actual.data?.take(10)}")
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
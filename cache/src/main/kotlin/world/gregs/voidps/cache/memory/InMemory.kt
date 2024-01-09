package world.gregs.voidps.cache.memory

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index
import com.displee.cache.index.ReferenceTable
import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import lzma.sdk.lzma.Decoder
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.secure.Xtea
import java.io.*
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.system.exitProcess

class InMemory {

    private val archiveIds = IntArray(64_000)
    private val fileIds = IntArray(3_000)
    private val compressor = BZIP2Compressor()
    private var decompressed: ByteArray = ByteArray(900_000)
    private var decompressed2: ByteArray = ByteArray(4_702_000)
    private var output: ByteArray = ByteArray(665_000)
    private val sectorData = ByteArray(Index.SECTOR_SIZE)

    fun load(path: String, xteas: Map<Int, IntArray>?): Array<Array<Array<ByteArray?>?>?> {
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
        return Array(indicesLength) { indexId ->
            val file = File(path, "${CacheLibrary.CACHE_FILE_NAME}.idx$indexId")
            if (!file.exists()) {
                logger.warn { "No index $indexId file found." }
                return@Array null
            }
            try {
//                val archives = output[indexId]
                val archiveSectorSize = readArchiveSector(mainFile, mainFileLength, index255Raf, 255, indexId)
                if (archiveSectorSize == 0) {
                    logger.debug { "Loaded index $indexId. 0" }
                    return@Array null
                }
                val decompressedSize = decompress(this.output, decompressed = decompressed, size = archiveSectorSize)
                val tableBuffer = BufferReader(decompressed)
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
                var highest = 0
                for (i in 0 until archiveCount) {
                    val archiveId = readValue(version, tableBuffer) + previous
                    previous = archiveId
                    archiveIds[i] = archiveId
                    if (archiveId > highest) {
                        highest = archiveId
                    }
                }
                val archives = Array<Array<ByteArray?>?>(highest + 1) { null }
                if (named) {
                    tableBuffer.skip(archiveCount * 4)
                }
                if (hasWhirlPool) {
                    tableBuffer.skip(archiveCount * Index.WHIRLPOOL_SIZE)
                }
                tableBuffer.skip(archiveCount * 8) // Crc & revisions
                val archiveFileSizes = IntArray(archiveCount) { readValue(version, tableBuffer) }
                val raf = RandomAccessFile(file, "r")
                for (archiveIndex in 0 until archiveCount) {
                    val archiveId = archiveIds[archiveIndex]
                    val fileCount = archiveFileSizes[archiveIndex]
                    val sectorSize = readArchiveSector(mainFile, mainFileLength, raf, indexId, archiveId)
                    if(sectorSize == 0) {
                        continue
                    }
                    var fileId = 0
                    for (fileIndex in 0 until fileCount) {
                        fileId += readValue(version, tableBuffer)
                        fileIds[fileIndex] = fileId
                    }
                    val archiveFiles: Array<ByteArray?> = Array(fileId + 1) { null }
                    archives[archiveId] = archiveFiles
                    val keys = if(indexId == 5) xteas?.get(archiveId) else null
                    val indexDecompressedSize = decompress(this.output, keys, decompressed2, sectorSize)
                    if (indexDecompressedSize == 0) {
                        // TODO we don't really need this
                        if (fileCount == 1) {
                            archiveFiles[fileId] = null
                        } else {
                            for (fileId in 0 until fileCount) {
                                archiveFiles[fileId] = null
                            }
                        }
                        continue
                    }
                    if (fileCount == 1) {
                        archiveFiles[fileId] = decompressed2.copyOf(indexDecompressedSize)
                        continue
                    }
                    val indexBuffer = BufferReader(decompressed2)
                    val rawArray = indexBuffer.array()
                    var fileDataSizesOffset = indexDecompressedSize
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
                logger.debug { "Loaded index $indexId. ${archives.size}" }
                archives
            } catch (e: Exception) {
                logger.warn(e) { "Failed to load index $indexId." }
                Array(0) { null }
            }
        }
    }

    val deflater = Deflater(Deflater.DEFAULT_COMPRESSION, true)

    fun decompress(data: ByteArray, keys: IntArray? = null, decompressed: ByteArray, size: Int): Int {
        if (keys != null && (keys[0] != 0 || keys[1] != 0 || keys[2] != 0 || 0 != keys[3])) {
            Xtea.decipher(data, keys, 5, size)
        }
        val buffer = BufferReader(data)
        val type = buffer.readUnsignedByte()
        val compressedSize = buffer.readInt() and 0xFFFFFF
        var decompressedSize = 0
        if (type != 0) {
            decompressedSize = buffer.readInt() and 0xFFFFFF
        }

        // TODO Replace with backing array and remove byte array copies
        when (type) {
            0 -> {
                buffer.readBytes(decompressed, 0, compressedSize)
                return compressedSize
            }
            1 -> compressor.decompress(decompressed, decompressedSize, data, 9)
            2 -> {
                val offset = buffer.position()
                if (buffer.readByte() != 31 || buffer.readByte() != -117) {
                    return 0
                }
                try {
                    inflater.setInput(data, offset + 10, data.size - (offset + 18))
                    inflater.inflate(decompressed)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    return 0
                } finally {
                    inflater.reset()
                }
            }
            3 -> decompress(data, buffer.position(), decompressed, decompressedSize)
        }
        return decompressedSize
    }

    private val decoder = Decoder()

    private fun decompress(compressed: ByteArray, offset: Int, decompressed: ByteArray, decompressedLength: Int) {
        if (!decoder.setDecoderProperties(compressed)) {
            logger.error { "LZMA: Bad properties." }
            return
        }
        val input = ByteArrayInputStream(compressed)
        input.skip(offset.toLong())
        val output = ByteArrayWrapperOutputStream(decompressed)
        decoder.code(input, output, decompressedLength.toLong())
    }

    class ByteArrayWrapperOutputStream(private val byteArray: ByteArray) : OutputStream() {
        private var position = 0

        override fun write(b: Int) {
            byteArray[position++] = b.toByte()
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            System.arraycopy(b, off, byteArray, position, len)
            position += len
        }

        override fun flush() {
        }

        override fun close() {
        }
    }

    private val inflater = Inflater(true)

    fun inflate(buffer: BufferReader, data: ByteArray): Boolean {
        val bytes = buffer.array()
        val offset = buffer.position()
        if (bytes[offset].toInt() != 31 || bytes[offset + 1].toInt() != -117) {
            return false
        }
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

    fun readArchiveSector(mainFile: RandomAccessFile, length: Long, raf: RandomAccessFile, indexId: Int, sectorId: Int): Int {
        if (length < Index.INDEX_SIZE * sectorId + Index.INDEX_SIZE) {
            return 0
        }
        raf.seek(sectorId.toLong() * Index.INDEX_SIZE)
        raf.read(sectorData, 0, Index.INDEX_SIZE)
        val bigSector = sectorId > 65535
        val buffer = BufferReader(sectorData)
        val sectorSize = buffer.readUnsignedMedium()
        var sectorPosition = buffer.readUnsignedMedium()
        if (sectorSize < 0 || sectorPosition <= 0 || sectorPosition > mainFile.length() / Index.SECTOR_SIZE) {
            return 0
        }
        var read = 0
        var chunk = 0
        val sectorHeaderSize = if (bigSector) Index.SECTOR_HEADER_SIZE_BIG else Index.SECTOR_HEADER_SIZE_SMALL
        val sectorDataSize = if (bigSector) Index.SECTOR_DATA_SIZE_BIG else Index.SECTOR_DATA_SIZE_SMALL
        while (read < sectorSize) {
            if (sectorPosition == 0) {
                return 0
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
                return 0
            } else if (sectorNextPosition < 0 || sectorNextPosition > mainFile.length() / Index.SECTOR_SIZE) {
                return 0
            }
            val bufferData = buffer.array()
            for (i in 0 until requiredToRead) {
                output[read++] = bufferData[i + sectorHeaderSize]
            }
            sectorPosition = sectorNextPosition
            chunk++
        }
        return read
    }

    private fun readValue(version: Int, buffer: BufferReader) = if (version >= 7) buffer.readBigSmart() else buffer.readUnsignedShort()

    companion object {
        private val logger = InlineLogger()
        fun loadBinary(file: File): Map<Int, IntArray> {
            val xteas = Int2ObjectOpenHashMap<IntArray>()
            val reader = BufferReader(file.readBytes())
            while (reader.remaining > 0) {
                val region = reader.readShort()
                xteas[region] = IntArray(4) { reader.readInt() }
            }
            return xteas
        }
        @JvmStatic
        fun main(args: Array<String>) {
            val memory = InMemory()
            val path = "./data/cache/"
            val xteas = loadBinary(File("./data/xteas.dat"))
            val start = System.currentTimeMillis()
            val indices = memory.load(path, xteas)
            // TODO check data see if there's enough gaps to justify using Maps instead of arrays
            // TODO memory usage of storing inflated vs deflated
            println("Loaded cache in ${System.currentTimeMillis() - start}ms")

            Thread.sleep(10000)
            println(indices.size)
            exitProcess(0)
            val lib = CacheLibrary(path)
            for (index in lib.indices()) {
                if (index.archives().size != indices.get(index.id)?.size) {
                    println("Different archive size")
                }
                for (archive in index.archives()) {
                    if (archive.files.size != indices.get(index.id)?.get(archive.id)?.size) {
                        println("Different archive size: ${index.id} ${archive.id} ${archive.files.size} != ${indices.get(index.id)?.get(archive.id)?.size}")
                    }
                    for (file in archive.files()) {
                        val expected = lib.data(index.id, archive.id, file.id)
                        val actual = indices.get(index.id)?.get(archive.id)?.get(file.id)
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
package world.gregs.voidps.cache

import com.displee.cache.index.Index
import world.gregs.voidps.buffer.read.BufferReader
import java.io.RandomAccessFile

object CacheReader {
//    val cache = CacheLibrary("./data/cache/")

    @JvmStatic
    fun main(args: Array<String>) {
//        println(expected?.data?.size)

        val start = System.currentTimeMillis()
        crc(Indices.OBJECTS)
        crc(Indices.INTERFACES)
        crc(Indices.ANIMATIONS)
        crc(Indices.ENUMS)
        crc(Indices.GRAPHICS)
        crc(Indices.ITEMS)
        crc(Indices.NPCS)
        crc(Indices.QUICK_CHAT_MESSAGES)
        crc(Indices.QUICK_CHAT_MENUS)
        crc(Indices.HUFFMAN)
        crc(Indices.CLIENT_SCRIPTS)
        println("Took ${System.currentTimeMillis() - start}ms")
    }

    fun crc(index: Int): Int {
        return generateCrc(readSector(index)!!)
    }

    private val CRC_TABLE = IntArray(256) {
        var crc = it
        for (i in 0..7) {
            crc = if (crc and 0x1 == 1) {
                crc ushr 1 xor 0x12477cdf.inv()
            } else {
                crc ushr 1
            }
        }
        crc
    }

    fun generateCrc(data: ByteArray, offset: Int = 0, length: Int = data.size): Int {
        var crc = -1
        for (i in offset until length) {
            crc = crc ushr 8 xor CRC_TABLE[crc xor data[i].toInt() and 0xff]
        }
        crc = crc xor -0x1
        return crc
    }

    private fun readSector(id: Int): ByteArray? {
        val mainFile = RandomAccessFile("./data/cache/main_file_cache.dat2", "r")
        val raf = RandomAccessFile("./data/cache/main_file_cache.idx255", "r")
        if (mainFile.length() < Index.INDEX_SIZE * id + Index.INDEX_SIZE) {
            return null
        }
        val sectorData = ByteArray(Index.SECTOR_SIZE)
        raf.seek(id.toLong() * Index.INDEX_SIZE)
        raf.read(sectorData, 0, Index.INDEX_SIZE)
        val bigSector = id > 65535
        val buffer = BufferReader(sectorData)
        val size = buffer.readUnsignedMedium()
        var position = buffer.readUnsignedMedium()
        if (size < 0 || position <= 0 || position > mainFile.length() / Index.SECTOR_SIZE) {
            return null
        }
        val data = ByteArray(size)
        var read = 0
        var chunkCount = 0
        val sectorHeaderSize = if (bigSector) Index.SECTOR_HEADER_SIZE_BIG else Index.SECTOR_HEADER_SIZE_SMALL
        val sectorDataSize = if (bigSector) Index.SECTOR_DATA_SIZE_BIG else Index.SECTOR_DATA_SIZE_SMALL
        while (read < size) {
            if (position == 0) {
                return null
            }
            var requiredToRead = size - read
            if (requiredToRead > sectorDataSize) {
                requiredToRead = sectorDataSize
            }
            mainFile.seek(position.toLong() * Index.SECTOR_SIZE)
            mainFile.read(buffer.array(), 0, requiredToRead + sectorHeaderSize)
            buffer.position(0)
            val sectorId = if (bigSector) {
                buffer.readInt()
            } else {
                buffer.readUnsignedShort()
            }
            val chunk = buffer.readUnsignedShort()
            val nextPosition = buffer.readUnsignedMedium()
            val index = buffer.readUnsignedByte()
            if (index != 255 || id != sectorId || chunkCount != chunk) {
                return null
            } else if (nextPosition < 0 || nextPosition > mainFile.length() / Index.SECTOR_SIZE) {
                return null
            }
            val bufferData = buffer.array()
            for (i in 0 until requiredToRead) {
                data[read++] = bufferData[i + sectorHeaderSize]
            }
            position = nextPosition
            chunkCount++
        }
        return data
    }
}
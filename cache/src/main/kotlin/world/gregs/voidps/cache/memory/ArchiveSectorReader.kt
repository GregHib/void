package world.gregs.voidps.cache.memory

import com.displee.cache.index.Index
import world.gregs.voidps.buffer.read.BufferReader
import java.io.RandomAccessFile

class ArchiveSectorReader(maximumSize: Int = 665_000) {
    val data: ByteArray = ByteArray(maximumSize)
    private val sectorData = ByteArray(520)

    fun read(mainFile: RandomAccessFile, length: Long, raf: RandomAccessFile, indexId: Int, sectorId: Int): Int {
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
            // TODO system copy
            for (i in 0 until requiredToRead) {
                data[read++] = bufferData[i + sectorHeaderSize]
            }
            sectorPosition = sectorNextPosition
            chunk++
        }
        return read
    }

}
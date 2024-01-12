package world.gregs.voidps.cache.memory.load

import com.displee.cache.index.Index
import world.gregs.voidps.buffer.read.BufferReader
import java.io.RandomAccessFile

object Archive {
    const val INDEX_SIZE = 6
    const val CACHE_FILE_NAME = "main_file_cache"

    fun readArchiveSector(mainFile: RandomAccessFile, length: Long, raf: RandomAccessFile, indexId: Int, sectorId: Int): ByteArray? {
        if (length < Index.INDEX_SIZE * sectorId + Index.INDEX_SIZE) {
            return null
        }
        raf.seek(sectorId.toLong() * Index.INDEX_SIZE)
        val sectorData = ByteArray(Index.SECTOR_SIZE)
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
        val output = ByteArray(sectorSize)
        while (read < sectorSize) {
            if (sectorPosition == 0) {
                return null
            }
            var requiredToRead = sectorSize - read
            if (requiredToRead > sectorDataSize) {
                requiredToRead = sectorDataSize
            }
            mainFile.seek(sectorPosition.toLong() * Index.SECTOR_SIZE)
            mainFile.read(sectorData, 0, requiredToRead + sectorHeaderSize)
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
            System.arraycopy(sectorData, sectorHeaderSize, output, read, requiredToRead)
            read += requiredToRead
            sectorPosition = sectorNextPosition
            chunk++
        }
        return output
    }
}
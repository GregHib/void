package world.gregs.voidps.js5

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import world.gregs.voidps.cache.Cache
import kotlin.math.min

class FileServer(
    private val cache: Cache,
    private val versionTable: ByteArray
) {
    private val logger = InlineLogger()

    /**
     * Fulfills a request by sending the requested files data to the requester
     */
    suspend fun fulfill(read: ByteReadChannel, write: ByteWriteChannel, prefetch: Boolean) {
        val value = read.readUMedium()
        val index = value shr 16
        val archive = value and 0xffff
        val data = if (index == 255 && archive == 255) versionTable else cache.sector(index, archive)
        if (data == null || data.size < 4) {
            return logger.warn { "Unable to fulfill request $index $archive $prefetch." }
        }
        serve(write, index, archive, data, prefetch)
    }

    /**
     * Writes response header followed by the contents of [data] to [write]
     */
    suspend fun serve(write: ByteWriteChannel, index: Int, archive: Int, data: ByteArray, prefetch: Boolean) {
        val compression = data[0].toInt()
        val size = getInt(data[1], data[2], data[3], data[4]) + if (compression != 0) 8 else 4
        logger.trace { "Serving file $index $archive - $size." }
        write.writeByte(index)
        write.writeShort(archive)
        write.writeByte(if (prefetch) compression or 0x80 else compression)
        serve(write, HEADER, data, OFFSET, size, SPLIT)
    }

    /**
     * Writes [source] [offset] [size] to [write] and starting at [headerSize] inserting a [SEPARATOR] every [split] bytes
     */
    suspend fun serve(write: ByteWriteChannel, headerSize: Int, source: ByteArray, offset: Int, size: Int, split: Int) {
        var length = min(size, split - headerSize)
        write.writeFully(source, offset, length)
        var written = length
        while (written < size) {
            write.writeByte(SEPARATOR)

            length = if (size - written < split) size - written else split - 1
            write.writeFully(source, written + offset, length)
            written += length
        }
    }

    companion object {

        private fun getInt(b1: Byte, b2: Byte, b3: Byte, b4: Byte) = b1.toInt() shl 24 or (b2.toInt() and 0xff shl 16) or (b3.toInt() and 0xff shl 8) or (b4.toInt() and 0xff)

        private const val SEPARATOR = 255
        private const val HEADER = 4
        private const val SPLIT = 512
        private const val OFFSET = 1
    }
}
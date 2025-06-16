package world.gregs.voidps.network.file

import com.github.michaelbull.logging.InlineLogger
import io.ktor.utils.io.*
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.network.file.type.CacheFileProvider
import world.gregs.voidps.network.file.type.MemoryFileProvider
import java.util.*
import kotlin.math.min

/**
 * Provides cache sectors requested by the client
 */
interface FileProvider {

    fun data(index: Int, archive: Int): ByteArray?

    suspend fun encode(write: ByteWriteChannel, data: ByteArray)

    suspend fun serve(write: ByteWriteChannel, request: Int, prefetch: Boolean): Boolean {
        val index = request shr 16
        val archive = request and 0xffff
        val data = data(index, archive)

        if (data == null || data.size < 4) {
            logger.warn { "Unable to fulfill request $index $archive $prefetch." }
            return false
        }
        val compression = data[0].toInt()
        write.writeByte(index)
        write.writeShort(archive)
        write.writeByte(if (prefetch) compression or 0x80 else compression)
        encode(write, data)
        write.flush()
        return true
    }

    companion object {
        private val logger = InlineLogger("FileProvider")

        fun load(cache: Cache, properties: Properties): FileProvider {
            val start = System.currentTimeMillis()
            val live = properties.getProperty("server.live").toBoolean()
            val provider = if (live) MemoryFileProvider(cache) else CacheFileProvider(cache)
            logger.info { "Loaded file provider in ${System.currentTimeMillis() - start}ms" }
            return provider
        }

        internal fun getInt(b1: Byte, b2: Byte, b3: Byte, b4: Byte) = b1.toInt() shl 24 or (b2.toInt() and 0xff shl 16) or (b3.toInt() and 0xff shl 8) or (b4.toInt() and 0xff)

        internal const val SEPARATOR = 255
        private const val HEADER = 4
        internal const val SPLIT = 512
        internal const val LARGEST_BLOCK = SPLIT - HEADER
        internal const val OFFSET = 1

        internal fun encode(data: ByteArray): ByteArray {
            val compression = data[0].toInt()
            val size = getInt(data[1], data[2], data[3], data[4]) + if (compression != 0) 8 else 4
            val write = BufferWriter(data.size + (size / SPLIT) + 1)
            write.writeByte(compression)
            var length = min(size, LARGEST_BLOCK)
            write.writeBytes(data, OFFSET, length)
            var written = length
            while (written < size) {
                write.writeByte(SEPARATOR)
                length = if (size - written < SPLIT) size - written else SPLIT - 1
                write.writeBytes(data, written + OFFSET, length)
                written += length
            }
            return write.toArray()
        }
    }
}
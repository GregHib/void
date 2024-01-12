package world.gregs.voidps.cache.memory.load

import com.github.michaelbull.logging.InlineLogger
import lzma.sdk.lzma.Decoder
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.memory.BZIP2Compressor
import world.gregs.voidps.cache.secure.Xtea
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.Inflater


class ThreadContext {
    val inflater = Inflater(true)

    fun decompress(context: ThreadContext, data: ByteArray, keys: IntArray? = null): ByteArray? {
        if (keys != null && (keys[0] != 0 || keys[1] != 0 || keys[2] != 0 || 0 != keys[3])) {
            Xtea.decipher(data, keys, 5)
        }
        val buffer = BufferReader(data)
        val type = buffer.readUnsignedByte()
        val compressedSize = buffer.readInt() and 0xFFFFFF
        var decompressedSize = 0
        if (type != 0) {
            decompressedSize = buffer.readInt() and 0xFFFFFF
        }
        when (type) {
            0 -> {
                val decompressed = ByteArray(compressedSize)
                buffer.readBytes(decompressed, 0, compressedSize)
                return decompressed
            }
            1 -> {
                // Deprecated
                if (!warned.get()) {
                    logger.warn { "GZIP2 Compression found - replace to improve read performance." }
                    warned.set(true)
                }
                val decompressed = ByteArray(decompressedSize)
                context.compressor.decompress(decompressed, decompressedSize, data, 9)
                return decompressed
            }
            2 -> {
                val offset = buffer.position()
                if (buffer.readByte() != 31 || buffer.readByte() != -117) {
                    return null
                }
                return try {
                    val decompressed = ByteArray(decompressedSize)
                    context.inflater.setInput(data, offset + 10, data.size - (offset + 18))
                    context.inflater.finished()
                    context.inflater.inflate(decompressed)
                    decompressed
                } catch (exception: Exception) {
                    logger.warn(exception) { "Error decompressing gzip data." }
                    null
                } finally {
                    context.inflater.reset()
                }
            }
            3 -> {
                val decompressed = ByteArray(decompressedSize)
                context.decompress(data, buffer.position(), decompressed, decompressedSize)
                return decompressed
            }
        }
        return null
    }

    val compressor = BZIP2Compressor()

    private val decoder = Decoder()

    fun decompress(compressed: ByteArray, offset: Int, decompressed: ByteArray, decompressedLength: Int) {
        if (!decoder.setDecoderProperties(compressed)) {
            logger.error { "LZMA: Bad properties." }
            return
        }
        val input = ByteArrayInputStream(compressed)
        input.skip(offset.toLong())
        val output = ByteArrayWrapperOutputStream(decompressed)
        decoder.code(input, output, decompressedLength.toLong())
    }

    private class ByteArrayWrapperOutputStream(private val byteArray: ByteArray) : OutputStream() {
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

    companion object {
        private val warned = AtomicBoolean()
        val logger = InlineLogger()
    }
}
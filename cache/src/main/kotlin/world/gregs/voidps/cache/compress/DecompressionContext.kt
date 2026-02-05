package world.gregs.voidps.cache.compress

import com.github.michaelbull.logging.InlineLogger
import lzma.sdk.lzma.Decoder
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.cache.secure.Xtea
import java.io.ByteArrayInputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.Inflater

/**
 * Context per thread for decompressing data in parallel
 */
internal class DecompressionContext {
    private val gzipInflater = Inflater(true)
    @Suppress("DEPRECATION")
    private val bzip2Compressor: BZIP2Compressor by lazy { BZIP2Compressor() }
    private val lzmaDecoder: Decoder by lazy { Decoder() }

    fun decompress(data: ByteArray, keys: IntArray? = null): ByteArray? {
        if (keys != null && (keys[0] != 0 || keys[1] != 0 || keys[2] != 0 || 0 != keys[3])) {
            Xtea.decipher(data, keys, 5)
        }
        val buffer = ArrayReader(data)
        val type = buffer.readUnsignedByte()
        val compressedSize = buffer.readInt() and 0xFFFFFF
        var decompressedSize = 0
        if (type != 0) {
            decompressedSize = buffer.readInt() and 0xFFFFFF
        }
        when (type) {
            NONE -> {
                val decompressed = ByteArray(compressedSize)
                buffer.readBytes(decompressed, 0, compressedSize)
                return decompressed
            }
            BZIP2 -> {
                if (!warned.get()) {
                    logger.warn { "BZIP2 Compression found - replace to improve read performance." }
                    warned.set(true)
                }
                val decompressed = ByteArray(decompressedSize)
                @Suppress("DEPRECATION")
                bzip2Compressor.decompress(decompressed, decompressedSize, data, 9)
                return decompressed
            }
            GZIP -> {
                val offset = buffer.position()
                if (buffer.readByte() != 31 || buffer.readByte() != -117) {
                    return null
                }
                return try {
                    val decompressed = ByteArray(decompressedSize)
                    gzipInflater.setInput(data, offset + 10, data.size - (offset + 18))
                    gzipInflater.finished()
                    gzipInflater.inflate(decompressed)
                    decompressed
                } catch (exception: Exception) {
                    logger.warn(exception) { "Error decompressing gzip data." }
                    null
                } finally {
                    gzipInflater.reset()
                }
            }
            LZMA -> {
                val decompressed = ByteArray(decompressedSize)
                decompress(data, buffer.position(), decompressed, decompressedSize)
                return decompressed
            }
        }
        return null
    }

    private fun decompress(compressed: ByteArray, offset: Int, decompressed: ByteArray, decompressedLength: Int) {
        if (!lzmaDecoder.setDecoderProperties(compressed)) {
            logger.error { "LZMA: Bad properties." }
            return
        }
        val input = ByteArrayInputStream(compressed)
        input.skip(offset.toLong())
        val output = ByteArrayWrapperOutputStream(decompressed)
        lzmaDecoder.code(input, output, decompressedLength.toLong())
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
        private const val NONE = 0
        private const val BZIP2 = 1
        private const val GZIP = 2
        private const val LZMA = 3
        private val warned = AtomicBoolean()
        private val logger = InlineLogger()
    }
}

package world.gregs.voidps.cache.memory

import com.github.michaelbull.logging.InlineLogger
import lzma.sdk.lzma.Decoder
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.secure.Xtea
import java.io.ByteArrayInputStream
import java.util.zip.Inflater

class Decompressor(maximum: Int = 900_000) {

    val data: ByteArray = ByteArray(maximum)
    private val logger = InlineLogger()
    private val compressor = BZIP2Compressor()
    private val inflater = Inflater(true)

    fun decompress(source: ByteArray, size: Int, keys: IntArray? = null): Int {
        if (keys != null && (keys[0] != 0 || keys[1] != 0 || keys[2] != 0 || 0 != keys[3])) {
            Xtea.decipher(source, keys, 5, size)
        }
        val buffer = BufferReader(source)
        val type = buffer.readUnsignedByte()
        val compressedSize = buffer.readInt() and 0xFFFFFF
        var decompressedSize = 0
        if (type != 0) {
            decompressedSize = buffer.readInt() and 0xFFFFFF
        }

        // TODO Replace with backing array and remove byte array copies
        when (type) {
            0 -> {
                buffer.readBytes(data, 0, compressedSize)
                return compressedSize
            }
            1 -> compressor.decompress(data, decompressedSize, source, 9)
            2 -> {
                val offset = buffer.position()
                if (buffer.readByte() != 31 || buffer.readByte() != -117) {
                    return 0
                }
                try {
                    inflater.setInput(source, offset + 10, source.size - (offset + 18))
                    inflater.inflate(data)
                } catch (exception: Exception) {
                    logger.error(exception) { "Error inflating data." }
                    return 0
                } finally {
                    inflater.reset()
                }
            }
            3 -> decompress(source, buffer.position(), data, decompressedSize)
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
        val output = InMemory.ByteArrayWrapperOutputStream(decompressed)
        decoder.code(input, output, decompressedLength.toLong())
    }
}
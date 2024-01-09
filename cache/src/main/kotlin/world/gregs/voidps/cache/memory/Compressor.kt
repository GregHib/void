package world.gregs.voidps.cache.memory

import java.util.zip.Deflater

class Compressor(
    private val deflate: Boolean = true,
    maximum: Int
) {

    private val deflater = Deflater(Deflater.BEST_SPEED, true)
    private var data: ByteArray = ByteArray(maximum)

    fun compress(source: ByteArray, offset: Int = 0, length: Int = source.size): ByteArray {
        if (deflate) {
            deflater.setInput(source, offset, length)
            deflater.finish()
            val size = deflater.deflate(data)
            val output = data.copyOf(size)
            deflater.reset()
            return output
        }
        if (length != source.size) {
            return source.copyOf(length)
        }
        return source
    }

}
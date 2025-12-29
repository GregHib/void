package world.gregs.voidps.buffer.read

import java.nio.ByteBuffer

class BufferReader(
    val buffer: ByteBuffer,
) : Reader {

    constructor(array: ByteArray) : this(buffer = ByteBuffer.wrap(array))

    override val length: Int = buffer.remaining()
    override val remaining: Int
        get() = buffer.remaining()

    override fun peek(): Int {
        return buffer.get(buffer.position()).toInt()
    }

    private var bitIndex = 0

    override fun readByte(): Int = buffer.get().toInt()

    override fun readString(): String {
        if (buffer.hasArray()) {
            val array = buffer.array()
            val offset = buffer.arrayOffset() + position()
            val start = offset
            var pos = offset
            while (array[pos] != 0.toByte()) {
                pos++
            }
            val length = pos - start
            position(position() + length + 1)
            return String(array, start, length, Charsets.UTF_8)
        } else {
            // Fallback for direct buffers
            val start = position()
            var pos = start
            while (buffer.get(pos) != 0.toByte()) {
                pos++
            }

            val length = pos - start
            val bytes = ByteArray(length)
            buffer.position(start)
            buffer.get(bytes)
            buffer.position(pos + 1)

            return String(bytes, Charsets.UTF_8)
        }
    }

    override fun readBytes(value: ByteArray) {
        buffer.get(value)
    }

    override fun readBytes(value: ShortArray) {
        buffer
            .asShortBuffer()
            .get(value)
        buffer.position(buffer.position() + value.size * 2)
    }

    override fun readBytes(value: IntArray) {
        buffer
            .asIntBuffer()
            .get(value)
        buffer.position(buffer.position() + value.size * 4)
    }

    override fun readBytes(value: LongArray) {
        buffer
            .asLongBuffer()
            .get(value)
        buffer.position(buffer.position() + value.size * 8)
    }

    override fun readBytes(value: FloatArray) {
        buffer
            .asFloatBuffer()
            .get(value)
        buffer.position(buffer.position() + value.size * 4)
    }

    override fun readBytes(value: DoubleArray) {
        buffer
            .asDoubleBuffer()
            .get(value)
        buffer.position(buffer.position() + value.size * 8)
    }

    override fun readBytes(array: ByteArray, offset: Int, length: Int) {
        buffer.get(array, offset, length)
    }

    override fun skip(amount: Int) {
        buffer.position(buffer.position() + amount)
    }

    override fun position(): Int = buffer.position()

    override fun position(index: Int) {
        buffer.position(index)
    }

    override fun array(): ByteArray = buffer.array()

    override fun readableBytes(): Int = buffer.remaining()

    override fun startBitAccess(): Reader {
        bitIndex = buffer.position() * 8
        return this
    }

    override fun stopBitAccess(): Reader {
        buffer.position((bitIndex + 7) / 8)
        return this
    }

    @Suppress("NAME_SHADOWING")
    override fun readBits(bitCount: Int): Int {
        if (bitCount !in 0..32) {
            throw IllegalArgumentException("Number of bits must be between 1 and 32 inclusive")
        }

        var bitCount = bitCount
        var bytePos = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        var value = 0
        bitIndex += bitCount

        while (bitCount > bitOffset) {
            value += buffer.get(bytePos++).toInt() and BIT_MASKS[bitOffset] shl bitCount - bitOffset
            bitCount -= bitOffset
            bitOffset = 8
        }
        value += if (bitCount == bitOffset) {
            buffer.get(bytePos).toInt() and BIT_MASKS[bitOffset]
        } else {
            buffer.get(bytePos).toInt() shr bitOffset - bitCount and BIT_MASKS[bitCount]
        }
        return value
    }

    companion object {
        /**
         * Bit masks for [readBits]
         */
        private val BIT_MASKS = IntArray(32)

        init {
            for (i in BIT_MASKS.indices) {
                BIT_MASKS[i] = (1 shl i) - 1
            }
        }
    }
}

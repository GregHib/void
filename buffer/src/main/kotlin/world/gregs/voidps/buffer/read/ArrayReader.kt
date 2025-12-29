package world.gregs.voidps.buffer.read

import java.nio.ByteBuffer

class ArrayReader(
    array: ByteArray = ByteArray(0)
) : Reader {
    var array: ByteArray = array
        private set

    constructor(buffer: ByteBuffer) : this(buffer.array())

    override var length = array.size
    var position = 0
    override val remaining: Int
        get() = length - position
    private var bitIndex = 0

    fun set(array: ByteArray) {
        position = 0
        bitIndex = 0
        length = array.size
        this.array = array
    }

    override fun peek(): Int = array[position].toInt()

    override fun readByte(): Int = array[position++].toInt()

    override fun readString(): String {
        val start = position
        var pos = start
        while (array[pos] != 0.toByte()) {
            pos++
        }
        val length = pos - start
        position(position() + length + 1)
        return String(array, start, length, Charsets.UTF_8)
    }

    override fun readBytes(value: ByteArray) {
        System.arraycopy(array, position, value, 0, value.size)
        position += value.size
    }

    override fun readBytes(value: ShortArray) {
        ByteBuffer.wrap(array, position, value.size * 2)
            .asShortBuffer()
            .get(value)
        position += value.size * 2
    }

    override fun readBytes(value: IntArray) {
        ByteBuffer.wrap(array, position, value.size * 4)
            .asIntBuffer()
            .get(value)
        position += value.size * 4
    }

    override fun readBytes(value: LongArray) {
        ByteBuffer.wrap(array, position, value.size * 8)
            .asLongBuffer()
            .get(value)
        position += value.size * 8
    }

    override fun readBytes(value: FloatArray) {
        ByteBuffer.wrap(array, position, value.size * 4)
            .asFloatBuffer()
            .get(value)
        position += value.size * 4
    }

    override fun readBytes(value: DoubleArray) {
        ByteBuffer.wrap(array, position, value.size * 8)
            .asDoubleBuffer()
            .get(value)
        position += value.size * 8
    }

    override fun readBytes(array: ByteArray, offset: Int, length: Int) {
        System.arraycopy(this.array, position, array, offset, length)
        position += length
    }

    override fun skip(amount: Int) {
        position += amount
    }

    override fun position(): Int = position

    override fun position(index: Int) {
        position = index
    }

    override fun array(): ByteArray = array

    override fun readableBytes(): Int = remaining

    override fun startBitAccess(): Reader {
        bitIndex = position() * 8
        return this
    }

    override fun stopBitAccess(): Reader {
        position((bitIndex + 7) / 8)
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
            value += array[bytePos++].toInt() and BIT_MASKS[bitOffset] shl bitCount - bitOffset
            bitCount -= bitOffset
            bitOffset = 8
        }
        value += if (bitCount == bitOffset) {
           array[bytePos].toInt() and BIT_MASKS[bitOffset]
        } else {
           array[bytePos].toInt() shr bitOffset - bitCount and BIT_MASKS[bitCount]
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

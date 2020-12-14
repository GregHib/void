package rs.dusk.core.io.read

import rs.dusk.core.io.DataType
import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import java.nio.ByteBuffer
import kotlin.math.pow

open class BufferReader(override val buffer: ByteBuffer) : Reader {

    constructor(array: ByteArray) : this(buffer = ByteBuffer.wrap(array))

    override val length: Int = buffer.remaining()
    private var bitIndex = 0

    override fun readSmart(): Int {
        val peek = readUnsignedByte()
        return if (peek < 128) {
            peek and 0xFF
        } else {
            buffer.position(buffer.position() - 1)
            readUnsignedShort() - 32768
        }
    }

    override fun readBigSmart(): Int {
        val peek = buffer.get().toInt()
        buffer.position(buffer.position() - 1)
        return if (peek < -2) {
            readInt() and 0x7fffffff
        } else {
            val value = readShort()
            if (value == 32767) {
                -1
            } else {
                value
            }
        }
    }

    override fun readLargeSmart(): Int {
        var baseValue = 0
        var lastValue = readSmart()
        while (lastValue == 32767) {
            lastValue = readSmart()
            baseValue += 32767
        }
        return baseValue + lastValue
    }

    override fun readLong(): Long {
        val first = readInt().toLong() and 0xffffffffL
        val second = readInt().toLong() and 0xffffffffL
        return second + (first shl 32)
    }

    override fun readString(): String {
        val sb = StringBuilder()
        var b: Int
        while (buffer.hasRemaining()) {
            b = readByte()
            if (b == 0) {
                break
            }
            sb.append(b.toChar())
        }
        return sb.toString()
    }

    override fun readBytes(value: ByteArray) {
        buffer.get(value)
    }

    override fun readBytes(array: ByteArray, offset: Int, length: Int) {
        buffer.get(array, offset, length)
    }

    override fun skip(amount: Int) {
        buffer.position(buffer.position() + amount)
    }

    override fun readableBytes(): Int {
        return buffer.remaining()
    }

    override fun readSigned(type: DataType, modifier: Modifier, order: Endian): Long {
        var longValue = read(type, modifier, order)
        if (type != DataType.LONG) {
            val max = 2.0.pow(type.byteCount * 8.0 - 1).toInt()
            if (longValue > max - 1) {
                longValue -= max * 2L
            }
        }
        return longValue
    }

    override fun readUnsigned(type: DataType, modifier: Modifier, order: Endian): Long {
        if (type == DataType.LONG) {
            throw IllegalArgumentException("Longs must be signed")
        }
        val longValue = read(type, modifier, order)
        return longValue and -0x1L
    }

    /**
     * Reads [type] number of bytes with [modifier] and [order]
     * @param type The byte type to read
     * @param modifier The first byte read modifier
     * @param order The endianness
     * @return The read value
     */
    private fun read(type: DataType, modifier: Modifier, order: Endian): Long {
        check(buffer.remaining() >= type.byteCount) {
            "Not enough allocated buffer remaining $type."
        }

        if (order == Endian.MIDDLE) {
            check(modifier == Modifier.NONE || modifier == Modifier.INVERSE) {
                "Middle endian doesn't support variable modifier $modifier"
            }
            check(type == DataType.INT) {
                "Middle endian can only be used with an integer"
            }
        }

        var longValue: Long = 0
        var read: Int
        for (index in order.getRange(modifier, type.byteCount)) {
            read = buffer.get().toInt()
            read = when (if(index == 0 && order != Endian.MIDDLE) modifier else Modifier.NONE) {
                Modifier.ADD -> read - 128 and 0xff
                Modifier.INVERSE -> -read and 0xff
                Modifier.SUBTRACT -> 128 - read and 0xff
                else -> read and 0xff shl index * 8
            }
            longValue = longValue or read.toLong()
        }
        return longValue
    }

    override fun startBitAccess(): Reader {
        bitIndex = buffer.position() * 8
        return this
    }

    override fun finishBitAccess(): Reader {
        buffer.position((bitIndex + 7) / 8)
        return this
    }

    @Suppress("NAME_SHADOWING")
    override fun readBits(bitCount: Int): Int {
        if (bitCount < 0 || bitCount > 32) {
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
            for (i in BIT_MASKS.indices)
                BIT_MASKS[i] = (1 shl i) - 1
        }
    }
}
package world.gregs.voidps.buffer.read

import java.nio.ByteBuffer

class BufferReader(
    array: ByteArray
) : Reader {
    var array: ByteArray = array
        private set

    constructor(buffer: ByteBuffer) : this(buffer.array())

    override val length: Int
        get() = array.size
    var position = 0
    override val remaining: Int
        get() = length - position
    private var bitIndex = 0

    fun set(array: ByteArray) {
        position = 0
        bitIndex = 0
        this.array = array
    }

    override fun peek(): Int = array[position].toInt()

    override fun readByte(): Int = array[position++].toInt()

    override fun readByteAdd(): Int = (readByte() - 128).toByte().toInt()

    override fun readByteInverse(): Int = -readByte()

    override fun readByteSubtract(): Int = (readByteInverse() + 128).toByte().toInt()

    override fun readUnsignedByte(): Int = readByte() and 0xff

    override fun readShort(): Int = (readByte() shl 8) or readUnsignedByte()

    override fun readShortAdd(): Int = (readByte() shl 8) or readUnsignedByteAdd()

    override fun readUnsignedShortAdd(): Int = (readByte() shl 8) or ((readByte() - 128) and 0xff)

    override fun readShortLittle(): Int = readUnsignedByte() or (readByte() shl 8)

    override fun readShortAddLittle(): Int = readUnsignedByteAdd() or (readByte() shl 8)

    override fun readUnsignedByteAdd(): Int = (readByte() - 128).toByte().toInt()

    override fun readUnsignedShort(): Int = (readUnsignedByte() shl 8) or readUnsignedByte()

    override fun readUnsignedShortLittle(): Int = readUnsignedByte() or (readUnsignedByte() shl 8)

    override fun readMedium(): Int = (readByte() shl 16) or (readByte() shl 8) or readUnsignedByte()

    override fun readUnsignedMedium(): Int = (readUnsignedByte() shl 16) or (readUnsignedByte() shl 8) or readUnsignedByte()

    override fun readInt(): Int = (readUnsignedByte() shl 24) or (readUnsignedByte() shl 16) or (readUnsignedByte() shl 8) or readUnsignedByte()

    override fun readIntInverseMiddle(): Int = (readByte() shl 16) or (readByte() shl 24) or readUnsignedByte() or (readByte() shl 8)

    override fun readIntLittle(): Int = readUnsignedByte() or (readByte() shl 8) or (readByte() shl 16) or (readByte() shl 24)

    override fun readUnsignedIntMiddle(): Int = (readUnsignedByte() shl 8) or readUnsignedByte() or (readUnsignedByte() shl 24) or (readUnsignedByte() shl 16)

    override fun readSmart(): Int {
        val peek = readUnsignedByte()
        return if (peek < 128) {
            peek
        } else {
            (peek shl 8 or readUnsignedByte()) - 32768
        }
    }

    override fun readBigSmart(): Int {
        val peek = readByte()
        return if (peek < 0) {
            ((peek shl 24) or (readUnsignedByte() shl 16) or (readUnsignedByte() shl 8) or readUnsignedByte()) and 0x7fffffff
        } else {
            val value = (peek shl 8) or readUnsignedByte()
            if (value == 32767) -1 else value
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
        while (remaining > 0) {
            b = readUnsignedByte()
            if (b == 0) {
                break
            }
            sb.append(b.toChar())
        }
        return sb.toString()
    }

    override fun readBytes(value: ByteArray) {
        for (i in value.indices) {
            value[i] = readByte().toByte()
        }
    }

    override fun readBytes(array: ByteArray, offset: Int, length: Int) {
        for (i in 0 until length) {
            array[offset + i] = readByte().toByte()
        }
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
        if (bitCount < 0 || bitCount > 32) {
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

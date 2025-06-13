package world.gregs.voidps.buffer.read

import java.nio.ByteBuffer

class BufferReader(
    val buffer: ByteBuffer,
) : Reader {

    constructor(array: ByteArray) : this(buffer = ByteBuffer.wrap(array))

    override val length: Int = buffer.remaining()
    override val remaining: Int
        get() = buffer.remaining()
    private var bitIndex = 0

    override fun readByte(): Int = buffer.get().toInt()

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
        while (buffer.hasRemaining()) {
            b = readUnsignedByte()
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
            for (i in BIT_MASKS.indices) {
                BIT_MASKS[i] = (1 shl i) - 1
            }
        }
    }
}

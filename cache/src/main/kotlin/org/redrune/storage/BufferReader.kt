package org.redrune.storage

import org.redrune.core.network.model.packet.DataType
import org.redrune.core.network.model.packet.Endian
import org.redrune.core.network.model.packet.Modifier
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
            val max = 2.0.pow(type.length * 8.0 - 1).toInt()
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
        //Check bytes are available
        if (buffer.remaining() < type.length) {
            throw IndexOutOfBoundsException("Not enough allocated buffer remaining $type.")
        }

        var longValue: Long = 0
        when (order) {
            Endian.BIG, Endian.LITTLE -> {
                //For by length
                val range = if (order == Endian.LITTLE) 0 until type.length else type.length - 1 downTo 0
                var read: Long
                for (i in range) {
                    //If first and has a modifier
                    read = if (i == 0 && modifier != Modifier.NONE) {
                        //Read with variable modifier transform
                        when (modifier) {
                            Modifier.ADD -> buffer.get() - 128
                            Modifier.INVERSE -> -buffer.get()
                            Modifier.SUBTRACT -> 128 - buffer.get()
                            else -> throw IllegalArgumentException("Unknown byte modifier")
                        } and 0xFF
                    } else {
                        //Read with position shift
                        buffer.get().toInt() and 0xFF shl i * 8
                    }.toLong()
                    longValue = longValue or read
                }
            }
            Endian.MIDDLE -> {
                if (type != DataType.INT) {
                    throw IllegalArgumentException("Middle endian can only be used with an integer")
                }

                if (modifier != Modifier.NONE && modifier != Modifier.INVERSE) {
                    throw IllegalArgumentException("Middle endian doesn't support variable modifier $modifier")
                }

                //Reverse range if inverse modifier
                for (i in if (modifier == Modifier.NONE) middleEndianRange else middleEndianRange.reversed()) {
                    longValue = longValue or (buffer.get().toInt() and 0xFF shl i).toLong()
                }
            }
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
        private val middleEndianRange = listOf(8, 0, 24, 16)

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
package org.redrune.network.packet.access

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.redrune.tools.func.getHexContents
import org.redrune.network.packet.DataType
import org.redrune.network.packet.Endian
import org.redrune.network.packet.Modifier
import org.redrune.network.packet.PacketType

/**
 * @author Greg Hibb
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class PacketReader(val opcode: Int, private val type: PacketType?, private val buffer: ByteBuf) {

    constructor(byteArray: ByteArray) : this(-1, PacketType.RAW, Unpooled.copiedBuffer(byteArray))

    private var bitIndex = 0

    /**
     * Reads a boolean.
     * @param type The variable read type
     * @return [Boolean]
     */
    fun readBoolean(type: Modifier = Modifier.NONE): Boolean {
        return readByte(type) == 1
    }

    /**
     * Reads a boolean.
     * @return [Boolean]
     */
    fun readUnsignedBoolean(): Boolean {
        return readUnsignedByte() == 1
    }

    /**
     * Reads a byte.
     * @param type The variable read type
     * @return [Byte]
     */
    fun readByte(type: Modifier = Modifier.NONE): Int {
        return readSigned(DataType.BYTE, type).toInt()
    }

    /**
     * Reads an unsigned byte.
     * @return [Short]
     */
    fun readUnsignedByte(): Int {
        return readUnsigned(DataType.BYTE).toInt()
    }

    /**
     * Reads a short.
     * @param type The variable read type
     * @param order The read order
     * @return [Short]
     */
    fun readShort(type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Int {
        return readSigned(DataType.SHORT, type, order).toInt()
    }

    /**
     * Reads an unsigned short.
     * @return [Int]
     */
    fun readUnsignedShort(): Int {
        return readUnsigned(DataType.SHORT).toInt()
    }

    /**
     * Reads a 3-byte integer.
     * @return [Int]
     */
    fun readMedium(): Int {
        return readSigned(DataType.MEDIUM).toInt()
    }

    /**
     * Reads a integer.
     * @param type The variable read type
     * @param order The read order
     * @return [Int]
     */
    fun readInt(type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Int {
        return readSigned(DataType.INT, type, order).toInt()
    }

    fun readSmart(): Int {
        val peek = readUnsignedByte()
        return if (peek < 128) {
            peek and 0xFF
        } else {
            buffer.readerIndex(buffer.readerIndex() - 1)
            readUnsignedShort() - 32768
        }
    }

    fun readBigSmart(): Int {
        val peek = buffer.getByte(buffer.readerIndex()).toInt()
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

    fun readLargeSmart(): Int {
        var baseValue = 0
        var lastValue = readSmart()
        while (lastValue == 32767) {
            lastValue = readSmart()
            baseValue += 32767
        }
        return baseValue + lastValue
    }

    fun readLong(): Long {
        val first = readInt().toLong() and 0xffffffffL
        val second = readInt().toLong() and 0xffffffffL
        return second + (first shl 32)
    }

    fun readString(): String {
        val sb = StringBuilder()
        var b: Int
        while (buffer.isReadable) {
            b = readByte()
            if (b == 0) {
                break
            }
            sb.append(b.toChar())
        }
        return sb.toString()
    }

    fun readBytes(value: ByteArray) {
        buffer.readBytes(value)
    }

    fun readBytes(array: ByteArray, offset: Int, length: Int) {
        buffer.readBytes(array, offset, length)
    }

    fun release() {
        buffer.release()
    }

    fun retain() {
        buffer.retain()
    }

    fun skip(amount: Int) {
        buffer.skipBytes(amount)
    }

    fun readableBytes(): Int {
        return buffer.readableBytes()
    }

    fun resetReader() {
        buffer.resetReaderIndex()
    }

    fun resetWriter() {
        buffer.resetWriterIndex()
    }

    fun markReader() {
        buffer.markReaderIndex()
    }

    fun markWriter() {
        buffer.markWriterIndex()
    }

    fun reader(): Int {
        return buffer.readerIndex()
    }

    fun writer(): Int {
        return buffer.writerIndex()
    }

    fun readSigned(type: DataType, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Long {
        var longValue = read(type, modifier, order)
        if (type != DataType.LONG) {
            val max = Math.pow(2.0, type.length * 8.0 - 1).toInt()
            if (longValue > max - 1) {
                longValue -= max * 2L
            }
        }
        return longValue
    }

    fun readUnsigned(type: DataType, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Long {
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
        if (!buffer.isReadable(type.length)) {
            throw IndexOutOfBoundsException("Not enough allocated buffer remaining $type, remaining=${buffer.readableBytes()}.")
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
                            Modifier.ADD -> buffer.readByte() - 128
                            Modifier.INVERSE -> -buffer.readByte()
                            Modifier.SUBTRACT -> 128 - buffer.readByte()
                            else -> throw IllegalArgumentException("Unknown byte modifier")
                        } and 0xFF
                    } else {
                        //Read with position shift
                        buffer.readByte().toInt() and 0xFF shl i * 8
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

                val range = listOf(8, 0, 24, 16)
                //Reverse range if inverse modifier
                for (i in if (modifier == Modifier.NONE) range else range.reversed()) {
                    longValue = longValue or (buffer.readByte().toInt() and 0xFF shl i).toLong()
                }
            }
        }
        return longValue
    }

    fun startBitAccess(): PacketReader {
        bitIndex = buffer.readerIndex() * 8
        return this
    }

    fun finishBitAccess(): PacketReader {
        buffer.readerIndex((bitIndex + 7) / 8)
        return this
    }

    @Suppress("NAME_SHADOWING")
    fun readBits(bitCount: Int): Int {
        if (bitCount < 0 || bitCount > 32) {
            throw IllegalArgumentException("Number of bits must be between 1 and 32 inclusive")
        }

        var bitCount = bitCount
        var bytePos = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        var value = 0
        bitIndex += bitCount

        while (bitCount > bitOffset) {
            value += buffer.getByte(bytePos++).toInt() and BIT_MASKS[bitOffset] shl bitCount - bitOffset
            bitCount -= bitOffset
            bitOffset = 8
        }
        value += if (bitCount == bitOffset) {
            buffer.getByte(bytePos).toInt() and BIT_MASKS[bitOffset]
        } else {
            buffer.getByte(bytePos).toInt() shr bitOffset - bitCount and BIT_MASKS[bitCount]
        }
        return value
    }

    fun getBuffer(): ByteBuf {
        return buffer
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

    override fun toString(): String {
        return "PacketReader[opcode=$opcode, type=$type, buffer=${buffer.getHexContents()}"
    }

}
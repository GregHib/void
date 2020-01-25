package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import org.redrune.network.packet.struct.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 2:34 a.m.
 */
class PacketBuilder(
    val opcode: Int = -1,
    val header: PacketHeader = PacketHeader.RAW,
    val buffer: ByteBuf
) {

    private var mode: AccessMode = AccessMode.BYTE_ACCESS
    private var bitIndex = 0

    /**
     * Writes a byte to [buffer].
     * @param value [Int]
     */
    fun writeByte(value: Int, type: Modifier = Modifier.NONE): PacketBuilder {
        write(DataType.BYTE, value, type)
        return this
    }

    /**
     * Writes a [Short] to [buffer].
     * @param value [Int]
     */
    fun writeShort(value: Int, type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): PacketBuilder {
        write(DataType.SHORT, value, type, order)
        return this
    }

    fun writeSmart(value: Int): PacketBuilder {
        if (value >= 128) {
            writeShort(value + 32768)
        } else {
            writeByte(value)
        }
        return this
    }

    /**
     * Writes a Medium [Int] to [buffer]
     * @param value [Int]
     */
    fun writeMedium(value: Int): PacketBuilder {
        write(DataType.MEDIUM, value)
        return this
    }

    /**
     * Writes a [Int] to [buffer].
     * @param value [Int]
     */
    fun writeInt(value: Int, type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): PacketBuilder {
        write(DataType.INT, value, type, order)
        return this
    }

    /**
     * Writes a [Long] to [buffer].
     * @param value [Long]
     */
    fun writeLong(value: Long): PacketBuilder {
        write(DataType.LONG, value)
        return this
    }

    fun toPacket(): Packet {
        check(!(mode !== AccessMode.BYTE_ACCESS)) { "Must be in byte access mode to convert to a packet" }
        return Packet(opcode, header, buffer)
    }

    val length: Int
        get() {
            checkByteAccess()
            return buffer.writerIndex()
        }

    fun switchToByteAccess() {
        check(!(mode === AccessMode.BYTE_ACCESS)) { "Already in byte access mode" }
        mode = AccessMode.BYTE_ACCESS
        buffer.writerIndex((bitIndex + 7) / 8)
    }

    fun switchToBitAccess() {
        check(!(mode === AccessMode.BIT_ACCESS)) { "Already in bit access mode" }
        mode = AccessMode.BIT_ACCESS
        bitIndex = buffer.writerIndex() * 8
    }

    fun putRawBuilder(builder: PacketBuilder) {
        checkByteAccess()
        require(!(builder.header !== PacketHeader.RAW)) { "Builder must be raw!" }
        builder.checkByteAccess()
        writeBytes(builder.buffer)
    }

    /**
     * Puts a raw builder in reverse. Both builders (this and parameter) must
     * be in byte access mode.
     * @param builder The builder.
     */
    fun putRawBuilderReverse(builder: PacketBuilder) {
        checkByteAccess()
        require(!(builder.header !== PacketHeader.RAW)) { "Builder must be raw!" }
        builder.checkByteAccess()
        putBytesReverse(builder.buffer)
    }

    /**
     * Puts a standard data type with the specified value, byte order and
     * transformation.
     * @param type The data type.
     * @param order The byte order.
     * @param transformation The transformation.
     * @param value The value.
     * @throws IllegalStateException if this reader is not in byte access mode.
     * @throws IllegalArgumentException if the combination is invalid.
     */
    fun write(type: DataType, value: Number, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG) {
        checkByteAccess()
        val longValue = value.toLong()
        when (order) {
            Endian.BIG, Endian.LITTLE -> {
                val range = if (order == Endian.LITTLE) 0 until type.bytes else type.bytes - 1 downTo 0
                for (i in range) {
                    if (i == 0 && modifier != Modifier.NONE) {
                        when (modifier) {
                            Modifier.ADD -> buffer.writeByte((longValue + 128).toByte().toInt())
                            Modifier.NEGATE -> buffer.writeByte((-longValue).toByte().toInt())
                            Modifier.SUBTRACT -> buffer.writeByte((128 - longValue).toByte().toInt())
                            else -> throw IllegalArgumentException("Unknown byte modifier")
                        }
                    } else {
                        buffer.writeByte((longValue shr i * 8).toByte().toInt())
                    }
                }
            }
            Endian.MIDDLE -> {
                if (modifier != Modifier.NONE && modifier != Modifier.NEGATE) {
                    throw IllegalArgumentException("Middle endian doesn't support variable modifier $modifier")
                }

                if (type != DataType.INT) {
                    throw IllegalArgumentException("Middle endian can only be used with an integer")
                }

                val range = listOf(8, 0, 24, 16)
                //Reverse range if inverse modifier
                for (i in if (modifier == Modifier.NONE) range else range.reversed()) {
                    buffer.writeByte((longValue shr i).toByte().toInt())
                }
            }
        }
    }

    /**
     * Puts a string into the buffer.
     * @param str The string.
     */
    fun putString(str: String) {
        checkByteAccess()
        val chars = str.toCharArray()
        for (c in chars) {
            buffer.writeByte((c as Byte).toInt())
        }
        buffer.writeByte(0)
    }

    /**
     * Puts a smart into the buffer.
     * @param value The value.
     */
    fun putSmart(value: Int) {
        checkByteAccess()
        if (value < 128) {
            buffer.writeByte(value)
        } else {
            buffer.writeShort(value)
        }
    }

    /**
     * Puts the bytes from the specified buffer into this packet's buffer.
     * @param buffer The source [ByteBuf].
     * @throws IllegalStateException if the builder is not in byte access mode.
     */
    fun writeBytes(buffer: ByteBuf) {
        val bytes = ByteArray(buffer.readableBytes())
        buffer.markReaderIndex()
        try {
            buffer.readBytes(bytes)
        } finally {
            buffer.resetReaderIndex()
        }
        writeBytes(bytes)
    }

    /**
     * Puts the bytes from the specified buffer into this packet's buffer, in
     * reverse.
     * @param buffer The source [ByteBuf].
     * @throws IllegalStateException if the builder is not in byte access mode.
     */
    fun putBytesReverse(buffer: ByteBuf) {
        val bytes = ByteArray(buffer.readableBytes())
        buffer.markReaderIndex()
        try {
            buffer.readBytes(bytes)
        } finally {
            buffer.resetReaderIndex()
        }
        putBytesReverse(bytes)
    }

    /**
     * Puts the specified byte array into the buffer.
     * @param bytes The byte array.
     * @throws IllegalStateException if the builder is not in bit access mode.
     */
    fun writeBytes(bytes: ByteArray?) {
        buffer.writeBytes(bytes)
    }

    /**
     * Puts the specified byte array into the buffer in reverse.
     * @param bytes The byte array.
     * @throws IllegalStateException if the builder is not in byte access mode.
     */
    fun putBytesReverse(bytes: ByteArray) {
        checkByteAccess()
        for (i in bytes.indices.reversed()) {
            buffer.writeByte(bytes[i].toInt())
        }
    }

    /**
     * Puts a single bit into the buffer. If `flag` is `true`, the
     * value of the bit is `1`. If `flag` is `false`, the
     * value of the bit is `0`.
     * @param flag The flag.
     * @throws IllegalStateException if the builder is not in bit access mode.
     */
    fun putBit(flag: Boolean) {
        putBit(if (flag) 1 else 0)
    }

    /**
     * Puts a single bit into the buffer with the value `value`.
     * @param value The value.
     * @throws IllegalStateException if the builder is not in bit access mode.
     */
    fun putBit(value: Int) {
        putBits(1, value)
    }

    /**
     * Puts `numBits` into the buffer with the value `value`.
     * @param numBits The number of bits to put into the buffer.
     * @param value The value.
     * @throws IllegalStateException if the builder is not in bit access mode.
     * @throws IllegalArgumentException if the number of bits is not between 1
     * and 31 inclusive.
     */
    fun putBits(numBits: Int, value: Int) {
        var numBits = numBits
        require(!(numBits <= 0 || numBits > 32)) { "Number of bits must be between 1 and 31 inclusive" }
        checkBitAccess()
        var bytePos = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        bitIndex += numBits
        var requiredSpace = bytePos - buffer.writerIndex() + 1
        requiredSpace += (numBits + 7) / 8
        buffer.ensureWritable(requiredSpace)
        while (numBits > bitOffset) {
            var tmp = buffer.getByte(bytePos).toInt()
            tmp = tmp and BITMASKS[bitOffset].inv()
            tmp = tmp or (value shr numBits - bitOffset) and BITMASKS[bitOffset]
            buffer.setByte(bytePos++, tmp)
            numBits -= bitOffset
            bitOffset = 8
        }
        if (numBits == bitOffset) {
            var tmp = buffer.getByte(bytePos).toInt()
            tmp = tmp and BITMASKS[bitOffset].inv()
            tmp = tmp or value and BITMASKS[bitOffset]
            buffer.setByte(bytePos, tmp)
        } else {
            var tmp = buffer.getByte(bytePos).toInt()
            tmp = tmp and (BITMASKS[numBits] shl bitOffset - numBits).inv()
            tmp = tmp or (value and BITMASKS[numBits]) shl bitOffset - numBits
            buffer.setByte(bytePos, tmp)
        }
    }

    /**
     * Checks that this builder is in the byte access mode.
     * @throws IllegalStateException if the builder is not in byte access mode.
     */
    private fun checkByteAccess() {
        check(!(mode !== AccessMode.BYTE_ACCESS)) { "For byte-based calls to work, the mode must be byte access" }
    }

    /**
     * Checks that this builder is in the bit access mode.
     * @throws IllegalStateException if the builder is not in bit access mode.
     */
    private fun checkBitAccess() {
        check(!(mode !== AccessMode.BIT_ACCESS)) { "For bit-based calls to work, the mode must be bit access" }
    }

    companion object {
        private val BITMASKS = intArrayOf(
            0x0, 0x1, 0x3, 0x7,
            0xf, 0x1f, 0x3f, 0x7f,
            0xff, 0x1ff, 0x3ff, 0x7ff,
            0xfff, 0x1fff, 0x3fff, 0x7fff,
            0xffff, 0x1ffff, 0x3ffff, 0x7ffff,
            0xfffff, 0x1fffff, 0x3fffff, 0x7fffff,
            0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff,
            0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff,
            -1
        )
    }

}
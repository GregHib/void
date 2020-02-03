package org.redrune.network.model.packet

import io.netty.buffer.ByteBuf

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 2:34 a.m.
 */
// TODO design this better
class PacketBuilder(
    val opcode: Int = -1,
    val header: PacketType = PacketType.RAW,
    val buffer: ByteBuf
) {
    private var sizeIndex = 0
    private var bitIndex = 0
    private var mode = AccessMode.BYTE

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
        write(DataType.TRI_BYTE, value)
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
        check(!(mode !== AccessMode.BYTE)) { "Must be in byte access mode to convert to a packet" }
        return Packet(opcode, buffer)
    }

    val length: Int
        get() {
            checkByteAccess()
            return buffer.writerIndex()
        }

    fun switchToBitAccess() {
        mode = AccessMode.BIT
        bitIndex = buffer.writerIndex() * 8
    }

    fun putRawBuilder(builder: PacketBuilder) {
        checkByteAccess()
        require(!(builder.header !== PacketType.RAW)) { "Builder must be raw!" }
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
        require(!(builder.header !== PacketType.RAW)) { "Builder must be raw!" }
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
                            Modifier.INVERSE -> buffer.writeByte((-longValue).toByte().toInt())
                            Modifier.SUBTRACT -> buffer.writeByte((128 - longValue).toByte().toInt())
                            else -> throw IllegalArgumentException("Unknown byte modifier")
                        }
                    } else {
                        buffer.writeByte((longValue shr i * 8).toByte().toInt())
                    }
                }
            }
            Endian.MIDDLE -> {
                if (modifier != Modifier.NONE && modifier != Modifier.INVERSE) {
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

    fun writeBits(bitCount: Int, value: Boolean): PacketBuilder {
        return writeBits(bitCount, if (value) 1 else 0)
    }

    fun writeBits(bitCount: Int, value: Int): PacketBuilder {
//        checkBitAccess()
        var numBits = bitCount

        var bytePos = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        bitIndex += numBits

        var requiredSpace = bytePos - buffer.writerIndex() + 1
        requiredSpace += (numBits + 7) / 8
        buffer.ensureWritable(requiredSpace)

        while (numBits > bitOffset) {
            var tmp = buffer.getByte(bytePos).toInt()
            tmp = tmp and BIT_MASKS[bitOffset].inv()
            tmp = tmp or (value shr numBits - bitOffset and BIT_MASKS[bitOffset])
            buffer.setByte(bytePos++, tmp)
            numBits -= bitOffset
            bitOffset = 8
        }
        if (numBits == bitOffset) {
            var tmp = buffer.getByte(bytePos).toInt()
            tmp = tmp and BIT_MASKS[bitOffset].inv()
            tmp = tmp or (value and BIT_MASKS[bitOffset])
            buffer.setByte(bytePos, tmp)
        } else {
            var tmp = buffer.getByte(bytePos).toInt()
            tmp = tmp and (BIT_MASKS[numBits] shl bitOffset - numBits).inv()
            tmp = tmp or (value and BIT_MASKS[numBits] shl bitOffset - numBits)
            buffer.setByte(bytePos, tmp)
        }
        return this
    }

    /**
     * Checks the write mode is byte
     */
    private fun checkByteAccess() {
        if (mode != AccessMode.BYTE) {
            throw IllegalStateException("Can't write bytes while in bit access mode")
        }
    }

    /**
     * Checks the write mode is bit
     */
    private fun checkBitAccess() {
        if (mode != AccessMode.BIT) {
            throw IllegalStateException("Can't write bits while in byte access mode")
        }
    }

    fun finishBitAccess(): PacketBuilder {
        /*if (mode == AccessMode.BYTE) {
            throw IllegalStateException("Already in byte access mode")
        }*/
        mode = AccessMode.BYTE
        buffer.writerIndex((bitIndex + 7) / 8)
        return this
    }

    companion object {
        /**
         * Bit masks for [writeBits]
         */
        private val BIT_MASKS = IntArray(32)

        init {
            for (i in BIT_MASKS.indices)
                BIT_MASKS[i] = (1 shl i) - 1
        }
    }

    /**
     * The packet write mode
     */
    private enum class AccessMode {
        BYTE,
        BIT;
    }

}
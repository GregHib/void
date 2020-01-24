package org.redrune.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 2:34 a.m.
 */
class GameFrameBuilder(
    private val allocator: ByteBufAllocator,
    val opcode: Int = -1,
    val header: PacketHeader = PacketHeader.RAW,
    val buffer: ByteBuf = allocator.buffer()
) {
    private var mode: AccessMode = AccessMode.BYTE_ACCESS
    private var bitIndex = 0

    constructor(alloc: ByteBufAllocator, opcode: Int) : this(alloc, opcode, PacketHeader.FIXED)

    fun toGameFrame(): Packet {
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

    fun putRawBuilder(builder: GameFrameBuilder) {
        checkByteAccess()
        require(!(builder.header !== PacketHeader.RAW)) { "Builder must be raw!" }
        builder.checkByteAccess()
        putBytes(builder.buffer)
    }

    /**
     * Puts a raw builder in reverse. Both builders (this and parameter) must
     * be in byte access mode.
     * @param builder The builder.
     */
    fun putRawBuilderReverse(builder: GameFrameBuilder) {
        checkByteAccess()
        require(!(builder.header !== PacketHeader.RAW)) { "Builder must be raw!" }
        builder.checkByteAccess()
        putBytesReverse(builder.buffer)
    }

    /**
     * Puts a standard data type with the specified value.
     * @param type The data type.
     * @param value The value.
     * @throws IllegalStateException if this reader is not in byte access mode.
     */
    fun put(type: DataType, value: Number) {
        put(type, DataOrder.BIG, DataTransformation.NONE, value)
    }

    /**
     * Puts a standard data type with the specified value and byte order.
     * @param type The data type.
     * @param order The byte order.
     * @param value The value.
     * @throws IllegalStateException if this reader is not in byte access mode.
     * @throws IllegalArgumentException if the combination is invalid.
     */
    fun put(type: DataType, order: DataOrder, value: Number) {
        put(type, order, DataTransformation.NONE, value)
    }

    /**
     * Puts a standard data type with the specified value and transformation.
     * @param type The type.
     * @param transformation The transformation.
     * @param value The value.
     * @throws IllegalStateException if this reader is not in byte access mode.
     * @throws IllegalArgumentException if the combination is invalid.
     */
    fun put(type: DataType, transformation: DataTransformation, value: Number) {
        put(type, DataOrder.BIG, transformation, value)
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
    fun put(type: DataType, order: DataOrder, transformation: DataTransformation, value: Number) {
        checkByteAccess()
        val longValue: Long = value.toLong()
        val length: Int = type.bytes
        if (order === DataOrder.BIG) {
            for (i in length - 1 downTo 0) {
                if (i == 0 && transformation !== DataTransformation.NONE) {
                    if (transformation === DataTransformation.ADD) {
                        buffer.writeByte(((longValue + 128).toByte()).toInt())
                    } else if (transformation === DataTransformation.NEGATE) {
                        buffer.writeByte(((-longValue).toByte()).toInt())
                    } else if (transformation === DataTransformation.SUBTRACT) {
                        buffer.writeByte(((128 - longValue).toByte()).toInt())
                    } else {
                        throw IllegalArgumentException("unknown transformation")
                    }
                } else {
                    buffer.writeByte(((longValue shr i * 8).toByte()).toInt())
                }
            }
        } else if (order === DataOrder.LITTLE) {
            for (i in 0 until length) {
                if (i == 0 && transformation !== DataTransformation.NONE) {
                    if (transformation === DataTransformation.ADD) {
                        buffer.writeByte(((longValue + 128).toByte()).toInt())
                    } else if (transformation === DataTransformation.NEGATE) {
                        buffer.writeByte(((-longValue).toByte()).toInt())
                    } else if (transformation === DataTransformation.SUBTRACT) {
                        buffer.writeByte(((128 - longValue) as Byte).toInt())
                    } else {
                        throw IllegalArgumentException("unknown transformation")
                    }
                } else {
                    buffer.writeByte(((longValue shr i * 8) as Byte).toInt())
                }
            }
        } else if (order === DataOrder.MIDDLE) {
            require(!(transformation !== DataTransformation.NONE)) { "middle endian cannot be transformed" }
            require(!(type !== DataType.INT)) { "middle endian can only be used with an integer" }
            buffer.writeByte(((longValue shr 8) as Byte).toInt())
            buffer.writeByte((longValue as Byte).toInt())
            buffer.writeByte(((longValue shr 24) as Byte).toInt())
            buffer.writeByte(((longValue shr 16) as Byte).toInt())
        } else if (order === DataOrder.INVERSED_MIDDLE) {
            require(!(transformation !== DataTransformation.NONE)) { "inversed middle endian cannot be transformed" }
            require(!(type !== DataType.INT)) { "inversed middle endian can only be used with an integer" }
            buffer.writeByte(((longValue shr 16) as Byte).toInt())
            buffer.writeByte(((longValue shr 24) as Byte).toInt())
            buffer.writeByte((longValue as Byte).toInt())
            buffer.writeByte(((longValue shr 8) as Byte).toInt())
        } else {
            throw IllegalArgumentException("unknown order")
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
    fun putBytes(buffer: ByteBuf) {
        val bytes = ByteArray(buffer.readableBytes())
        buffer.markReaderIndex()
        try {
            buffer.readBytes(bytes)
        } finally {
            buffer.resetReaderIndex()
        }
        putBytes(bytes)
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
    fun putBytes(bytes: ByteArray?) {
        buffer.writeBytes(bytes)
    }

    /**
     * Puts the bytes into the buffer with the specified transformation.
     * @param transformation The transformation.
     * @param bytes The byte array.
     * @throws IllegalStateException if the builder is not in byte access mode.
     */
    fun putBytes(transformation: DataTransformation, bytes: ByteArray) {
        if (transformation === DataTransformation.NONE) {
            putBytes(bytes)
        } else {
            for (b in bytes) {
                put(DataType.BYTE, transformation, b)
            }
        }
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
     * Puts the specified byte array into the buffer in reverse with the
     * specified transformation.
     * @param transformation The transformation.
     * @param bytes The byte array.
     * @throws IllegalStateException if the builder is not in byte access mode.
     */
    fun putBytesReverse(transformation: DataTransformation, bytes: ByteArray) {
        if (transformation === DataTransformation.NONE) {
            putBytesReverse(bytes)
        } else {
            for (i in bytes.indices.reversed()) {
                put(DataType.BYTE, transformation, bytes[i])
            }
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
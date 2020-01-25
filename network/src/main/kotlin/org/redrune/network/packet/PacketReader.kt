package org.redrune.network.packet

import org.redrune.network.packet.struct.AccessMode
import org.redrune.network.packet.struct.DataType
import org.redrune.network.packet.struct.Endian
import org.redrune.network.packet.struct.Modifier
import org.redrune.tools.func.ByteBufUtils
import kotlin.experimental.and

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-21
 */
class PacketReader(private val packet: Packet) {
    private val buffer = packet.buffer
    private var mode: AccessMode = AccessMode.BYTE_ACCESS
    private var bitIndex = 0
    val opcode = packet.opcode

    fun toPacket(): Packet {
        return packet
    }

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

    fun getLength(): Int {
        checkByteAccess()
        return buffer.readableBytes()
    }

    fun switchToByteAccess() {
        check(!(mode == AccessMode.BYTE_ACCESS)) { "Already in byte access mode" }
        mode = AccessMode.BYTE_ACCESS
        buffer.readerIndex((bitIndex + 7) / 8)
    }

    fun switchToBitAccess() {
        check(!(mode == AccessMode.BIT_ACCESS)) { "Already in bit access mode" }
        mode = AccessMode.BIT_ACCESS
        bitIndex = buffer.readerIndex() * 8
    }

    fun getString(): String {
        checkByteAccess()
        return ByteBufUtils.readString(buffer)
    }

    fun getSignedSmart(): Int {
        checkByteAccess()
        val peek = buffer.getByte(buffer.readerIndex()).toInt()
        return if (peek < 128) {
            buffer.readByte() - 64
        } else {
            buffer.readShort() - 49152
        }
    }

    fun getUnsignedSmart(): Int {
        checkByteAccess()
        val peek = buffer.getByte(buffer.readerIndex()).toInt()
        return if (peek < 128) {
            buffer.readByte().toInt()
        } else {
            buffer.readShort() - 32768
        }
    }

    fun readSigned(type: DataType, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Long {
        var longValue = get(type, modifier, order)
        if (type != DataType.LONG) {
            val max = Math.pow(2.0, type.bytes * 8.0 - 1).toInt()
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
        val longValue = get(type, modifier, order)
        return longValue and -0x1L
    }

    private operator fun get(type: DataType, modifier: Modifier, order: Endian): Long {
        checkByteAccess()
        var longValue: Long = 0
        val length: Int = type.bytes
        when (order) {
            Endian.BIG -> {
                for (i in length - 1 downTo 0) {
                    longValue = if (i == 0 && modifier != Modifier.NONE) {
                        if (modifier == Modifier.ADD) {
                            longValue or (buffer.readByte() - 128).toLong() and 0xFF
                        } else if (modifier == Modifier.NEGATE) {
                            longValue or (-buffer.readByte()).toLong() and 0xFF
                        } else if (modifier == Modifier.SUBTRACT) {
                            longValue or (128 - buffer.readByte()).toLong() and 0xFF
                        } else {
                            throw IllegalArgumentException("unknown transformation")
                        }
                    } else {
                        longValue or ((buffer.readByte().toInt() and 0xFF shl i * 8).toLong())
                    }
                }
            }
            Endian.LITTLE -> {
                for (i in 0 until length) {
                    longValue = if (i == 0 && modifier != Modifier.NONE) {
                        if (modifier == Modifier.ADD) {
                            longValue or (buffer.readByte() - 128).toLong() and 0xFF
                        } else if (modifier == Modifier.NEGATE) {
                            longValue or (-buffer.readByte()).toLong() and 0xFF
                        } else if (modifier == Modifier.SUBTRACT) {
                            longValue or (128 - buffer.readByte()).toLong() and 0xFF
                        } else {
                            throw IllegalArgumentException("unknown transformation")
                        }
                    } else {
                        longValue or ((buffer.readByte().toInt() and 0xFF shl i * 8).toLong())
                    }
                }
            }
            Endian.MIDDLE -> {
                require(!(modifier != Modifier.NONE)) { "middle endian cannot be transformed" }
                require(!(type != DataType.INT)) { "middle endian can only be used with an integer" }
                longValue = longValue or ((buffer.readByte() and 0xFF.toByte()).toLong()) shl 8
                longValue = longValue or buffer.readByte().toLong() and 0xFF
                longValue = longValue or ((buffer.readByte() and 0xFF.toByte()).toLong()) shl 24
                longValue = longValue or ((buffer.readByte() and 0xFF.toByte()).toLong()) shl 16
            }
            Endian.INVERSED_MIDDLE -> {
                require(!(modifier != Modifier.NONE)) { "inversed middle endian cannot be transformed" }
                require(!(type != DataType.INT)) { "inversed middle endian can only be used with an integer" }
                longValue = longValue or ((buffer.readByte() and 0xFF.toByte()).toLong()) shl 16
                longValue = longValue or ((buffer.readByte() and 0xFF.toByte()).toLong()) shl 24
                longValue = longValue or buffer.readByte().toLong() and 0xFF
                longValue = longValue or ((buffer.readByte() and 0xFF.toByte()).toLong()) shl 8
            }
            else -> {
                throw IllegalArgumentException("unknown order")
            }
        }
        return longValue
    }

    fun getBytes(bytes: ByteArray) {
        checkByteAccess()
        for (i in bytes.indices) {
            bytes[i] = buffer.readByte()
        }
    }

    fun getBytes(transformation: Modifier, bytes: ByteArray) {
        if (transformation == Modifier.NONE) {
            getBytesReverse(bytes)
        } else {
            for (i in bytes.indices) {
                bytes[i] = readSigned(DataType.BYTE, transformation).toByte()
            }
        }
    }

    fun getBytesReverse(bytes: ByteArray) {
        checkByteAccess()
        for (i in bytes.indices.reversed()) {
            bytes[i] = buffer.readByte()
        }
    }

    fun getBytesReverse(transformation: Modifier, bytes: ByteArray) {
        if (transformation == Modifier.NONE) {
            getBytesReverse(bytes)
        } else {
            for (i in bytes.indices.reversed()) {
                bytes[i] = readSigned(DataType.BYTE, transformation).toByte()
            }
        }
    }

    private fun checkByteAccess() {
        check(!(mode != AccessMode.BYTE_ACCESS)) { "For byte-based calls to work, the mode must be byte access" }
    }

    private fun checkBitAccess() {
        check(!(mode != AccessMode.BIT_ACCESS)) { "For bit-based calls to work, the mode must be bit access" }
    }

    fun getBit(): Int {
        return getBits(1)
    }

    fun getBits(bitCount: Int): Int {
        var bitCount = bitCount
        require(!(bitCount < 0 || bitCount > 32)) { "Number of bits must be between 1 and 32 inclusive" }
        checkBitAccess()
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

    companion object {
        private val BIT_MASKS = intArrayOf(
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

package org.redrune.network.model.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
// TODO design this beter
class PacketReader(val opcode: Int, val payload: ByteBuf) {

    constructor(packet: Packet) : this(packet.opcode, packet.payload)
    constructor(byteArray: ByteArray) : this(-1, Unpooled.copiedBuffer(byteArray))

    fun isReadable(): Boolean {
        return payload.isReadable
    }

    fun readableBytes(): Int {
        return payload.readableBytes()
    }

    fun readBytes(length: Int): ByteBuf {
        return payload.readBytes(length)
    }

    fun readBytes(value: ByteArray): ByteBuf {
        return payload.readBytes(value)
    }

    fun readBytes(array: ByteArray, offset: Int, length: Int): ByteBuf {
        return payload.readBytes(array, offset, length)
    }

    fun readUnsignedByte(): Int {
        return readUnsigned(DataType.BYTE).toInt()
    }

    fun readUnsignedShort(): Int {
        return readUnsigned(DataType.SHORT).toInt()
    }

    fun readUnsignedInt(): Int {
        return readUnsigned(DataType.INT).toInt()
    }

    fun readTriByte(): Int {
        return readUnsigned(DataType.TRI_BYTE).toInt()
    }

    fun readByte(type: Modifier = Modifier.NONE): Int {
        return readSigned(DataType.BYTE, type).toInt()
    }

    fun skip(amount: Int) {
        payload.skipBytes(amount)
    }

    fun readBoolean(type: Modifier = Modifier.NONE): Boolean {
        return readByte(type) == 1
    }

    fun readInt(type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Int {
        return readSigned(DataType.INT, type, order).toInt()
    }

    fun readShort(type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Int {
        return readSigned(DataType.SHORT, type, order).toInt()
    }

    fun readLong(): Long {
        val first = readInt().toLong() and 0xffffffffL
        val second = readInt().toLong() and 0xffffffffL
        return second + (first shl 32)
    }

    fun readString(): String {
        val sb = StringBuilder()
        var b: Int
        while (payload.isReadable) {
            b = readByte()
            if (b == 0) {
                break
            }
            sb.append(b.toChar())
        }
        return sb.toString()
    }

    private fun readSigned(type: DataType, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Long {
        var longValue = read(type, modifier, order)
        if (type != DataType.LONG) {
            val max = Math.pow(2.0, type.bytes * 8.0 - 1).toInt()
            if (longValue > max - 1) {
                longValue -= max * 2L
            }
        }
        return longValue
    }

    private fun readUnsigned(type: DataType, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Long {
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
        if (!payload.isReadable(type.bytes)) {
            throw IndexOutOfBoundsException("Not enough allocated buffer remaining $type.")
        }

        var longValue: Long = 0
        when (order) {
            Endian.BIG, Endian.LITTLE -> {
                //For by length
                val range = if (order == Endian.LITTLE) 0 until type.bytes else type.bytes - 1 downTo 0
                var read: Long
                for (i in range) {
                    //If first and has a modifier
                    read = if (i == 0 && modifier != Modifier.NONE) {
                        //Read with variable modifier transform
                        when (modifier) {
                            Modifier.ADD -> payload.readByte() - 128
                            Modifier.INVERSE -> -payload.readByte()
                            Modifier.SUBTRACT -> 128 - payload.readByte()
                            else -> throw IllegalArgumentException("Unknown byte modifier")
                        } and 0xFF
                    } else {
                        //Read with position shift
                        payload.readByte().toInt() and 0xFF shl i * 8
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
                    longValue = longValue or (payload.readByte().toInt() and 0xFF shl i).toLong()
                }
            }
        }
        return longValue
    }
}

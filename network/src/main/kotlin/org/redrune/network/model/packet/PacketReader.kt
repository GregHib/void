package org.redrune.network.model.packet

import io.netty.buffer.ByteBuf

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class PacketReader(private val buffer: ByteBuf) {

    fun isReadable(): Boolean {
        return buffer.isReadable
    }

    fun readableBytes(): Int {
        return buffer.readableBytes()
    }

    fun readBytes(length: Int): ByteBuf {
        return buffer.readBytes(length)
    }

    fun readBytes(value: ByteArray): ByteBuf {
        return buffer.readBytes(value)
    }

    fun readBytes(array: ByteArray, offset: Int, length: Int): ByteBuf {
        return buffer.readBytes(array, offset, length)
    }

    fun readUnsignedByte(): Int {
        return readUnsigned(DataType.BYTE).toInt()
    }

    fun readUnsignedInt(): Int {
        return readUnsigned(DataType.INT).toInt()
    }

    private fun readSigned(type: DataType, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Long {
        var longValue = read(type, modifier, order)
        if (type != DataType.LONG) {
            val max = Math.pow(2.0, type.length * 8.0 - 1).toInt()
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
        if (!buffer.isReadable(type.length)) {
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
}

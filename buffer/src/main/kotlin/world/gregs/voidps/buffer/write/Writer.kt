package world.gregs.voidps.buffer.write

import io.netty.buffer.ByteBuf
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier

/**
 * All functions relative to writing directly to a packet are done by this class
 *
 * @author GregHib <greg@gregs.world>
 * @since February 18, 2020
 */
interface Writer {

    fun setByte(index: Int, value: Int): Writer

    /**
     * Writes a byte to [buffer].
     * @param value [Int]
     */
    fun writeByte(value: Int): Writer {
        write(DataType.BYTE, value, Modifier.NONE)
        return this
    }

    fun writeByteAdd(value: Int): Writer {
        write(DataType.BYTE, value, Modifier.ADD)
        return this
    }

    fun writeByteInverse(value: Int): Writer {
        write(DataType.BYTE, value, Modifier.INVERSE)
        return this
    }

    fun writeByteSubtract(value: Int): Writer {
        write(DataType.BYTE, value, Modifier.SUBTRACT)
        return this
    }

    /**
     * Writes a boolean as a byte to [buffer].
     * @param value [Boolean]
     */
    fun writeByte(value: Boolean): Writer {
        writeByte(if (value) 1 else 0)
        return this
    }

    /**
     * Writes a [Short] to [buffer].
     * @param value [Int]
     */
    fun writeShort(value: Int, type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Writer {
        write(DataType.SHORT, value, type, order)
        return this
    }

    fun writeShortAdd(value: Int): Writer {
        write(DataType.SHORT, value, Modifier.ADD, Endian.BIG)
        return this
    }

    fun writeShortLittle(value: Int): Writer {
        write(DataType.SHORT, value, Modifier.NONE, Endian.LITTLE)
        return this
    }

    fun writeShortAddLittle(value: Int): Writer {
        write(DataType.SHORT, value, Modifier.ADD, Endian.LITTLE)
        return this
    }

    /**
     * Writes a Medium [Int] to [buffer]
     * @param value [Int]
     */
    fun writeMedium(value: Int): Writer {
        write(DataType.MEDIUM, value)
        return this
    }

    /**
     * Writes a [Int] to [buffer].
     * @param value [Int]
     */
    fun writeInt(value: Int): Writer {
        write(DataType.INT, value, Modifier.NONE, Endian.BIG)
        return this
    }

    fun writeIntMiddle(value: Int): Writer {
        write(DataType.INT, value, Modifier.NONE, Endian.MIDDLE)
        return this
    }

    fun writeIntInverse(value: Int): Writer {
        write(DataType.INT, value, Modifier.INVERSE, Endian.BIG)
        return this
    }

    fun writeIntInverseMiddle(value: Int): Writer {
        write(DataType.INT, value, Modifier.INVERSE, Endian.MIDDLE)
        return this
    }

    fun writeIntLittle(value: Int): Writer {
        write(DataType.INT, value, Modifier.NONE, Endian.LITTLE)
        return this
    }

    fun writeIntInverseLittle(value: Int): Writer {
        write(DataType.INT, value, Modifier.INVERSE, Endian.LITTLE)
        return this
    }

    /**
     * Writes a [Long] to [buffer].
     * @param value [Long]
     */
    fun writeLong(value: Long): Writer {
        write(DataType.LONG, value)
        return this
    }

    fun writeSmart(value: Int): Writer {
        if (value >= 128) {
            writeShort(value + 32768)
        } else {
            writeByte(value)
        }
        return this
    }

    fun writeString(value: String?): Writer {
        if (value != null) {
            writeBytes(value.toByteArray())
        }
        writeByte(0)
        return this
    }

    fun writePrefixedString(value: String): Writer {
        writeByte(0)
        writeBytes(value.toByteArray())
        writeByte(0)
        return this
    }

    fun writeBytes(value: ByteArray): Writer

    fun writeBytes(value: ByteBuf): Writer

    fun writeBytes(data: ByteArray, offset: Int, length: Int): Writer

    fun writeBytes(data: ByteBuf, offset: Int, length: Int): Writer

    fun startBitAccess(): Writer

    fun finishBitAccess(): Writer

    fun writeBits(bitCount: Int, value: Boolean): Writer {
        return writeBits(bitCount, if (value) 1 else 0)
    }

    fun writeBits(bitCount: Int, value: Int): Writer

    fun skip(position: Int): Writer {
        for (i in 0 until position) {
            writeByte(0)
        }
        return this
    }

    fun position(): Int

    fun position(index: Int)

    fun toArray(): ByteArray

    fun array(): ByteArray

    fun clear()

    fun remaining(): Int

    @Throws(IllegalArgumentException::class)
    fun write(type: DataType, value: Number, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG)

}
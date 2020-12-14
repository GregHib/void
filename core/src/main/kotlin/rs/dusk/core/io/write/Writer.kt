package rs.dusk.core.io.write

import io.netty.buffer.ByteBuf
import rs.dusk.core.io.DataType
import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier

/**
 * All functions relative to writing directly to a packet are done by this class
 *
 * @author Greg Hibberd <greg@greghibberd.com>
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
interface Writer {

    /**
     * Writes a byte to [buffer].
     * @param value [Int]
     */
    fun writeByte(value: Int, type: Modifier = Modifier.NONE): Writer {
        write(DataType.BYTE, value, type)
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
    fun writeInt(value: Int, type: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Writer {
        write(DataType.INT, value, type, order)
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

    /**
     * Writes a boolean as a byte to [buffer].
     * @param value [Boolean]
     */
    fun writeByte(value: Boolean, type: Modifier = Modifier.NONE): Writer {
        return writeByte(if (value) 1 else 0, type)
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

    @Throws(IllegalArgumentException::class)
    fun write(type: DataType, value: Number, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG)

}
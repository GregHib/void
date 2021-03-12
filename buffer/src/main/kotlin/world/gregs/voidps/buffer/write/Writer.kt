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

    fun setByte(index: Int, value: Int)

    fun writeByte(value: Int)

    fun writeByteAdd(value: Int) = write(DataType.BYTE, value, Modifier.ADD)

    fun writeByteInverse(value: Int) = write(DataType.BYTE, value, Modifier.INVERSE)

    fun writeByteSubtract(value: Int) = write(DataType.BYTE, value, Modifier.SUBTRACT)

    fun writeByte(value: Boolean) = writeByte(if (value) 1 else 0)

    fun writeShort(value: Int) = write(DataType.SHORT, value)

    fun writeShortAdd(value: Int) = write(DataType.SHORT, value, Modifier.ADD)

    fun writeShortLittle(value: Int) = write(DataType.SHORT, value, Modifier.NONE, Endian.LITTLE)

    fun writeShortAddLittle(value: Int) = write(DataType.SHORT, value, Modifier.ADD, Endian.LITTLE)

    fun writeMedium(value: Int) = write(DataType.MEDIUM, value)

    fun writeInt(value: Int) = write(DataType.INT, value)

    fun writeIntMiddle(value: Int) = write(DataType.INT, value, Modifier.NONE, Endian.MIDDLE)

    fun writeIntInverse(value: Int) = write(DataType.INT, value, Modifier.INVERSE)

    fun writeIntInverseMiddle(value: Int) = write(DataType.INT, value, Modifier.INVERSE, Endian.MIDDLE)

    fun writeIntLittle(value: Int) = write(DataType.INT, value, Modifier.NONE, Endian.LITTLE)

    fun writeIntInverseLittle(value: Int) = write(DataType.INT, value, Modifier.INVERSE, Endian.LITTLE)

    fun writeLong(value: Long)

    fun writeSmart(value: Int) {
        if (value >= 128) {
            writeShort(value + 32768)
        } else {
            writeByte(value)
        }
    }

    fun writeString(value: String?) {
        if (value != null) {
            writeBytes(value.toByteArray())
        }
        writeByte(0)
    }

    fun writePrefixedString(value: String) {
        writeByte(0)
        writeBytes(value.toByteArray())
        writeByte(0)
    }

    fun writeBytes(value: ByteArray)

    fun writeBytes(value: ByteBuf)

    fun writeBytes(data: ByteArray, offset: Int, length: Int)

    fun writeBytes(data: ByteBuf, offset: Int, length: Int)

    fun startBitAccess()

    fun finishBitAccess()

    fun writeBits(bitCount: Int, value: Boolean) {
        writeBits(bitCount, if (value) 1 else 0)
    }

    fun writeBits(bitCount: Int, value: Int)

    fun skip(position: Int) {
        for (i in 0 until position) {
            writeByte(0)
        }
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
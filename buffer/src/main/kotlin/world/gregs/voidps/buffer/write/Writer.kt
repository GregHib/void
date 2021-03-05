package world.gregs.voidps.buffer.write

import io.netty.buffer.ByteBuf

/**
 * All functions relative to writing directly to a packet are done by this class
 *
 * @author GregHib <greg@gregs.world>
 * @since February 18, 2020
 */
interface Writer {

    fun setByte(index: Int, value: Int): Writer

    fun writeByte(value: Int): Writer

    fun writeByteAdd(value: Int): Writer

    fun writeByteInverse(value: Int): Writer

    fun writeByteSubtract(value: Int): Writer

    fun writeByte(value: Boolean): Writer {
        writeByte(if (value) 1 else 0)
        return this
    }

    fun writeShort(value: Int): Writer

    fun writeShortAdd(value: Int): Writer

    fun writeShortLittle(value: Int): Writer

    fun writeShortAddLittle(value: Int): Writer

    fun writeMedium(value: Int): Writer

    fun writeInt(value: Int): Writer

    fun writeIntMiddle(value: Int): Writer

    fun writeIntInverse(value: Int): Writer

    fun writeIntInverseMiddle(value: Int): Writer

    fun writeIntLittle(value: Int): Writer

    fun writeIntInverseLittle(value: Int): Writer

    fun writeLong(value: Long): Writer

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

}
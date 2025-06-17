package world.gregs.voidps.buffer.write

/**
 * All functions relative to writing directly to a packet are done by this class
 */
interface Writer {

    fun setByte(index: Int, value: Int)

    fun writeByte(value: Int)

    fun writeByteAdd(value: Int)

    fun writeByteInverse(value: Int)

    fun writeByteSubtract(value: Int)

    fun writeByte(value: Boolean) {
        writeByte(if (value) 1 else 0)
    }

    fun writeShort(value: Int)

    fun writeShortAdd(value: Int)

    fun writeShortLittle(value: Int)

    fun writeShortAddLittle(value: Int)

    fun writeMedium(value: Int)

    fun writeInt(value: Int)

    fun writeIntMiddle(value: Int)

    fun writeIntInverse(value: Int)

    fun writeIntInverseMiddle(value: Int)

    fun writeIntLittle(value: Int)

    fun writeIntInverseLittle(value: Int)

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
            for (char in value) {
                writeByte(char.code)
            }
        }
        writeByte(0)
    }

    fun writePrefixedString(value: String) {
        writeByte(0)
        for (char in value) {
            writeByte(char.code)
        }
        writeByte(0)
    }

    fun writeBytes(value: ByteArray)

    fun writeBytes(data: ByteArray, offset: Int, length: Int)

    fun startBitAccess()

    fun stopBitAccess()

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

    fun bitIndex(): Int

    fun bitIndex(index: Int)

    fun toArray(): ByteArray

    fun array(): ByteArray

    fun clear()

    fun remaining(): Int
}

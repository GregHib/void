package world.gregs.voidps.buffer.write

import world.gregs.voidps.buffer.Unicode

/**
 * All functions relative to writing directly to a packet are done by this class
 */
interface Writer {

    fun setByte(index: Int, value: Int)

    fun writeByte(value: Int)

    fun writeByte(value: Boolean) {
        writeByte(if (value) 1 else 0)
    }

    fun writeByteAdd(value: Int) {
        writeByte(value + 128)
    }

    fun writeByteInverse(value: Int) {
        writeByte(-value)
    }

    fun writeByteSubtract(value: Int) {
        writeByte(-value + 128)
    }

    fun writeShort(value: Int) {
        writeByte(value shr 8)
        writeByte(value)
    }

    fun writeShortAdd(value: Int) {
        writeByte(value shr 8)
        writeByteAdd(value)
    }

    fun writeShortLittle(value: Int) {
        writeByte(value)
        writeByte(value shr 8)
    }

    fun writeShortAddLittle(value: Int) {
        writeByteAdd(value)
        writeByte(value shr 8)
    }

    fun writeMedium(value: Int) {
        writeByte(value shr 16)
        writeByte(value shr 8)
        writeByte(value)
    }

    fun writeInt(value: Int) {
        writeByte(value shr 24)
        writeByte(value shr 16)
        writeByte(value shr 8)
        writeByte(value)
    }

    fun writeIntMiddle(value: Int) {
        writeByte(value shr 8)
        writeByte(value)
        writeByte(value shr 24)
        writeByte(value shr 16)
    }

    fun writeIntInverse(value: Int) {
        writeByte(value shr 8)
        writeByte(value shr 24)
        writeByte(value shr 16)
        writeByteInverse(value)
    }

    fun writeIntInverseMiddle(value: Int) {
        writeByte(value shr 16)
        writeByte(value shr 24)
        writeByte(value)
        writeByte(value shr 8)
    }

    fun writeIntLittle(value: Int) {
        writeByte(value)
        writeByte(value shr 8)
        writeByte(value shr 16)
        writeByte(value shr 24)
    }

    fun writeIntInverseLittle(value: Int) {
        writeByteInverse(value)
        writeByte(value shr 8)
        writeByte(value shr 16)
        writeByte(value shr 24)
    }

    fun writeLong(value: Long) {
        writeByte((value shr 56).toInt())
        writeByte((value shr 48).toInt())
        writeByte((value shr 40).toInt())
        writeByte((value shr 32).toInt())
        writeByte((value shr 24).toInt())
        writeByte((value shr 16).toInt())
        writeByte((value shr 8).toInt())
        writeByte(value.toInt())
    }

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

    fun writeCharString(value: String?) {
        if (value != null) {
            for (char in value) {
                writeChar(char)
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

    fun writeChar(value: Char) = writeByte(Unicode.charToByte(value))

    fun writeBytes(value: ByteArray)

    fun writeBytes(data: ByteArray, offset: Int, length: Int)

    fun writeBytes(value: ShortArray)

    fun writeBytes(value: IntArray)

    fun writeBytes(value: LongArray)

    fun writeBytes(value: FloatArray)

    fun writeBytes(value: DoubleArray)

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

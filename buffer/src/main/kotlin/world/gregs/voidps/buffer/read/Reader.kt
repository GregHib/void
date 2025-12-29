package world.gregs.voidps.buffer.read

import world.gregs.voidps.buffer.Unicode

interface Reader {

    /**
     * Starting length of the packet
     */
    val length: Int

    val remaining: Int

    fun readBoolean() = readByte() == 1

    fun readBooleanAdd() = readByteAdd() == 1

    fun readBooleanInverse() = readByteInverse() == 1

    fun readBooleanSubtract() = readByteSubtract() == 1

    fun readUnsignedBoolean() = readUnsignedByte() == 1

    fun peek(): Int

    fun readByte(): Int

    fun readByteAdd(): Int = (readByte() - 128).toByte().toInt()

    fun readByteInverse(): Int = -readByte()

    fun readByteSubtract(): Int = (readByteInverse() + 128).toByte().toInt()

    fun readUnsignedByte(): Int = readByte() and 0xff

    fun readShort(): Int = (readByte() shl 8) or readUnsignedByte()

    fun readShortAdd(): Int = (readByte() shl 8) or readUnsignedByteAdd()

    fun readUnsignedShortAdd(): Int = (readByte() shl 8) or ((readByte() - 128) and 0xff)

    fun readShortLittle(): Int = readUnsignedByte() or (readByte() shl 8)

    fun readShortAddLittle(): Int = readUnsignedByteAdd() or (readByte() shl 8)

    fun readUnsignedByteAdd(): Int = (readByte() - 128).toByte().toInt()

    fun readUnsignedShort(): Int = (readUnsignedByte() shl 8) or readUnsignedByte()

    fun readUnsignedShortLittle(): Int = readUnsignedByte() or (readUnsignedByte() shl 8)

    fun readMedium(): Int = (readByte() shl 16) or (readByte() shl 8) or readUnsignedByte()

    fun readUnsignedMedium(): Int = (readUnsignedByte() shl 16) or (readUnsignedByte() shl 8) or readUnsignedByte()

    fun readInt(): Int = (readUnsignedByte() shl 24) or (readUnsignedByte() shl 16) or (readUnsignedByte() shl 8) or readUnsignedByte()

    fun readIntInverseMiddle(): Int = (readByte() shl 16) or (readByte() shl 24) or readUnsignedByte() or (readByte() shl 8)

    fun readIntLittle(): Int = readUnsignedByte() or (readByte() shl 8) or (readByte() shl 16) or (readByte() shl 24)

    fun readUnsignedIntMiddle(): Int = (readUnsignedByte() shl 8) or readUnsignedByte() or (readUnsignedByte() shl 24) or (readUnsignedByte() shl 16)

    fun readSmart(): Int {
        val peek = readUnsignedByte()
        return if (peek < 128) {
            peek
        } else {
            (peek shl 8 or readUnsignedByte()) - 32768
        }
    }

    fun readBigSmart(): Int {
        val peek = readByte()
        return if (peek < 0) {
            ((peek shl 24) or (readUnsignedByte() shl 16) or (readUnsignedByte() shl 8) or readUnsignedByte()) and 0x7fffffff
        } else {
            val value = (peek shl 8) or readUnsignedByte()
            if (value == 32767) -1 else value
        }
    }

    fun readLargeSmart(): Int {
        var baseValue = 0
        var lastValue = readSmart()
        while (lastValue == 32767) {
            lastValue = readSmart()
            baseValue += 32767
        }
        return baseValue + lastValue
    }

    fun readLong(): Long {
        val first = readInt().toLong() and 0xffffffffL
        val second = readInt().toLong() and 0xffffffffL
        return second + (first shl 32)
    }

    fun readString(): String

    fun readCharString(): String {
        val start = position()
        var pos = start
        while (array()[pos] != 0.toByte()) {
            pos++
        }
        val length = pos - start
        val string = String(CharArray(length) { readChar().toChar() })
        skip(1)
        return string
    }

    fun readChar(): Int = Unicode.byteToChar(readUnsignedByte())

    /**
     * Reads all bytes into [ByteArray]
     * @param value The array to be written to.
     */
    fun readBytes(value: ByteArray)

    /**
     * Reads all big endian shorts into [ShortArray]
     * @param value The array to be written to.
     */
    fun readBytes(value: ShortArray)

    /**
     * Reads all big endian ints into [IntArray]
     * @param value The array to be written to.
     */
    fun readBytes(value: IntArray)

    /**
     * Reads all big endian longs into [LongArray]
     * @param value The array to be written to.
     */
    fun readBytes(value: LongArray)

    /**
     * Reads all floats into [FloatArray]
     * @param value The array to be written to.
     */
    fun readBytes(value: FloatArray)

    /**
     * Reads all doubles into [DoubleArray]
     * @param value The array to be written to.
     */
    fun readBytes(value: DoubleArray)

    /**
     * Reads [length] number of bytes starting at [offset] to [array].
     * @param array The [ByteArray] to be written to
     * @param offset Destination index
     * @param length Number of bytes to read
     */
    fun readBytes(array: ByteArray, offset: Int, length: Int = array.size)

    /**
     * Skips the [amount] bytes.
     * @param amount Number of bytes to skip
     */
    fun skip(amount: Int)

    fun position(): Int

    fun array(): ByteArray

    fun position(index: Int)

    /**
     * Returns the remaining number of readable bytes.
     * @return [Int]
     */
    fun readableBytes(): Int

    /**
     * Enables individual decoded byte writing aka 'bit access'
     */
    fun startBitAccess(): Reader

    /**
     * Disables 'bit access' mode
     */
    fun stopBitAccess(): Reader

    /**
     * Writes a bit during 'bit access'
     * @param bitCount number of bits to be written
     */
    fun readBits(bitCount: Int): Int

}

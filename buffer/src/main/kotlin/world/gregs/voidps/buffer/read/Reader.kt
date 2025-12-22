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

    fun readByteAdd(): Int

    fun readByteInverse(): Int

    fun readByteSubtract(): Int

    fun readUnsignedByte(): Int

    fun readUnsignedByteAdd(): Int

    fun readShort(): Int

    fun readShortAdd(): Int

    fun readShortLittle(): Int

    fun readShortAddLittle(): Int

    fun readUnsignedShort(): Int

    fun readUnsignedShortLittle(): Int

    fun readUnsignedShortAdd(): Int

    fun readMedium(): Int

    fun readUnsignedMedium(): Int

    fun readInt(): Int

    fun readIntInverseMiddle(): Int

    fun readIntLittle(): Int

    fun readUnsignedIntMiddle(): Int

    fun readSmart(): Int

    fun readBigSmart(): Int

    fun readLargeSmart(): Int

    fun readLong(): Long

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

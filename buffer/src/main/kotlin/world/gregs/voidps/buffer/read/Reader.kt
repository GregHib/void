package world.gregs.voidps.buffer.read

interface Reader {

    /**
     * Starting length of the packet
     */
    val length: Int

    fun readBoolean() = readByte() == 1

    fun readBooleanAdd() = readByteAdd() == 1

    fun readBooleanInverse() = readByteInverse() == 1

    fun readBooleanSubtract() = readByteSubtract() == 1

    fun readUnsignedBoolean() = readUnsignedByte() == 1

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

    /**
     * Reads all bytes into [ByteArray]
     * @param value The array to be written to.
     */
    fun readBytes(value: ByteArray)

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
     * Disables 'bit access'
     */
    fun finishBitAccess(): Reader

    /**
     * Writes a bit during 'bit access'
     * @param bitCount number of bits to be written
     * @param value bit value to be set
     */
    fun readBits(bitCount: Int): Int
}
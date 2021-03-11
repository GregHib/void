package world.gregs.voidps.buffer.read

import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier

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

    fun readByteAdd(): Int = readSigned(DataType.BYTE, Modifier.ADD).toInt()

    fun readByteInverse(): Int = readSigned(DataType.BYTE, Modifier.INVERSE).toInt()

    fun readByteSubtract(): Int = readSigned(DataType.BYTE, Modifier.SUBTRACT).toInt()

    fun readUnsignedByte(): Int = readUnsigned(DataType.BYTE).toInt()

    fun readUnsignedByteAdd(): Int = readUnsigned(DataType.BYTE, Modifier.ADD).toInt()

    fun readShort(): Int = readSigned(DataType.SHORT).toInt()

    fun readShortAdd(): Int = readSigned(DataType.SHORT, Modifier.ADD).toInt()

    fun readUnsignedShortAdd(): Int = readUnsigned(DataType.SHORT, Modifier.ADD).toInt()

    fun readShortLittle(): Int = readSigned(DataType.SHORT, Modifier.NONE, Endian.LITTLE).toInt()

    fun readShortAddLittle(): Int = readSigned(DataType.SHORT, Modifier.ADD, Endian.LITTLE).toInt()

    fun readUnsignedShort(): Int = readUnsigned(DataType.SHORT).toInt()

    fun readUnsignedShortLittle(): Int = readUnsigned(DataType.SHORT, Modifier.NONE, Endian.LITTLE).toInt()

    fun readUnsignedShortAddLittle(): Int = readUnsigned(DataType.SHORT, Modifier.ADD, Endian.LITTLE).toInt()

    fun readMedium(): Int = readSigned(DataType.MEDIUM).toInt()

    fun readUnsignedMedium(): Int = readUnsigned(DataType.MEDIUM).toInt()

    fun readInt(): Int = readSigned(DataType.INT).toInt()

    fun readIntInverseMiddle(): Int = readSigned(DataType.INT, Modifier.INVERSE, Endian.MIDDLE).toInt()

    fun readIntLittle(): Int = readSigned(DataType.INT, Modifier.NONE, Endian.LITTLE).toInt()

    fun readUnsignedIntMiddle(): Int = readUnsigned(DataType.INT, Modifier.NONE, Endian.MIDDLE).toInt()

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
     * Reads [length] number of bytes with [type] and [order]
     * @param type The byte type to read
     * @param modifier The first byte read modifier
     * @param order The endianness
     * @return The positive or negative read value
     */
    fun readSigned(type: DataType, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Long

    /**
     * Reads [length] number of bytes with [type] and [order]
     * @param type The byte type to read
     * @param modifier The first byte read modifier
     * @param order The endianness
     * @return The positive read value
     */
    fun readUnsigned(type: DataType, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG): Long

    /**
     * Writes a bit during 'bit access'
     * @param bitCount number of bits to be written
     * @param value bit value to be set
     */
    fun readBits(bitCount: Int): Int
}
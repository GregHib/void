package rs.dusk.core.io.read

import rs.dusk.core.io.DataType
import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier
import java.nio.ByteBuffer

/**
 * A unit of byte data
 */
interface Reader {

    /**
     * Packet buffer
     */
    val buffer: ByteBuffer

    /**
     * Starting length of the packet
     */
    val length: Int

    /**
     * Reads a boolean.
     * @param type The variable read type
     * @return [Boolean]
     */
    fun readBoolean(type: Modifier = Modifier.NONE) = readByte(type) == 1

    /**
     * Reads a boolean.
     * @return [Boolean]
     */
    fun readUnsignedBoolean() = readUnsignedByte() == 1

    /**
     * Reads a byte.
     * @param type The variable read type
     * @return [Byte]
     */
    fun readByte(type: Modifier = Modifier.NONE) = readSigned(DataType.BYTE, type).toInt()

    /**
     * Reads an unsigned byte.
     * @return [Short]
     */
    fun readUnsignedByte() = readUnsigned(DataType.BYTE).toInt()

    /**
     * Reads a short.
     * @param type The variable read type
     * @param order The read order
     * @return [Short]
     */
    fun readShort(type: Modifier = Modifier.NONE, order: Endian = Endian.BIG) =
            readSigned(DataType.SHORT, type, order).toInt()

    /**
     * Reads an unsigned short.
     * @return [Int]
     */
    fun readUnsignedShort() = readUnsigned(DataType.SHORT).toInt()

    /**
     * Reads a 3-byte integer.
     * @return [Int]
     */
    fun readMedium() = readSigned(DataType.MEDIUM).toInt()

    /**
     * Reads a 3-byte integer.
     * @return [Int]
     */
    fun readUnsignedMedium() = readUnsigned(DataType.MEDIUM).toInt()

    /**
     * Reads a integer.
     * @param type The variable read type
     * @param order The read order
     * @return [Int]
     */
    fun readInt(type: Modifier = Modifier.NONE, order: Endian = Endian.BIG) =
            readSigned(DataType.INT, type, order).toInt()

    /**
     * Reads a smart
     * @return [Int]
     */
    fun readSmart(): Int

    /**
     * Reads a smart
     * @return [Int]
     */
    fun readBigSmart(): Int

    /**
     * Reads a smart
     * @return [Int]
     */
    fun readLargeSmart(): Int

    /**
     * Reads a long.
     * @return [Long]
     */
    fun readLong(): Long

    /**
     * Reads a string.
     * @return [String]
     */
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

    /**
     * Returns the remaining number of readable bytes.
     * @return [Int]
     */
    fun readableBytes(): Int

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
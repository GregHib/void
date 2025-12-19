package world.gregs.voidps.buffer.write

import java.nio.ByteBuffer

/**
 * All functions relative to writing directly to a packet are done by this class
 */
class ArrayWriter(
    capacity: Int = 64,
    private val buffer: ByteArray = ByteArray(capacity),
) : Writer {

    private var bitIndex = -1
    private var position = 0

    override fun writeByte(value: Int) {
        buffer[position++] = value.toByte()
    }

    override fun writeByteAdd(value: Int) {
        writeByte(value + 128)
    }

    override fun writeByteInverse(value: Int) {
        writeByte(-value)
    }

    override fun writeByteSubtract(value: Int) {
        writeByte(-value + 128)
    }

    override fun setByte(index: Int, value: Int) {
        buffer[index] = value.toByte()
    }

    override fun writeShort(value: Int) {
        writeByte(value shr 8)
        writeByte(value)
    }

    override fun writeShortAdd(value: Int) {
        writeByte(value shr 8)
        writeByteAdd(value)
    }

    override fun writeShortLittle(value: Int) {
        writeByte(value)
        writeByte(value shr 8)
    }

    override fun writeShortAddLittle(value: Int) {
        writeByteAdd(value)
        writeByte(value shr 8)
    }

    override fun writeMedium(value: Int) {
        writeByte(value shr 16)
        writeByte(value shr 8)
        writeByte(value)
    }

    override fun writeInt(value: Int) {
        writeByte(value shr 24)
        writeByte(value shr 16)
        writeByte(value shr 8)
        writeByte(value)
    }

    override fun writeIntMiddle(value: Int) {
        writeByte(value shr 8)
        writeByte(value)
        writeByte(value shr 24)
        writeByte(value shr 16)
    }

    override fun writeIntInverse(value: Int) {
        writeByte(value shr 8)
        writeByte(value shr 24)
        writeByte(value shr 16)
        writeByteInverse(value)
    }

    override fun writeIntInverseMiddle(value: Int) {
        writeByte(value shr 16)
        writeByte(value shr 24)
        writeByte(value)
        writeByte(value shr 8)
    }

    override fun writeIntLittle(value: Int) {
        writeByte(value)
        writeByte(value shr 8)
        writeByte(value shr 16)
        writeByte(value shr 24)
    }

    override fun writeIntInverseLittle(value: Int) {
        writeByteInverse(value)
        writeByte(value shr 8)
        writeByte(value shr 16)
        writeByte(value shr 24)
    }

    override fun writeLong(value: Long) {
        writeByte((value shr 56).toInt())
        writeByte((value shr 48).toInt())
        writeByte((value shr 40).toInt())
        writeByte((value shr 32).toInt())
        writeByte((value shr 24).toInt())
        writeByte((value shr 16).toInt())
        writeByte((value shr 8).toInt())
        writeByte(value.toInt())
    }

    override fun writeBytes(value: ByteArray) {
        System.arraycopy(value, 0, buffer, position, value.size)
        position += value.size
    }

    override fun writeBytes(value: ShortArray) {
        ByteBuffer.wrap(buffer, position, value.size * 2)
            .asShortBuffer()
            .put(value)
        position += value.size * 2
    }

    override fun writeBytes(value: IntArray) {
        ByteBuffer.wrap(buffer, position, value.size * 4)
            .asIntBuffer()
            .put(value)
        position += value.size * 4
    }

    override fun writeBytes(data: ByteArray, offset: Int, length: Int) {
        ByteBuffer.wrap(buffer, position, length)
            .put(data, offset, length)
        position += length
    }

    override fun startBitAccess() {
        bitIndex = position * 8
    }

    override fun stopBitAccess() {
        position = position()
        bitIndex = -1
    }

    override fun writeBits(bitCount: Int, value: Boolean) {
        writeBits(bitCount, if (value) 1 else 0)
    }

    override fun writeBits(bitCount: Int, value: Int) {
        var numBits = bitCount

        var byteIndex = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        bitIndex += numBits

        var tmp: Int
        var max: Int
        while (numBits > bitOffset) {
            tmp = buffer[byteIndex].toInt()
            max = BIT_MASKS[bitOffset]
            tmp = tmp and max.inv() or (value shr numBits - bitOffset and max)
            setByte(byteIndex++, tmp)
            numBits -= bitOffset
            bitOffset = 8
        }

        tmp = buffer[byteIndex].toInt()
        max = BIT_MASKS[numBits]
        if (numBits == bitOffset) {
            tmp = tmp and max.inv() or (value and max)
        } else {
            tmp = tmp and (max shl bitOffset - numBits).inv()
            tmp = tmp or (value and max shl bitOffset - numBits)
        }
        setByte(byteIndex, tmp)
    }

    override fun bitIndex(): Int = bitIndex

    override fun bitIndex(index: Int) {
        bitIndex = index
    }

    override fun position(): Int {
        return if (bitIndex != -1) {
            (bitIndex + 7) / 8
        } else {
            return position
        }
    }

    override fun position(index: Int) {
        position = index
    }

    override fun toArray(): ByteArray {
        val data = ByteArray(position())
        System.arraycopy(buffer, 0, data, 0, data.size)
        return data
    }

    override fun array(): ByteArray = buffer

    override fun clear() {
        buffer.fill(0)
    }

    override fun remaining(): Int = buffer.size - position

    companion object {
        val BIT_MASKS = IntArray(32) { (1 shl it) - 1 }
    }
}

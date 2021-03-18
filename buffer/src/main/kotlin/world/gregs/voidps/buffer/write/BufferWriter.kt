package world.gregs.voidps.buffer.write

import java.nio.ByteBuffer

/**
 * All functions relative to writing directly to a packet are done by this class
 *
 * @author GregHib <greg@gregs.world>
 * @since February 18, 2020
 */
class BufferWriter(
    capacity: Int = 64,
    private val buffer: ByteBuffer = ByteBuffer.allocate(capacity)
) : Writer {

    private var bitIndex = 0

    override fun writeByte(value: Int) {
        buffer.put(value.toByte())
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
        buffer.put(index, value.toByte())
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
        buffer.put(value)
    }

    override fun writeBytes(data: ByteArray, offset: Int, length: Int) {
        buffer.put(data, offset, length)
    }

    override fun startBitAccess() {
        bitIndex = buffer.position() * 8
    }

    override fun finishBitAccess() {
        buffer.position((bitIndex + 7) / 8)
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
            tmp = buffer.get(byteIndex).toInt()
            max = BIT_MASKS[bitOffset]
            tmp = tmp and max.inv() or (value shr numBits - bitOffset and max)
            buffer.put(byteIndex++, tmp.toByte())
            numBits -= bitOffset
            bitOffset = 8
        }

        tmp = buffer.get(byteIndex).toInt()
        max = BIT_MASKS[numBits]
        if (numBits == bitOffset) {
            tmp = tmp and max.inv() or (value and max)
        } else {
            tmp = tmp and (max shl bitOffset - numBits).inv()
            tmp = tmp or (value and max shl bitOffset - numBits)
        }
        buffer.put(byteIndex, tmp.toByte())
    }

    override fun position(): Int {
        return buffer.position()
    }

    override fun position(index: Int) {
        buffer.position(index)
    }

    override fun toArray(): ByteArray {
        val data = ByteArray(position())
        System.arraycopy(buffer.array(), 0, data, 0, data.size)
        return data
    }

    override fun array(): ByteArray {
        return buffer.array()
    }

    override fun clear() {
        buffer.clear()
    }

    override fun remaining(): Int {
        return buffer.remaining()
    }

    companion object {
        val BIT_MASKS = IntArray(32)

        init {
            for (i in BIT_MASKS.indices) {
                BIT_MASKS[i] = (1 shl i) - 1
            }
        }
    }
}
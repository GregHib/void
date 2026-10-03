package world.gregs.voidps.buffer.write

import java.nio.ByteBuffer

/**
 * All functions relative to writing directly to a packet are done by this class
 */
class BufferWriter(
    capacity: Int = 64,
    private val buffer: ByteBuffer = ByteBuffer.allocate(capacity),
) : Writer {

    private var bitIndex = -1

    override fun writeByte(value: Int) {
        buffer.put(value.toByte())
    }

    override fun setByte(index: Int, value: Int) {
        buffer.put(index, value.toByte())
    }

    override fun writeBytes(value: ByteArray) {
        buffer.put(value)
    }

    override fun writeBytes(data: ByteArray, offset: Int, length: Int) {
        buffer.put(data, offset, length)
    }

    override fun writeBytes(value: ShortArray) {
        buffer
            .asShortBuffer()
            .put(value)
        position(position() + value.size * 2)
    }

    override fun writeBytes(value: IntArray) {
        buffer
            .asIntBuffer()
            .put(value)
        position(position() + value.size * 4)
    }

    override fun writeBytes(value: LongArray) {
        buffer
            .asLongBuffer()
            .put(value)
        position(position() + value.size * 8)
    }

    override fun writeBytes(value: FloatArray) {
        buffer
            .asFloatBuffer()
            .put(value)
        position(position() + value.size * 4)
    }

    override fun writeBytes(value: DoubleArray) {
        buffer
            .asDoubleBuffer()
            .put(value)
        position(position() + value.size * 8)
    }

    override fun startBitAccess() {
        bitIndex = buffer.position() * 8
    }

    override fun stopBitAccess() {
        buffer.position(position())
        bitIndex = -1
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

    override fun bitIndex(): Int = bitIndex

    override fun bitIndex(index: Int) {
        bitIndex = index
    }

    override fun position(): Int = if (bitIndex != -1) {
        (bitIndex + 7) / 8
    } else {
        buffer.position()
    }

    override fun position(index: Int) {
        buffer.position(index)
    }

    override fun toArray(): ByteArray {
        val data = ByteArray(position())
        System.arraycopy(buffer.array(), 0, data, 0, data.size)
        return data
    }

    override fun array(): ByteArray = buffer.array()

    override fun clear() {
        buffer.clear()
    }

    override fun remaining(): Int = buffer.remaining()

    companion object {
        val BIT_MASKS = IntArray(32) { (1 shl it) - 1 }
    }
}

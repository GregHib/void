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

    override fun setByte(index: Int, value: Int) {
        buffer[index] = value.toByte()
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

    override fun writeBytes(value: LongArray) {
        ByteBuffer.wrap(buffer, position, value.size * 8)
            .asLongBuffer()
            .put(value)
        position += value.size * 8
    }

    override fun writeBytes(value: FloatArray) {
        ByteBuffer.wrap(buffer, position, value.size * 4)
            .asFloatBuffer()
            .put(value)
        position += value.size * 4
    }

    override fun writeBytes(value: DoubleArray) {
        ByteBuffer.wrap(buffer, position, value.size * 8)
            .asDoubleBuffer()
            .put(value)
        position += value.size * 8
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
            position
        }
    }

    override fun position(index: Int) {
        position = index
    }
    fun cut(start: Int, size: Int): ArrayWriter {
        require(size >= 0) { "Size must be non-negative" }
        require(start >= 0 && start + size <= buffer.size) { "Cut range out of bounds" }

        val copy = ByteArray(buffer.size - size)
        System.arraycopy(buffer, 0, copy, 0, start)
        System.arraycopy(buffer, start + size, copy, start, buffer.size - start - size)
        return ArrayWriter(buffer = copy)
    }

//    fun cut(start: Int, end: Int): ArrayWriter {
//        require(start <= end) { "Start position can't exceed end position" }
//        val copy = ByteArray(buffer.size - (end - start))
//        System.arraycopy(buffer, 0, copy, 0, start)
//        System.arraycopy(buffer, end, copy, start, buffer.size - end)
//        return ArrayWriter(buffer = copy)
//    }

    fun insert(start: Int, size: Int): ArrayWriter {
        val copy = ByteArray(buffer.size + size)
        System.arraycopy(buffer, 0, copy, 0, start)
        System.arraycopy(buffer, start, copy, start + size, buffer.size - start)
        return ArrayWriter(buffer = copy)
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

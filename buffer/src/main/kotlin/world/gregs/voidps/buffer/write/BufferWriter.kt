package world.gregs.voidps.buffer.write

import io.netty.buffer.ByteBuf
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

    override fun writeByte(value: Int): Writer {
        buffer.put(value.toByte())
        return this
    }

    override fun writeByteAdd(value: Int): Writer {
        return writeByte(value + 128)
    }

    override fun writeByteInverse(value: Int): Writer {
        return writeByte(-value)
    }

    override fun writeByteSubtract(value: Int): Writer {
        return writeByte(-value + 128)
    }

    override fun setByte(index: Int, value: Int): Writer {
        buffer.put(index, value.toByte())
        return this
    }

    override fun writeShort(value: Int): Writer {
        writeByte(value shr 8)
        writeByte(value)
        return this
    }

    override fun writeShortAdd(value: Int): Writer {
        writeByte(value shr 8)
        writeByteAdd(value)
        return this
    }

    override fun writeShortLittle(value: Int): Writer {
        writeByte(value)
        writeByte(value shr 8)
        return this
    }

    override fun writeShortAddLittle(value: Int): Writer {
        writeByteAdd(value)
        writeByte(value shr 8)
        return this
    }

    override fun writeMedium(value: Int): Writer {
        writeByte(value shr 16)
        writeByte(value shr 8)
        writeByte(value)
        return this
    }

    override fun writeInt(value: Int): Writer {
        writeByte(value shr 24)
        writeByte(value shr 16)
        writeByte(value shr 8)
        writeByte(value)
        return this
    }

    override fun writeIntMiddle(value: Int): Writer {
        writeByte(value shr 8)
        writeByte(value)
        writeByte(value shr 24)
        writeByte(value shr 16)
        return this
    }

    override fun writeIntInverse(value: Int): Writer {
        writeByte(value shr 8)
        writeByte(value shr 24)
        writeByte(value shr 16)
        writeByteInverse(value)
        return this
    }

    override fun writeIntInverseMiddle(value: Int): Writer {
        writeByte(value shr 16)
        writeByte(value shr 24)
        writeByte(value)
        writeByte(value shr 8)
        return this
    }

    override fun writeIntLittle(value: Int): Writer {
        writeByte(value)
        writeByte(value shr 8)
        writeByte(value shr 16)
        writeByte(value shr 24)
        return this
    }

    override fun writeIntInverseLittle(value: Int): Writer {
        writeByteInverse(value)
        writeByte(value shr 8)
        writeByte(value shr 16)
        writeByte(value shr 24)
        return this
    }

    override fun writeLong(value: Long): Writer {
        writeByte((value shr 56).toInt())
        writeByte((value shr 48).toInt())
        writeByte((value shr 40).toInt())
        writeByte((value shr 32).toInt())
        writeByte((value shr 24).toInt())
        writeByte((value shr 16).toInt())
        writeByte((value shr 8).toInt())
        writeByte(value.toInt())
        return this
    }

    override fun writeBytes(value: ByteArray): BufferWriter {
        buffer.put(value)
        return this
    }

    override fun writeBytes(value: ByteBuf): BufferWriter {
        return writeBytes(value.array(), value.readerIndex(), value.readableBytes())
    }

    override fun writeBytes(data: ByteArray, offset: Int, length: Int): BufferWriter {
        buffer.put(data, offset, length)
        return this
    }

    override fun writeBytes(data: ByteBuf, offset: Int, length: Int): BufferWriter {
        return writeBytes(data.array(), offset, length)
    }

    override fun startBitAccess(): BufferWriter {
        bitIndex = buffer.position() * 8
        return this
    }

    override fun finishBitAccess(): BufferWriter {
        buffer.position((bitIndex + 7) / 8)
        return this
    }

    override fun writeBits(bitCount: Int, value: Boolean): BufferWriter {
        return writeBits(bitCount, if (value) 1 else 0)
    }

    override fun writeBits(bitCount: Int, value: Int): BufferWriter {
        var numBits = bitCount

        var byteIndex = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        bitIndex += numBits

//        buffer.ensureWritable((byteIndex - buffer.writerIndex() + 1) + (numBits + 7) / 8)

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
        return this
    }

    override fun skip(position: Int): BufferWriter {
        for (i in 0 until position) {
            writeByte(0)
        }
        return this
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
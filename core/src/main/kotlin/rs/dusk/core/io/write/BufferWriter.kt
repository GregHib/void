package rs.dusk.core.io.write

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import rs.dusk.core.io.DataType
import rs.dusk.core.io.Endian
import rs.dusk.core.io.Modifier

/**
 * All functions relative to writing directly to a packet are done by this class
 *
 * @author Greg Hibberd <greg@greghibberd.com>
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
open class BufferWriter(
        val buffer: ByteBuf = Unpooled.buffer()
) : Writer {
    private var bitIndex = 0

    override fun writeBytes(value: ByteArray): BufferWriter {
        buffer.writeBytes(value)
        return this
    }

    override fun writeBytes(value: ByteBuf): BufferWriter {
        buffer.writeBytes(value)
        return this
    }

    override fun writeBytes(data: ByteArray, offset: Int, length: Int): BufferWriter {
        buffer.writeBytes(data, offset, length)
        return this
    }

    override fun writeBytes(data: ByteBuf, offset: Int, length: Int): BufferWriter {
        buffer.writeBytes(data, offset, length)
        return this
    }


    override fun startBitAccess(): BufferWriter {
        bitIndex = buffer.writerIndex() * 8
        return this
    }

    override fun finishBitAccess(): BufferWriter {
        buffer.writerIndex((bitIndex + 7) / 8)
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

        buffer.ensureWritable((byteIndex - buffer.writerIndex() + 1) + (numBits + 7) / 8)

        var tmp: Int
        var max: Int
        while (numBits > bitOffset) {
            tmp = buffer.getByte(byteIndex).toInt()
            max = BIT_MASKS[bitOffset]
            tmp = tmp and max.inv() or (value shr numBits - bitOffset and max)
            buffer.setByte(byteIndex++, tmp)
            numBits -= bitOffset
            bitOffset = 8
        }

        tmp = buffer.getByte(byteIndex).toInt()
        max = BIT_MASKS[numBits]
        if (numBits == bitOffset) {
            tmp = tmp and max.inv() or (value and max)
        } else {
            tmp = tmp and (max shl bitOffset - numBits).inv()
            tmp = tmp or (value and max shl bitOffset - numBits)
        }
        buffer.setByte(byteIndex, tmp)
        return this
    }

    override fun skip(position: Int): BufferWriter {
        for (i in 0 until position) {
            writeByte(0)
        }
        return this
    }

    override fun position(): Int {
        return buffer.writerIndex()
    }

    override fun position(index: Int) {
        buffer.writerIndex(index)
    }

    override fun write(type: DataType, value: Number, modifier: Modifier, order: Endian) {
        if (order == Endian.MIDDLE) {
            check(modifier == Modifier.NONE || modifier == Modifier.INVERSE) {
                "Middle endian doesn't support variable modifier $modifier"
            }
            check(type == DataType.INT) {
                "Middle endian can only be used with an integer"
            }
        }

        for (index in order.getRange(modifier, type.byteCount)) {
            val modifiedValue = when (if (index == 0 && order != Endian.MIDDLE) modifier else Modifier.NONE) {
                Modifier.ADD -> value.toInt() + 128
                Modifier.INVERSE -> -value.toInt()
                Modifier.SUBTRACT -> 128 - value.toInt()
                else -> (value.toLong() shr index * 8).toInt()
            }
            buffer.writeByte(modifiedValue)
        }
    }

    override fun toArray(): ByteArray {
        val data = ByteArray(position())
        System.arraycopy(buffer.array(), 0, data, 0, data.size)
        return data
    }

    companion object {
        private val BIT_MASKS = IntArray(32)

        init {
            for (i in BIT_MASKS.indices) {
                BIT_MASKS[i] = (1 shl i) - 1
            }
        }
    }
}
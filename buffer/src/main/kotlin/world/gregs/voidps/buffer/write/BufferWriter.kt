package world.gregs.voidps.buffer.write

import io.netty.buffer.ByteBuf
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import java.nio.ByteBuffer

/**
 * All functions relative to writing directly to a packet are done by this class
 *
 * @author GregHib <greg@gregs.world>
 * @since February 18, 2020
 */
open class BufferWriter(
    capacity: Int = 64,
    val buffer: ByteBuffer = ByteBuffer.allocate(capacity)
) : Writer {
    private var bitIndex = 0

    override fun setByte(index: Int, value: Int): Writer {
        buffer.put(index, value.toByte())
        return this
    }

    override fun writeBytes(value: ByteArray): BufferWriter {
        buffer.put(value)
        return this
    }

    override fun writeBytes(value: ByteBuf): BufferWriter {
        buffer.put(value.array(), value.readerIndex(), value.readableBytes())
        return this
    }

    override fun writeBytes(data: ByteArray, offset: Int, length: Int): BufferWriter {
        buffer.put(data, offset, length)
        return this
    }

    override fun writeBytes(data: ByteBuf, offset: Int, length: Int): BufferWriter {
        buffer.put(data.array(), offset, length)
        return this
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
            buffer.put(modifiedValue.toByte())
        }
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
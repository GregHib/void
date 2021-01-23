package world.gregs.voidps.buffer.write

import io.netty.buffer.ByteBuf
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.write.BufferWriter.Companion.BIT_MASKS

/**
 * Writes a byte.
 * @param value [Int]
 */
fun ByteBuf.writeByte(value: Int, type: Modifier = Modifier.NONE) {
    write(DataType.BYTE, value, type)
}

/**
 * Writes a [Short].
 * @param value [Int]
 */
fun ByteBuf.writeShort(value: Int, type: Modifier = Modifier.NONE, order: Endian = Endian.BIG) {
    write(DataType.SHORT, value, type, order)
}

/**
 * Writes a [Int].
 * @param value [Int]
 */
fun ByteBuf.writeInt(value: Int, type: Modifier = Modifier.NONE, order: Endian = Endian.BIG) {
    write(DataType.INT, value, type, order)
}

/**
 * Writes a boolean as a byte.
 * @param value [Boolean]
 */
fun ByteBuf.writeByte(value: Boolean, type: Modifier = Modifier.NONE) {
    writeByte(if (value) 1 else 0, type)
}

fun ByteBuf.writeSmart(value: Int) {
    if (value >= 128) {
        writeShort(value + 32768)
    } else {
        writeByte(value)
    }
}

fun ByteBuf.writeString(value: String?) {
    if (value != null) {
        writeBytes(value.toByteArray())
    }
    writeByte(0)
}

fun ByteBuf.writePrefixedString(value: String) {
    writeByte(0)
    writeBytes(value.toByteArray())
    writeByte(0)
}

fun ByteBuf.startBitAccess(): Int {
    return writerIndex() * 8
}

fun ByteBuf.finishBitAccess(bitIndex: Int) {
    writerIndex((bitIndex + 7) / 8)
}

fun ByteBuf.writeBits(index: Int, bitCount: Int, value: Boolean): Int {
    return writeBits(index, bitCount, if (value) 1 else 0)
}

fun ByteBuf.writeBits(index: Int, bitCount: Int, value: Int): Int {
    var bitIndex = index
    var numBits = bitCount

    var byteIndex = bitIndex shr 3
    var bitOffset = 8 - (bitIndex and 7)
    bitIndex += numBits

    ensureWritable((byteIndex - writerIndex() + 1) + (numBits + 7) / 8)

    var tmp: Int
    var max: Int
    while (numBits > bitOffset) {
        tmp = getByte(byteIndex).toInt()
        max = BIT_MASKS[bitOffset]
        tmp = tmp and max.inv() or (value shr numBits - bitOffset and max)
        setByte(byteIndex++, tmp)
        numBits -= bitOffset
        bitOffset = 8
    }

    tmp = getByte(byteIndex).toInt()
    max = BIT_MASKS[numBits]
    if (numBits == bitOffset) {
        tmp = tmp and max.inv() or (value and max)
    } else {
        tmp = tmp and (max shl bitOffset - numBits).inv()
        tmp = tmp or (value and max shl bitOffset - numBits)
    }
    setByte(byteIndex, tmp)
    return bitCount
}

fun ByteBuf.write(type: DataType, value: Number, modifier: Modifier = Modifier.NONE, order: Endian = Endian.BIG) {
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
        writeByte(modifiedValue)
    }
}

/**
 * For debugging only
 */
fun ByteBuf.toArray(): ByteArray {
    val data = ByteArray(writerIndex())
    System.arraycopy(array(), 0, data, 0, data.size)
    return data
}
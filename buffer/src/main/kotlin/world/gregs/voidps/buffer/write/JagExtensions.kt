package world.gregs.voidps.buffer.write

import io.ktor.utils.io.*

suspend fun ByteWriteChannel.writeByte(value: Int) = writeByte(value.toByte())

suspend fun ByteWriteChannel.writeByte(value: Boolean) = writeByte(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeShort(value: Int) = writeShort(value.toShort())

suspend fun ByteWriteChannel.writeShortAdd(value: Int) {
    writeByte(value shr 8)
    writeByteAdd(value)
}

suspend fun ByteWriteChannel.writeShortLittle(value: Int) {
    writeByte(value)
    writeByte(value shr 8)
}

suspend fun ByteWriteChannel.writeShortAddLittle(value: Int) {
    writeByteAdd(value)
    writeByte(value shr 8)
}

suspend fun ByteWriteChannel.writeIntMiddle(value: Int) {
    writeByte(value shr 8)
    writeByte(value)
    writeByte(value shr 24)
    writeByte(value shr 16)
}

suspend fun ByteWriteChannel.writeIntInverseMiddle(value: Int) {
    writeByte(value shr 16)
    writeByte(value shr 24)
    writeByte(value)
    writeByte(value shr 8)
}

suspend fun ByteWriteChannel.writeIntLittle(value: Int) {
    writeByte(value)
    writeByte(value shr 8)
    writeByte(value shr 16)
    writeByte(value shr 24)
}

suspend fun ByteWriteChannel.writeIntInverse(value: Int) {
    writeByte(value shr 8)
    writeByte(value shr 24)
    writeByte(value shr 16)
    writeByteInverse(value)
}

suspend fun ByteWriteChannel.writeMedium(value: Int) {
    writeByte(value shr 16)
    writeByte(value shr 8)
    writeByte(value)
}

suspend fun ByteWriteChannel.writeSmart(value: Int) {
    if (value >= 128) {
        writeShort(value + 32768)
    } else {
        writeByte(value)
    }
}

suspend fun ByteWriteChannel.writeByteAdd(value: Int) = writeByte(value + 128)

suspend fun ByteWriteChannel.writeByteAdd(value: Boolean) = writeByteAdd(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeByteInverse(value: Int) = writeByte(-value)

suspend fun ByteWriteChannel.writeBytes(value: ByteArray) = writeFully(value)

suspend fun ByteWriteChannel.writeByteInverse(value: Boolean) = writeByte(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeByteSubtract(value: Int) = writeByte(-value + 128)

suspend fun ByteWriteChannel.writeString(value: String?) {
    if (value != null) {
        writeFully(value.toByteArray())
    }
    writeByte(0)
}

class BitAccessor {
    private var bitIndex = 0
    private val data = ByteArray(4096 * 2)

    fun writeBit(value: Boolean) = writeBits(1, if (value) 1 else 0)

    fun writeBits(count: Int, value: Int) {
        var numBits = count

        var byteIndex = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        bitIndex += numBits

//        buffer.ensureWritable((byteIndex - buffer.writerIndex() + 1) + (numBits + 7) / 8)

        var tmp: Int
        var max: Int
        while (numBits > bitOffset) {
            tmp = data[byteIndex].toInt()
            max = BufferWriter.BIT_MASKS[bitOffset]
            tmp = tmp and max.inv() or (value shr numBits - bitOffset and max)
            data[byteIndex++] = tmp.toByte()
            numBits -= bitOffset
            bitOffset = 8
        }

        tmp = data[byteIndex].toInt()
        max = BufferWriter.BIT_MASKS[numBits]
        if (numBits == bitOffset) {
            tmp = tmp and max.inv() or (value and max)
        } else {
            tmp = tmp and (max shl bitOffset - numBits).inv()
            tmp = tmp or (value and max shl bitOffset - numBits)
        }
        data[byteIndex] = tmp.toByte()
    }

    suspend fun write(channel: ByteWriteChannel) {
        channel.writeFully(data, 0, (bitIndex + 7) / 8)
    }
}

suspend fun ByteWriteChannel.bitAccess(block: BitAccessor.() -> Unit) {
    val accessor = BitAccessor()
    block.invoke(accessor)
    accessor.write(this)
}
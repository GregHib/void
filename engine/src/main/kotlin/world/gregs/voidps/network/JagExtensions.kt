package world.gregs.voidps.network

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import world.gregs.voidps.buffer.write.BufferWriter
import kotlin.text.toByteArray

suspend fun ByteWriteChannel.writeByte(value: Boolean) = writeByte(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeByteAdd(value: Int) = writeByte(value + 128)

suspend fun ByteWriteChannel.writeByteAdd(value: Boolean) = writeByteAdd(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeByteInverse(value: Int) = writeByte(-value)

suspend fun ByteWriteChannel.writeBytes(value: ByteArray) = writeFully(value)

suspend fun ByteWriteChannel.writeByteInverse(value: Boolean) = writeByte(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeByteSubtract(value: Int) = writeByte(-value + 128)

suspend fun ByteWriteChannel.writeShortAdd(value: Int) {
    writeByte(value shr 8)
    writeByteAdd(value)
}

suspend fun ByteWriteChannel.writeShortLittle(value: Int) = writeShort(value.toShort(), ByteOrder.LITTLE_ENDIAN)

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

suspend fun ByteWriteChannel.writeIntLittle(value: Int) = writeInt(value, ByteOrder.LITTLE_ENDIAN)

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

fun ByteReadPacket.readString(): String {
    val sb = StringBuilder()
    var b: Int
    while (remaining > 0) {
        b = readByte().toInt()
        if (b == 0) {
            break
        }
        sb.append(b.toChar())
    }
    return sb.toString()
}

suspend fun ByteReadChannel.readUByte() = readByte().toInt() and 0xff


fun ByteReadPacket.readShortAdd(): Int = (readByte().toInt() shl 8) or readUnsignedByteAdd()

fun ByteReadPacket.readUnsignedByteAdd(): Int = (readByte() - 128).toByte().toInt()

fun ByteReadPacket.readUnsignedByte(): Int {
    return readByte().toInt() and 0xff
}

fun ByteReadPacket.readUnsignedIntMiddle(): Int {
    return (readUnsignedByte() shl 8) or readUnsignedByte() or (readUnsignedByte() shl 24) or (readUnsignedByte() shl 16)
}

fun ByteReadPacket.readBoolean(): Boolean = readByte().toInt() == 1

fun ByteReadPacket.readByteInverse(): Int = -readByte()

fun ByteReadPacket.readByteSubtract(): Int = (readByteInverse() + 128).toByte().toInt()

fun ByteReadPacket.readBooleanInverse() = readByteInverse() == 1

fun ByteReadPacket.readBooleanSubtract() = readByteSubtract() == 1

fun ByteReadPacket.readByteAdd(): Int = (readByte() - 128).toByte().toInt()

fun ByteReadPacket.readBooleanAdd() = readByteAdd() == 1

fun ByteReadPacket.readShortAddLittle(): Int = readUnsignedByteAdd() or (readByte().toInt() shl 8)

fun ByteReadPacket.readIntInverseMiddle(): Int = (readByte().toInt() shl 16) or (readByte().toInt() shl 24) or readUnsignedByte() or (readByte().toInt() shl 8)

fun ByteReadPacket.readUnsignedShortLittle(): Int = readUnsignedByte() or (readUnsignedByte() shl 8)

fun ByteReadPacket.readSmart(): Int {
    val peek = readUnsignedByte()
    return if (peek < 128) {
        peek and 0xFF
    } else {
        (peek shl 8 or readUnsignedByte()) - 32768
    }
}

fun ByteReadPacket.readUnsignedShortAdd(): Int = (readByte().toInt() shl 8) or ((readByte() - 128) and 0xff)
@file:OptIn(ExperimentalUnsignedTypes::class)

package world.gregs.voidps.network.login.protocol

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import world.gregs.voidps.buffer.write.BufferWriter
import kotlin.random.Random
import kotlin.text.toByteArray

suspend fun ByteReadChannel.readUByte(): Int = readByte().toInt() and 0xff

suspend fun ByteReadChannel.readUShort(): Int = (readUByte() shl 8) or readUByte()

suspend fun ByteReadChannel.readMedium(): Int {
    return (readByte().toInt() shl 16) + (readByte().toInt() shl 8) + readByte().toInt()
}

suspend fun ByteReadChannel.readUMedium(): Int {
    return (readUByte() shl 16) + (readUByte() shl 8) + readUByte()
}

suspend fun ByteWriteChannel.writeByte(value: Boolean) = writeByte(if (value) 1 else 0)

suspend fun ByteWriteChannel.p1Alt1(value: Boolean) = p1Alt1(if (value) 1 else 0)

suspend fun ByteWriteChannel.p1Alt2(value: Boolean) = p1Alt2(if (value) 1 else 0)

suspend fun ByteWriteChannel.p1Alt1(value: Int) = writeByte(value + 128)

suspend fun ByteWriteChannel.p1Alt2(value: Int) = writeByte(-value)

suspend fun ByteWriteChannel.p1Alt3(value: Int) = writeByte(-value + 128) // p1b_alt3

suspend fun ByteWriteChannel.writeBytes(value: ByteArray) = writeFully(value)

suspend fun ByteWriteChannel.p2Alt2(value: Int) {
    writeByte(value shr 8)
    p1Alt1(value)
}

suspend fun ByteWriteChannel.ip2(value: Int) = writeShort(value.toShort(), ByteOrder.LITTLE_ENDIAN)

suspend fun ByteWriteChannel.p2Alt3(value: Int) {
    p1Alt1(value)
    writeByte(value shr 8)
}

suspend fun ByteWriteChannel.writeIntMiddle(value: Int) {
    writeByte(value shr 8)
    writeByte(value)
    writeByte(value shr 24)
    writeByte(value shr 16)
}

suspend fun ByteWriteChannel.writeIntInverse(value: Int) {
    writeByte(value shr 8)
    writeByte(value shr 24)
    writeByte(value shr 16)
    p1Alt2(value)
}

suspend fun ByteWriteChannel.p4Alt1(value: Int) = writeInt(value, ByteOrder.LITTLE_ENDIAN)

suspend fun ByteWriteChannel.p4Alt3(value: Int) {
    writeByte(value shr 16)
    writeByte(value shr 24)
    writeByte(value)
    writeByte(value shr 8)
}

suspend fun ByteWriteChannel.writeMedium(value: Int) {
    writeByte(value shr 16)
    writeByte(value shr 8)
    writeByte(value)
}

suspend fun ByteWriteChannel.p3Alt1(value: Int) {
    writeByte(value shr 16)
    writeByte(value)
    writeByte(value shr 8)
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

suspend fun ByteWriteChannel.respond(value: Int) {
    writeByte(value)
    flush()
}

suspend fun ByteWriteChannel.finish(value: Int) {
    respond(value)
    close()
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

fun ByteReadPacket.readGjstr(): String {
    val bytes = buildList {
        while (true) {
            val byte = readByte()
            if (byte == 0.toByte()) break
            add(byte)
        }
    }
    val array = bytes.toByteArray()
    return decode(0, array, array.size)
}

private val CODE_PAGE = charArrayOf(
    '\u20AC', '\u0000', '\u201A', '\u0192', '\u201E', '\u2026', '\u2020', '\u2021',
    '\u02C6', '\u2030', '\u0160', '\u2039', '\u0152', '\u0000', '\u017D', '\u0000',
    '\u0000', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022', '\u2013', '\u2014',
    '\u02DC', '\u2122', '\u0161', '\u203A', '\u0153', '\u0000', '\u017E', '\u0178'
)

fun decode(off: Int, data: ByteArray, len: Int): String {
    val chars = CharArray(len)
    var index = 0

    for (i in 0 until len) {
        var c = data[i + off].toInt() and 0xFF
        if (c == '\u0000'.code) {
            continue
        }

        if (c > '\u007F'.code && c < '\u00A0'.code) {
            var v: Char = CODE_PAGE[c - '\u0080'.code]
            if (v == '\u0000') {
                v = '?'
            }

            c = v.code
        }

        chars[index++] = c.toChar()
    }

    return String(chars, 0, index)
}


fun ByteReadPacket.readBoolean(): Boolean = readByte().toInt() == 1

fun ByteReadPacket.readBooleanInverse() = g1Alt2() == 1

fun ByteReadPacket.readBooleanSubtract() = g1Alt3() == 1

fun ByteReadPacket.readBooleanAdd() = g1Alt1() == 1

fun ByteReadPacket.g1Alt1(): Int = (readByte() - 128).toByte().toInt()

fun ByteReadPacket.g1Alt2(): Int = -readByte()

fun ByteReadPacket.g1Alt3(): Int = (g1Alt2() + 128).toByte().toInt()

fun ByteReadPacket.readShortAdd(): Int = (readByte().toInt() shl 8) or g1Alt1()

fun ByteReadPacket.readShortAddLittle(): Int = ((readByte().toInt() - 128) and 0xff) or (readByte().toInt() shl 8)

fun ByteReadPacket.readUnsignedShortAdd(): Int = (readUByte().toInt() shl 8) + ((readByte() - 128) and 0xFF)

fun ByteReadPacket.readUnsignedShort128(): Int =
    (readUByte().toInt() shl 8) + ((readByte() - 128) and 0xFF)

fun ByteReadPacket.g2Alt3(): Int =
    ((readByte().toInt() - 128) and 0xFF) + ((readUByte().toInt() and 0xFF) shl 8).toShort().toInt()

fun ByteReadPacket.g2Alt1(): Int = readUByte().toInt() or (readUByte().toInt() shl 8)

fun ByteReadPacket.g2Alt2(): Int = (readUByte().toInt() shl 8) or (readByte() - 128 and 0xFF)

fun ByteReadPacket.readUnsignedShortAddLittle(): Int = (readByte() - 128 and 0xff) + (readByte().toInt() shl 8 and 0xff00)

fun ByteReadPacket.g4Alt2(): Int = (readUByte().toInt() shl 8) or readUByte().toInt() or (readUByte().toInt() shl 24) or (readUByte().toInt() shl 16)

fun ByteReadPacket.readIntInverseMiddle(): Int = (readByte().toInt() shl 16) or (readByte().toInt() shl 24) or readUByte().toInt() or (readByte().toInt() shl 8)

fun ByteReadPacket.g4Alt3(): Int = (readUByte().toInt() shl 16) or (readUByte().toInt() shl 24) or readUByte().toInt() or (readUByte().toInt() shl 8)

fun ByteReadPacket.readIntV2(): Int = (readUByte().toInt() shl 16) + (readUByte().toInt() shl 24) + readUByte().toInt() + (readUByte().toInt() shl 8)

fun ByteReadPacket.g4Alt1(): Int = readUByte().toInt() or (readUByte().toInt() shl 8) or (readUByte().toInt() shl 16) or (readUByte().toInt() shl 24)

fun ByteReadPacket.readSmart(): Int {
    val peek = readUByte().toInt()
    return if (peek < 128) {
        peek and 0xFF
    } else {
        (peek shl 8 or readUByte().toInt()) - 32768
    }
}

suspend fun ByteWriteChannel.writeName(displayName: String, responseName: String = displayName) {
    val different = displayName != responseName
    writeByte(different)
    writeString(displayName)
    if (different) {
        writeString(responseName)
    }
}

internal var random: Random = Random.Default

suspend fun ByteWriteChannel.writeRandom() {
    // TODO shouldn't this be a hash? Of the username and message?
    writeShort(random.nextInt())
    writeMedium(random.nextInt())
}

/**
 * Writes a string as an RS long
 */
suspend fun ByteWriteChannel.writeLong(string: String) {
    var long = 0L
    for (i in 0 until string.length.coerceAtMost(12)) {
        val char = string[i].code
        long *= 37L
        when (char) {
            in 65..90 -> long += char - 64L
            in 97..122 -> long += char - 96L
            in 0..9 -> long += char - 21L
        }
    }
    while (long % 37L == 0L && long != 0L) {
        long /= 37L
    }
    writeLong(long)
}
package world.gregs.voidps.buffer.write

import io.ktor.utils.io.*

suspend fun ByteWriteChannel.writeSmart(value: Int) {
    if (value >= 128) {
        writeShort(value + 32768)
    } else {
        writeByte(value)
    }
}

suspend fun ByteWriteChannel.writeByte(value: Int) = writeByte(value.toByte())

suspend fun ByteWriteChannel.writeString(value: String?) {
    if (value != null) {
        writeFully(value.toByteArray())
    }
    writeByte(0)
}
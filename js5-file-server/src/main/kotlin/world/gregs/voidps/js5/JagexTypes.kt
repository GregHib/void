package world.gregs.voidps.js5

import io.ktor.utils.io.*

suspend fun ByteReadChannel.readMedium(): Int {
    return (readByte().toInt() shl 16) + (readByte().toInt() shl 8) + readByte().toInt()
}

suspend fun ByteReadChannel.readUByte(): Int {
    return readByte().toInt() and 0xff
}

suspend fun ByteReadChannel.readUMedium(): Int {
    return (readUByte() shl 16) + (readUByte() shl 8) + readUByte()
}
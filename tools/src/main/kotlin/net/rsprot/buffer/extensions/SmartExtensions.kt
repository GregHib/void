package net.rsprot.buffer.extensions

import io.netty.buffer.ByteBuf

fun ByteBuf.gSmart1or2s(): Int {
    val peek = getUnsignedByte(readerIndex()).toInt()
    return if (peek < 128) {
        readUnsignedByte().toInt() - 64
    } else {
        readUnsignedShort() - 49152
    }
}

fun ByteBuf.pSmart1or2s(value: Int) {
    require(value in -16384..16383) {
        "smart value out of range: $value"
    }
    if (value in -64..63) {
        writeByte(value + 64)
    } else {
        writeShort(value + 49152)
    }
}

package world.gregs.voidps.tools.photobooth.vendor

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

fun Reader.gSmart1or2s(): Int {
    val peek = peek() and 0xFF
    return if (peek < 128) {
        readUnsignedByte() - 64
    } else {
        readUnsignedShort() - 49152
    }
}

fun Writer.pSmart1or2s(value: Int) {
    require(value in -16384..16383) {
        "smart value out of range: $value"
    }
    if (value in -64..63) {
        writeByte(value + 64)
    } else {
        writeShort(value + 49152)
    }
}

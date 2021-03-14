package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.buffer.write.writeMedium
import world.gregs.voidps.buffer.write.writeString
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.GameOpcodes.TILE_TEXT
import world.gregs.voidps.network.PacketSize
import world.gregs.voidps.network.string

fun Client.tileText(
    tile: Int,
    duration: Int,
    height: Int,
    color: Int,
    text: String
) = send(TILE_TEXT, 8 + string(text), PacketSize.BYTE) {
    writeByte(0)
    writeByte(tile)
    writeShort(duration)
    writeByte(height)
    writeMedium(color)
    writeString(text)
}
package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Client.Companion.BYTE
import world.gregs.voidps.network.Client.Companion.string
import world.gregs.voidps.network.Protocol.TILE_TEXT
import world.gregs.voidps.network.writeMedium
import world.gregs.voidps.network.writeString

fun Client.tileText(
    tile: Int,
    duration: Int,
    height: Int,
    color: Int,
    text: String
) = send(TILE_TEXT, 8 + string(text), BYTE) {
    writeByte(0)
    writeByte(tile)
    writeShort(duration)
    writeByte(height)
    writeMedium(color)
    writeString(text)
}
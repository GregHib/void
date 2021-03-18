package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol.SOUND_AREA

fun Client.areaSound(
    tile: Int,
    id: Int,
    type: Int,
    rotation: Int,
    three: Int,
    four: Int,
    five: Int
) = send(SOUND_AREA, 8) {
    writeByte(tile)
    writeShort(id)
    writeByte((type shl 4) and rotation)
    writeByte(three)
    writeByte(four)
    writeShort(five)
}
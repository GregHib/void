package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.writeInt
import io.ktor.utils.io.writeShort
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol
import world.gregs.voidps.network.login.protocol.writeByte
import world.gregs.voidps.network.login.protocol.writeShort

object HintArrow {
    const val FILLED = 0
    const val OUTLINE = 1
    const val BLUE = 2
    const val GREEN = 3
    const val GOLD = 4
    const val RED = 5
    const val BLUE_MINIMAP = 6
    const val SILVER_MINIMAP = 7
    const val RED_MINIMAP = 8
}

object HintType {
    const val CLEAR = 0
    const val NPC = 1
    const val TILE = 2
    const val WEST = 3
    const val EAST = 4
    const val SOUTH = 5
    const val NORTH = 6
    const val PLAYER = 10
}
/**
 * Send system update timer
 * @param type of hint (0=clear, 1=npc, 2=tile_centre, 3=tile_west, 4=tile_east, 5=tile_south, 6=tile_north, 10=player)
 * @param sprite arrow to use (0=yellow, 1=yellow_outline, 2=blue, 3=green, 4=gold, 5=red, 6=blue_minimap_only, 7=silver_minimap_only, 8=red_minimap_only)
 * @param entityIndex of npc or player when type = 1 or 10
 * @param arrowIndex 0-8
 * @param x tile x
 * @param y tile y
 * @param level tile level
 * @param z tile height off the ground
 * @param radius to display minimap arrow (multiple of 32 tiles)
 * @param model stuck to the players feet
 */
fun Client.arrowHint(
    type: Int,
    arrowIndex: Int,
    sprite: Int = -1,
    entityIndex: Int = 0,
    x: Int = 0,
    y: Int = 0,
    level: Int = 0,
    z: Int = 0,
    radius: Int = 0,
    model: Int = 65535,
) = send(Protocol.HINT_ARROW) {
    writeByte((arrowIndex shl 5) or type)
    // The packet is a fixed twelve bytes, so every branch - clearing included - has to write all
    // of it. Stopping short leaves the client reading the packets behind this one as the rest of
    // it, which desyncs the stream and takes the client down.
    if (type == 0) {
        repeat(11) {
            writeByte(0)
        }
        return@send
    }
    writeByte(sprite)
    when (type) {
        1, 10 -> {
            writeShort(entityIndex)
            // Skip 6
            writeInt(0)
            writeShort(0)
        }
        in 2..6 -> {
            writeByte(level) // level
            writeShort(x) // x
            writeShort(y) // y
            writeByte(z) // z?
            writeShort(radius)
        }
        else -> repeat(8) {
            writeByte(0)
        }
    }
    writeShort(model)
}

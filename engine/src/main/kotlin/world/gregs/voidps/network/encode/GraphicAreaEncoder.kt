package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol.GRAPHIC_AREA

fun addGraphic(ag: AreaGraphic): (Player) -> Unit = { player ->
    player.client?.addAreaGraphic(ag.tile.offset(), ag.graphic.id, ag.graphic.height, ag.graphic.delay, ag.graphic.rotation)
}

/**
 * @param tile The tile offset from the chunk update send
 * @param id graphic id
 * @param height 0..255 start height off the ground
 * @param delay delay to start graphic 30 = 1 tick
 * @param rotation 0..7
 */
fun Client.addAreaGraphic(
    tile: Int,
    id: Int,
    height: Int,
    delay: Int,
    rotation: Int
) = send(GRAPHIC_AREA) {
    writeByte(tile)
    writeShort(id)
    writeByte(height)
    writeShort(delay)
    writeByte(rotation)
}
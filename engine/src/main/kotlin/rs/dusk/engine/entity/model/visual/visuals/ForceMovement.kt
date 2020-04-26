package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.model.Direction
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ForceMovement(
    var tile1: Tile = Tile(0),
    var delay1: Int = 0,
    var tile2: Tile = Tile(0),
    var delay2: Int = 0,
    var direction: Direction = Direction.NONE
) : Visual

fun Player.flagForceMovement() = visuals.flag(0x1000)

fun NPC.flagForceMovement() = visuals.flag(0x400)

fun Indexed.flagForceMovement() {
    if (this is Player) flagForceMovement() else if (this is NPC) flagForceMovement()
}

fun Indexed.getForceMovement() = visuals.getOrPut(ForceMovement::class) { ForceMovement() }

fun Indexed.setForceMovement(
    tile1: Tile,
    delay1: Int = 0,
    tile2: Tile = Tile(0),
    delay2: Int = 0,
    direction: Direction = Direction.NONE
) {
    val move = getForceMovement()
    move.tile1 = tile1
    move.delay1 = delay1
    move.tile2 = tile2
    move.delay2 = delay2
    move.direction = direction
    flagForceMovement()
}

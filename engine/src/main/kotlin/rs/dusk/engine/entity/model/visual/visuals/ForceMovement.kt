package rs.dusk.engine.entity.model.visual.visuals

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

const val PLAYER_FORCE_MOVEMENT_MASK = 0x1000

const val NPC_FORCE_MOVEMENT_MASK = 0x400

fun Player.flagForceMovement() = visuals.flag(PLAYER_FORCE_MOVEMENT_MASK)

fun NPC.flagForceMovement() = visuals.flag(NPC_FORCE_MOVEMENT_MASK)

fun Player.getForceMovement() = visuals.getOrPut(PLAYER_FORCE_MOVEMENT_MASK) { ForceMovement() }

fun NPC.getForceMovement() = visuals.getOrPut(NPC_FORCE_MOVEMENT_MASK) { ForceMovement() }

fun Player.setForceMovement(
    tile1: Tile,
    delay1: Int = 0,
    tile2: Tile = Tile(0),
    delay2: Int = 0,
    direction: Direction = Direction.NONE
) {
    setForceMovement(getForceMovement(), tile1, delay1, tile2, delay2, direction)
    flagForceMovement()
}

fun NPC.setForceMovement(
    tile1: Tile,
    delay1: Int = 0,
    tile2: Tile = Tile(0),
    delay2: Int = 0,
    direction: Direction = Direction.NONE
) {
    setForceMovement(getForceMovement(), tile1, delay1, tile2, delay2, direction)
    flagForceMovement()
}

private fun setForceMovement(
    move: ForceMovement,
    tile1: Tile,
    delay1: Int,
    tile2: Tile,
    delay2: Int,
    direction: Direction
) {
    move.tile1 = tile1
    move.delay1 = delay1
    move.tile2 = tile2
    move.delay2 = delay2
    move.direction = direction
}

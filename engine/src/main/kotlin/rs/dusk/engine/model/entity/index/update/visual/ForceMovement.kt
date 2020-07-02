package rs.dusk.engine.model.entity.index.update.visual

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.Visual
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ForceMovement(
    var start: Tile = Tile.EMPTY,
    var startDelay: Int = 0,
    var end: Tile = Tile.EMPTY,
    var endDelay: Int = 0,
    var direction: Direction = Direction.NONE
) : Visual

const val PLAYER_FORCE_MOVEMENT_MASK = 0x1000

const val NPC_FORCE_MOVEMENT_MASK = 0x400

fun Player.flagForceMovement() = visuals.flag(PLAYER_FORCE_MOVEMENT_MASK)

fun NPC.flagForceMovement() = visuals.flag(NPC_FORCE_MOVEMENT_MASK)

fun Player.getForceMovement() = visuals.getOrPut(PLAYER_FORCE_MOVEMENT_MASK) { ForceMovement() }

fun NPC.getForceMovement() = visuals.getOrPut(NPC_FORCE_MOVEMENT_MASK) { ForceMovement() }

/**
 * @param endDelta The delta position to move towards
 * @param endDelay Number of client ticks to take moving
 * @param startDelta The delta position to start at
 * @param startDelay Client ticks until starting the movement
 * @param direction The cardinal direction to face during movement
 */
fun Player.setForceMovement(
    endDelta: Tile = Tile.EMPTY,
    endDelay: Int = 0,
    startDelta: Tile = Tile(
        0
    ),
    startDelay: Int = 0,
    direction: Direction = Direction.NONE
) {
    setForceMovement(getForceMovement(), startDelta, startDelay, endDelta, endDelay, direction)
    flagForceMovement()
}

fun NPC.setForceMovement(
    endDelta: Tile = Tile.EMPTY,
    endDelay: Int = 0,
    startDelta: Tile = Tile(
        0
    ),
    startDelay: Int = 0,
    direction: Direction = Direction.NONE
) {
    setForceMovement(getForceMovement(), startDelta, startDelay, endDelta, endDelay, direction)
    flagForceMovement()
}

private fun setForceMovement(
    move: ForceMovement,
    start: Tile,
    startDelay: Int,
    end: Tile,
    endDelay: Int,
    direction: Direction
) {
    check(endDelay > startDelay) { "End delay ($endDelay) must be after start delay ($startDelay)." }
    move.start = start
    move.startDelay = startDelay
    move.end = end
    move.endDelay = endDelay
    move.direction = direction
}

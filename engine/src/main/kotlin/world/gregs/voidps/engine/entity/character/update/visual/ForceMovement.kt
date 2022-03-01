package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.network.visual.VisualMask.NPC_FORCE_MOVEMENT_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_FORCE_MOVEMENT_MASK

private fun mask(character: Character) = if (character is Player) PLAYER_FORCE_MOVEMENT_MASK else NPC_FORCE_MOVEMENT_MASK

fun Character.flagForceMovement() = visuals.flag(mask(this))

/**
 * @param endDelta The delta position to move towards
 * @param endDelay Number of client ticks to take moving
 * @param startDelta The delta position to start at
 * @param startDelay Client ticks until starting the movement
 * @param direction The cardinal direction to face during movement
 */
fun Character.setForceMovement(
    endDelta: Delta = Delta.EMPTY,
    endDelay: Int = 0,
    startDelta: Delta = Delta.EMPTY,
    startDelay: Int = 0,
    direction: Direction = Direction.NONE
) {
    val move = visuals.forceMovement
    check(endDelay > startDelay) { "End delay ($endDelay) must be after start delay ($startDelay)." }
    move.startX = startDelta.x
    move.startY = startDelta.y
    move.startDelay = startDelay
    move.endX = endDelta.x
    move.endY = endDelta.y
    move.endDelay = endDelay
    move.direction = direction.ordinal
    flagForceMovement()
}

fun Character.forceWalk(delta: Delta, delay: Int = 0, direction: Direction = Direction.NONE, block: () -> Unit = {}) {
    setForceMovement(delta, delay, direction = direction)
    this["force_walk"] = block
    delay(delay / 30) {
        move(delta)
        clearAnimation()
    }
}

fun Character.forceWalk(target: Tile, delay: Int = tile.distanceTo(target) * 30, direction: Direction = Direction.NONE, block: () -> Unit = {}) {
    forceWalk(target.delta(tile), delay, direction, block)
}
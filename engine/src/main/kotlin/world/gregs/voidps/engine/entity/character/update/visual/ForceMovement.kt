package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.event.EventHandler
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile

data class ForceMovement(
    var start: Delta = Delta.EMPTY,
    var startDelay: Int = 0,
    var end: Delta = Delta.EMPTY,
    var endDelay: Int = 0,
    var direction: Direction = Direction.NONE
) : Visual

const val PLAYER_FORCE_MOVEMENT_MASK = 0x2000

const val NPC_FORCE_MOVEMENT_MASK = 0x1000

private fun mask(character: Character) = if (character is Player) PLAYER_FORCE_MOVEMENT_MASK else NPC_FORCE_MOVEMENT_MASK

fun Character.flagForceMovement() = visuals.flag(mask(this))

fun Character.getForceMovement() = visuals.getOrPut(mask(this)) { ForceMovement() }

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
    setForceMovement(getForceMovement(), startDelta, startDelay, endDelta, endDelay, direction)
    flagForceMovement()
}

private fun setForceMovement(
    move: ForceMovement,
    start: Delta,
    startDelay: Int,
    end: Delta,
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

fun Character.forceWalk(delta: Delta, delay: Int = 0, direction: Direction = Direction.NONE, block: () -> Unit = {}) {
    setForceMovement(delta, delay, direction = direction)
    var handler: EventHandler? = null
    handler = events.on<Character, Moved> {
        block.invoke()
        handler?.let {
            events.remove(it)
        }
    }
    delay(this, delay / 30) {
        move(delta)
        clearAnimation()
    }
}

fun Character.forceWalk(target: Tile, delay: Int = tile.distanceTo(target) * 30, direction: Direction = Direction.NONE, block: () -> Unit = {}) {
    forceWalk(target.delta(tile), delay, direction, block)
}
package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class MovementSpeed(var running: Boolean = false) : Visual

const val MOVEMENT_SPEED_MASK = 0x1

fun Player.flagMovementSpeed() = visuals.flag(MOVEMENT_SPEED_MASK)

fun Player.getMovementSpeed() = visuals.getOrPut(MOVEMENT_SPEED_MASK) { MovementSpeed() }

fun Player.setMovementSpeed(running: Boolean) {
    getMovementSpeed().running = running
    flagMovementSpeed()
}
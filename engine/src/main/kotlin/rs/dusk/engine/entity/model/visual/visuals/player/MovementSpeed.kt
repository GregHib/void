package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class MovementSpeed(var running: Boolean = false) : Visual

fun Player.flagMovementSpeed() = visuals.flag(0x1)

fun Player.getMovementSpeed() = visuals.getOrPut(MovementSpeed::class) { MovementSpeed() }

fun Player.setMovementSpeed(running: Boolean) {
    getMovementSpeed().running = running
    flagMovementSpeed()
}
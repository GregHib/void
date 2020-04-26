package rs.dusk.engine.entity.model.visual.visuals.player

import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class MovementType(var type: Int = NONE) : Visual {

    companion object {
        const val NONE = 0
        const val WALK = 1
        const val RUN = 2
        const val TELEPORT = 127
    }
}

fun Player.flagMovementType() = visuals.flag(0x200)

fun Player.getMovementType() = visuals.getOrPut(MovementType::class) { MovementType() }

fun Player.setMovementType(type: Int) {
    getMovementType().type = type
    flagMovementType()
}
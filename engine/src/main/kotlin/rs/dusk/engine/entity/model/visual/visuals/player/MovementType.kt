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

const val MOVEMENT_TYPE_MASK = 0x200

fun Player.flagMovementType() = visuals.flag(MOVEMENT_TYPE_MASK)

fun Player.getMovementType() = visuals.getOrPut(MOVEMENT_TYPE_MASK) { MovementType() }

var Player.movementType: Int
    get() = getMovementType().type
    set(value) {
        getMovementType().type = value
        flagMovementType()
    }
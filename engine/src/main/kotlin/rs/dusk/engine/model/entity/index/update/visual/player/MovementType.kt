package rs.dusk.engine.model.entity.index.update.visual.player

import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.update.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class MovementType(var type: PlayerMoveType = PlayerMoveType.None) : Visual

const val MOVEMENT_TYPE_MASK = 0x200

fun Player.flagMovementType() = visuals.flag(MOVEMENT_TYPE_MASK)

fun Player.getMovementType() = visuals.getOrPut(MOVEMENT_TYPE_MASK) { MovementType() }

var Player.movementType: PlayerMoveType
    get() = getMovementType().type
    set(value) {
        getMovementType().type = value
        flagMovementType()
    }
package rs.dusk.engine.model.entity.index.update.visual.player

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.update.Visual
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class MovementType(var type: PlayerMoveType = PlayerMoveType.None) : Visual {
    override fun reset(character: Character) {
        val player = character as Player
        player.movementType = PlayerMoveType.None
    }
}

const val MOVEMENT_TYPE_MASK = 0x200

fun Player.flagMovementType() = visuals.flag(MOVEMENT_TYPE_MASK)

fun Player.getMovementType() = visuals.getOrPut(MOVEMENT_TYPE_MASK) { MovementType() }

var Player.movementType: PlayerMoveType
    get() = getMovementType().type
    set(value) {
        if (getMovementType().type != value) {
            getMovementType().type = value
            flagMovementType()
        }
    }

fun Player.tele(x: Int = tile.x, y: Int = tile.y, plane: Int = tile.plane) {
    action.run(ActionType.Teleport) {
        movement.delta = Tile(x - tile.x, y - tile.y, plane - tile.plane)
        if (movement.delta != Tile.EMPTY) {
            movementType = PlayerMoveType.Teleport
        }
    }
}
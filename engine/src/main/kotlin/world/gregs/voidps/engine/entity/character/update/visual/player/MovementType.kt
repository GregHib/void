package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerMoveType
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.map.Tile

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
data class MovementType(var type: PlayerMoveType = PlayerMoveType.None) : Visual {
    override fun reset(character: Character) {
        val player = character as Player
        player.movementType = PlayerMoveType.None
    }
}

const val MOVEMENT_TYPE_MASK = 0x800

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
        movement.target = null
        movement.callback = null
        movement.clear()
        movement.delta = Tile(x - tile.x, y - tile.y, plane - tile.plane)
        if (movement.delta != Tile.EMPTY) {
            movementType = PlayerMoveType.Teleport
        }
    }
}
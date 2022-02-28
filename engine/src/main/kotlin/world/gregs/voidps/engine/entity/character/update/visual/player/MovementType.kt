package world.gregs.voidps.engine.entity.character.update.visual.player

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.update.task.MoveType
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area

data class MovementType(var type: MoveType = MoveType.None) : Visual {
    override fun needsReset(character: Character): Boolean {
        return type != MoveType.None
    }

    override fun reset(character: Character) {
        val player = character as Player
        player.movementType = MoveType.None
    }
}

const val MOVEMENT_TYPE_MASK = 0x800

fun Player.flagMovementType() = visuals.flag(MOVEMENT_TYPE_MASK)

fun Player.getMovementType() = visuals.movementType

var Player.movementType: MoveType
    get() = getMovementType().type
    set(value) {
        if (getMovementType().type != value) {
            getMovementType().type = value
            flagMovementType()
        }
    }

fun Character.tele(tile: Tile) = tele(tile.delta(this.tile))

fun Character.tele(x: Int = tile.x, y: Int = tile.y, plane: Int = tile.plane) = tele(Delta(x - tile.x, y - tile.y, plane - tile.plane))

fun Character.tele(area: Area) = tele(area.random())

fun Character.tele(delta: Delta) {
    action.run(ActionType.Teleport) {
        move(delta)
    }
}

fun Character.move(tile: Tile) = move(tile.delta(this.tile))

fun Character.move(delta: Delta) {
    movement.clear()
    movement.delta = delta
    movement.previousTile = tile.add(delta).add(Direction.WEST)
    if (this is Player && movement.delta != Delta.EMPTY) {
        movementType = MoveType.Teleport
    }
}
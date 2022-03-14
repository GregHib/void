package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.network.visual.update.player.MoveType

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
    if (this is Player && delta != Delta.EMPTY) {
        movementType = MoveType.Teleport
    }
}
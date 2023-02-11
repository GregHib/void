package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Movement
import world.gregs.voidps.engine.entity.character.mode.interact.clear
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.network.visual.update.player.MoveType

fun Character.tele(x: Int = tile.x, y: Int = tile.y, plane: Int = tile.plane) = tele(Delta(x - tile.x, y - tile.y, plane - tile.plane))

fun Character.tele(area: Area) = tele(area.random())

fun Character.tele(tile: Tile, clearMode: Boolean = true) = tele(tile.delta(this.tile), clearMode)

fun Character.tele(delta: Delta, clearMode: Boolean = true) {
    if (delta == Delta.EMPTY) {
        return
    }
    clear(suspend = false, mode = clearMode)
    if (this is Player) {
        movementType = MoveType.Teleport
    }
    Movement.move(this, delta)
    previousTile = tile.add(Direction.WEST)
}
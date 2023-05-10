package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.network.visual.update.player.MoveType

fun Character.tele(x: Int = tile.x, y: Int = tile.y, plane: Int = tile.plane) = tele(Delta(x - tile.x, y - tile.y, plane - tile.plane))

fun Character.tele(area: Area) = tele(area.random())

fun Character.tele(tile: Tile, clearMode: Boolean = true, clearInterfaces: Boolean = true) = tele(tile.delta(this.tile), clearMode, clearInterfaces)

fun Character.tele(delta: Delta, clearMode: Boolean = true, clearInterfaces: Boolean = true) {
    if (delta == Delta.EMPTY) {
        return
    }
    if (clearMode) {
        mode = EmptyMode
    }
    if (this is Player) {
        if (clearInterfaces) {
            closeInterfaces()
        }
        movementType = MoveType.Teleport
    }
    previousTile = tile.add(delta).add(Direction.WEST)
    Movement.move(this, delta)
}
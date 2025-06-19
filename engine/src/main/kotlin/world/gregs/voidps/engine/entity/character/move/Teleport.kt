package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.network.login.protocol.visual.update.player.MoveType
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

fun Character.tele(x: Int = tile.x, y: Int = tile.y, level: Int = tile.level) = tele(Delta(x - tile.x, y - tile.y, level - tile.level))

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
    steps.clear()
    steps.previous = tile.add(delta).add(Direction.WEST)
    visuals.tele = true
    Movement.move(this, delta)
}

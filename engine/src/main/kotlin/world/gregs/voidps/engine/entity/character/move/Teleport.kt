package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.mode.Movement
import world.gregs.voidps.engine.entity.character.mode.interact.clear
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.network.visual.update.player.MoveType

fun Character.tele(tile: Tile) = tele(tile.delta(this.tile))

fun Character.tele(x: Int = tile.x, y: Int = tile.y, plane: Int = tile.plane) = tele(Delta(x - tile.x, y - tile.y, plane - tile.plane))

fun Character.tele(area: Area) = tele(area.random())

fun Character.tele(delta: Delta) = move(delta)

fun Character.move(tile: Tile) = move(tile.delta(this.tile))

fun Character.move(delta: Delta) {
    clear(suspend = false, animation = false)
    val from = tile
    tile = tile.add(delta)
    previousTile = tile.add(Direction.WEST)
    visuals.moved = true
    if (this is Player && delta != Delta.EMPTY) {
        movementType = MoveType.Teleport
    }
    Movement.move(this, from, tile)
    events.emit(Moved(from, tile))
}
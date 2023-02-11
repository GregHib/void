package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.TileTargetStrategy
import world.gregs.voidps.engine.map.Tile

fun Character.walkTo(target: Tile, force: Boolean = false) {
    mode = Movement(this, TileTargetStrategy(target), force)
}
package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.TileTargetStrategy
import world.gregs.voidps.type.Tile

fun Character.walkTo(target: Tile, noCollision: Boolean = false, slowRun: Boolean = false) {
    mode = Movement(this, TileTargetStrategy(target, noCollision, slowRun))
}
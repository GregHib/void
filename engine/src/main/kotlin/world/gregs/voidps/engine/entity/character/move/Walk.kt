package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.TileTargetStrategy
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Tile

/**
 * Walks player to [target]
 * Specify [noCollision] to walk through [GameObject]s and
 * [noRun] to force walking even if the player has running active
 */
fun Character.walkTo(target: Tile, noCollision: Boolean = false, noRun: Boolean = false) {
    mode = Movement(this, TileTargetStrategy(target, noCollision, noRun))
}

/**
 * Forces the character to walk to a tile
 */
context(SuspendableContext<*>) suspend fun Character.walkTo(tile: Tile) {
    if (this.tile == tile) {
        return
    }
    walkTo(tile, noCollision = false, noRun = false)
    while (this.tile != tile) {
        delay()
    }
}

/**
 * Force a character to walk to tile ignoring collisions
 */
context(SuspendableContext<*>) suspend fun Character.walkOver(tile: Tile) {
    if (this.tile == tile) {
        return
    }
    walkTo(tile, noCollision = true, noRun = true)
    while (this.tile != tile) {
        delay()
    }
}

/**
 * Steps a character to tile (run or walk)
 */
context(SuspendableContext<*>) suspend fun Character.stepTo(tile: Tile) {
    if (this.tile == tile) {
        return
    }
    walkTo(tile, noCollision = false, noRun = false)
    while (this.tile != tile) {
        delay()
    }
}

/**
 * Steps a character to tile (run or walk) ignoring collisions
 */
context(SuspendableContext<*>) suspend fun Character.stepOver(tile: Tile) {
    if (this.tile == tile) {
        return
    }
    walkTo(tile, noCollision = true, noRun = false)
    while (this.tile != tile) {
        delay()
    }
}
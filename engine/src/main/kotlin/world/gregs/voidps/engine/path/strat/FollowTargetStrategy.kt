package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile

/**
 * Checks if on the tile behind a player
 */
data class FollowTargetStrategy(
    private val character: Character
) : TileTargetStrategy {

    override val tile: Tile
        get() = character.movement.previousTile

    override val size: Size
        get() = character.size

    override fun reached(current: Tile, size: Size): Boolean {
        return tile == current
    }
}
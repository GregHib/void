package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile

/**
 * Checks if not within [distance] from target
 */
data class DistanceFromTargetStrategy(
    val target: Character,
    private val distance: Int
) : TileTargetStrategy {

    override val tile: Tile
        get() = target.tile

    override val size: Size
        get() = target.size

    override fun reached(current: Tile, size: Size): Boolean {
        return current.distanceTo(tile, size) > distance
    }
}
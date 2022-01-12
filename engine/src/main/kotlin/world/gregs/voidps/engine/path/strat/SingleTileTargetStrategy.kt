package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

/**
 * Checks if on an exact tile
 */
data class SingleTileTargetStrategy(
    override val tile: Tile,
    override val size: Size = Size.ONE
) : TileTargetStrategy {

    override fun reached(current: Tile, size: Size): Boolean {
        return tile == current
    }
}
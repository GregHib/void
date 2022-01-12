package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile

interface TileTargetStrategy {
    val tile: Tile
    val size: Size

    fun reached(current: Tile, size: Size): Boolean
}
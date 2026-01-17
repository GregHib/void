package world.gregs.voidps.tools.graph

import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

interface TileTraversalStrategy {

    fun blocked(x: Int, y: Int, level: Int, size: Int, direction: Direction): Boolean = true

    fun blocked(tile: Tile, size: Int, direction: Direction): Boolean = blocked(tile.x, tile.y, tile.level, size, direction)
}

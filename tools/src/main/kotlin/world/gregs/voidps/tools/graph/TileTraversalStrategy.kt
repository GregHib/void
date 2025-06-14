package world.gregs.voidps.tools.graph

import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

interface TileTraversalStrategy {

    fun blocked(collisions: Collisions, x: Int, y: Int, level: Int, size: Int, direction: Direction): Boolean = true

    fun blocked(collisions: Collisions, tile: Tile, size: Int, direction: Direction): Boolean = blocked(collisions, tile.x, tile.y, tile.level, size, direction)
}

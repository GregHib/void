package world.gregs.voidps.tools.graph

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions

interface TileTraversalStrategy {

    fun blocked(collisions: Collisions, x: Int, y: Int, plane: Int, size: Size, direction: Direction): Boolean = true

    fun blocked(collisions: Collisions, tile: Tile, size: Size, direction: Direction): Boolean = blocked(collisions, tile.x, tile.y, tile.plane, size, direction)

}
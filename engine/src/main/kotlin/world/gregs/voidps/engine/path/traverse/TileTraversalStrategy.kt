package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.TraversalStrategy

interface TileTraversalStrategy : TraversalStrategy {

    fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean = true

    fun blocked(tile: Tile, direction: Direction): Boolean = blocked(tile.x, tile.y, tile.plane, direction)

}
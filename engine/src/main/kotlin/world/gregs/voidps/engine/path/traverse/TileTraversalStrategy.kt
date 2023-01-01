package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategyOld

interface TileTraversalStrategy {

    fun blocked(collision: CollisionStrategyOld, x: Int, y: Int, plane: Int, size: Size, direction: Direction): Boolean = true

    fun blocked(collision: CollisionStrategyOld, tile: Tile, size: Size, direction: Direction): Boolean = blocked(collision, tile.x, tile.y, tile.plane, size, direction)

}
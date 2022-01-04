package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.TraversalStrategy

interface TileTraversalStrategy : TraversalStrategy {

    fun blocked(collision: CollisionStrategy, collisions: Collisions, x: Int, y: Int, plane: Int, size: Size, direction: Direction): Boolean = true

    fun blocked(collision: CollisionStrategy, collisions: Collisions, tile: Tile, size: Size, direction: Direction): Boolean = blocked(collision, collisions, tile.x, tile.y, tile.plane, size, direction)

}

val Character.traversal: TileTraversalStrategy
    get() = when {
        hasEffect("no_clip") -> NoClipTraversal
        size == Size.ONE -> SmallTraversal
        size.width == 2 && size.height == 2 -> MediumTraversal
        else -> LargeTraversal
    }
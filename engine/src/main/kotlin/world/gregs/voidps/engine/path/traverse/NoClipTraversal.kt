package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.map.collision.CollisionStrategyOld

object NoClipTraversal : TileTraversalStrategy {
    override fun blocked(collision: CollisionStrategyOld, x: Int, y: Int, plane: Int, size: Size, direction: Direction): Boolean = false
}
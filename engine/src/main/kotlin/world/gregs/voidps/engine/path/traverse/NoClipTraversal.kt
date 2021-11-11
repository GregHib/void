package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.Direction

object NoClipTraversal : TileTraversalStrategy {
    override fun blocked(x: Int, y: Int, plane: Int, direction: Direction): Boolean = false
}
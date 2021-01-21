package world.gregs.void.engine.path.algorithm

import world.gregs.void.engine.entity.Direction
import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.move.Movement
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.PathAlgorithm
import world.gregs.void.engine.path.PathResult
import world.gregs.void.engine.path.TargetStrategy
import world.gregs.void.engine.path.TraversalStrategy

/**
 * Moves in any direction towards the target until blocked by obstacle or reaches
 * Used for following and combat.
 * @author GregHib <greg@gregs.world>
 * @since May 20, 2020
 */
class DirectDiagonalSearch : PathAlgorithm {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult {
        val steps = movement.steps
        val delta = tile.delta(strategy.tile)
        var dx = delta.x
        var dy = delta.y
        var x = tile.x
        var y = tile.y

        while (dx != 0 || dy != 0) {
            val deltaX = -dx.coerceIn(-1, 1)
            val deltaY = -dy.coerceIn(-1, 1)
            val direction = Direction.of(deltaX, deltaY)
            if (direction.isDiagonal() && !traversal.blocked(x, y, tile.plane, direction)) {
                steps.add(direction)
                dx += deltaX
                dy += deltaY
                x += deltaX
                y += deltaY
            } else if(deltaX != 0 && !traversal.blocked(x, y, tile.plane, direction.horizontal())) {
                steps.add(direction.horizontal())
                dx += deltaX
                x += deltaX
            } else if(deltaY != 0 && !traversal.blocked(x, y, tile.plane, direction.vertical())) {
                steps.add(direction.vertical())
                dy += deltaY
                y += deltaY
            } else {
                break
            }
        }
        val last = tile.copy(x = x, y = y)
        return if (strategy.reached(last, size)) {
            PathResult.Success(last)
        } else {
            PathResult.Partial(last)
        }
    }

}
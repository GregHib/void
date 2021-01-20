package world.gregs.void.engine.path

import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.move.Movement
import world.gregs.void.engine.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
interface PathAlgorithm {
    /**
     * Calculates a route from [tile] to [strategy.target]
     * taking into account movement allowed by [traversal]
     * appending the individual steps to [movement.steps].
     * @return Success, Partial (movement but not reached target), Failure
     */
    fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult
}
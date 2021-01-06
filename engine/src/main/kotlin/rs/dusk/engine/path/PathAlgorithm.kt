package rs.dusk.engine.path

import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.move.Movement
import rs.dusk.engine.map.Tile

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
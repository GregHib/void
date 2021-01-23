package world.gregs.voidps.engine.path

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.map.Tile

/**
 * @author GregHib <greg@gregs.world>
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
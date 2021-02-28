package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathAlgorithm
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy

/**
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
interface TilePathAlgorithm : PathAlgorithm<TileTargetStrategy, TileTraversalStrategy> {

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
        strategy: TileTargetStrategy,
        traversal: TileTraversalStrategy
    ): PathResult
}
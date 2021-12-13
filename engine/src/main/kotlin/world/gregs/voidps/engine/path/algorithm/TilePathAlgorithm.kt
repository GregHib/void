package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathAlgorithm
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy

interface TilePathAlgorithm : PathAlgorithm<TileTargetStrategy, TileTraversalStrategy> {

    /**
     * Calculates a route from [tile] to [path.strategy.target]
     * taking into account movement allowed by [traversal]
     * appending the individual steps to [path.steps].
     * @return Success, Partial (movement but not reached target), Failure
     */
    fun find(
        tile: Tile,
        size: Size,
        path: Path,
        traversal: TileTraversalStrategy
    ): PathResult
}
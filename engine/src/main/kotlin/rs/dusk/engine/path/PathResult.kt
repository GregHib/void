package rs.dusk.engine.path

import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
sealed class PathResult {
    sealed class Success(val last: Tile) : PathResult() {
        class Complete(last: Tile) : Success(last)
        class Partial(last: Tile) : Success(last)
    }

    object Failure : PathResult()
}
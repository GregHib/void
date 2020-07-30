package rs.dusk.engine.path

import rs.dusk.engine.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
sealed class PathResult {
    sealed class Success(val last: Tile) : PathResult() {
        // Can reach target with/without steps
        class Complete(last: Tile) : Success(last)

        // Cannot reach target but steps taken to move closer
        class Partial(last: Tile) : Success(last)
    }

    // Cannot reach target so no steps taken
    object Failure : PathResult()
}
package rs.dusk.engine.path

import rs.dusk.engine.map.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
sealed class PathResult {
    // Can reach target with/without steps
    class Success(val last: Tile) : PathResult()

    // Cannot reach target but steps taken to move closer
    class Partial(val last: Tile) : PathResult()

    // Cannot reach target so no steps taken
    object Failure : PathResult()
}
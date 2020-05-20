package rs.dusk.engine.path

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
sealed class PathResult {
    object Success : PathResult()
    object Partial : PathResult()
    object Failure : PathResult()
    object Cancelled : PathResult()
}
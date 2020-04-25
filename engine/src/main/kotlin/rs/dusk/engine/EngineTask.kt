package rs.dusk.engine

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
abstract class EngineTask(val priority: Int = 0) : Runnable, Comparable<EngineTask> {
    override fun compareTo(other: EngineTask): Int {
        return other.priority.compareTo(priority)
    }
}
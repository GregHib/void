package rs.dusk.engine

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
abstract class EngineTask(tasks: EngineTasks? = null, val priority: Int = 0) : Runnable, Comparable<EngineTask> {

    init {
        @Suppress("LeakingThis")
        tasks?.add(this)
    }

    override fun compareTo(other: EngineTask): Int {
        return other.priority.compareTo(priority)
    }
}
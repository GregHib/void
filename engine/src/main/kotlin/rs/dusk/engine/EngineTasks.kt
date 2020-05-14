package rs.dusk.engine

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
data class EngineTasks(val data: LinkedHashSet<EngineTask> = LinkedHashSet()) : Set<EngineTask> by data
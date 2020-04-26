package rs.dusk.engine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
abstract class ParallelEngineTask(tasks: EngineTasks, priority: Int = 0) : EngineTask(tasks, priority) {
    val defers: Deque<Deferred<Unit>> = LinkedList()

    override fun run() = runBlocking {
        while (defers.isNotEmpty()) {
            defers.poll().await()
        }
    }
}
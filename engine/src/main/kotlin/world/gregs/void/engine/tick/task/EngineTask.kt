package world.gregs.void.engine.tick.task

import kotlinx.coroutines.runBlocking
import world.gregs.void.engine.event.priority
import world.gregs.void.engine.event.then
import world.gregs.void.engine.tick.TickUpdate

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
@Deprecated("Use scripts instead")
abstract class EngineTask(priority: Int) : Runnable {
    init {
        TickUpdate priority priority then {
            runBlocking {
                run()
            }
        }
    }
}
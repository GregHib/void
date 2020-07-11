package rs.dusk.engine.model.engine.task

import kotlinx.coroutines.runBlocking
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.TickUpdate

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
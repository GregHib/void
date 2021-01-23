package world.gregs.voidps.engine.tick.task

import kotlinx.coroutines.runBlocking
import world.gregs.voidps.engine.event.priority
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.tick.TickUpdate

/**
 * @author GregHib <greg@gregs.world>
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
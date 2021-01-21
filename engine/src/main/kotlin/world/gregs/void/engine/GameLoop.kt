package world.gregs.void.engine

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.runBlocking
import world.gregs.void.engine.action.Contexts
import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.task.SyncTask
import world.gregs.void.engine.task.TaskExecutor
import world.gregs.void.engine.task.delay
import world.gregs.void.engine.task.repeat
import world.gregs.void.engine.tick.Startup
import world.gregs.void.engine.tick.Tick
import world.gregs.void.engine.tick.TickInput
import world.gregs.void.engine.tick.TickUpdate
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

/**
 * @author GregHib <greg@gregs.world>
 * @since April 22, 2020
 */
class GameLoop(
    private val bus: EventBus,
    private val executor: TaskExecutor,
    private val service: ScheduledExecutorService
) {

    private val logger = InlineLogger()

    fun setup(start: SyncTask) {
        execute(Startup)
        executor.execute(start)
        executor.repeat {
            loopSafe(it)
        }
    }

    fun start() {
        service.scheduleAtFixedRate(executor, 0, ENGINE_DELAY, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        executor.clear()
        service.shutdown()
    }

    private fun loopSafe(tick: Long) {
        try {
            GameLoop.tick = tick
            timedLoop(tick)
        } catch (t: Throwable) {
            logger.error(t) { "Error in game loop!" }
        }
    }

    private fun timedLoop(tick: Long) {
        val nano = measureNanoTime { loopThreeTickEvents() }
        printTickIfOverThreshold(nano, tick)
    }

    private fun printTickIfOverThreshold(nano: Long, tick: Long) {
        val millis = TimeUnit.NANOSECONDS.toMillis(nano)
        if (millis >= MILLI_THRESHOLD) {
            logger.info { "Tick $tick took ${millis}ms" }
        }
    }

    private fun loopThreeTickEvents() {
        execute(TickInput) { tick ->
            execute(Tick(tick)) {
                execute(TickUpdate)
            }
        }
    }

    private inline fun <reified T : Any, reified E : Event<T>> execute(event: E, noinline after: ((Long) -> Unit)? = null) {
        executor.delay { tick ->
            runBlocking(Contexts.Game) {
                bus.emit(event)
            }
            after?.invoke(tick)
        }
    }

    companion object {
        var tick: Long = 0L
        private const val ENGINE_DELAY = 600L
        private const val MILLI_THRESHOLD = 5
    }
}
package rs.dusk.engine

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.runBlocking
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.engine.Startup
import rs.dusk.engine.model.engine.Tick
import rs.dusk.engine.model.engine.TickInput
import rs.dusk.engine.model.engine.TickUpdate
import rs.dusk.engine.task.StartTask
import rs.dusk.engine.task.TaskExecutor
import rs.dusk.engine.task.delay
import rs.dusk.engine.task.repeat
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
class GameLoop(
    private val bus: EventBus,
    private val executor: TaskExecutor,
    private val service: ScheduledExecutorService
) {

    private val logger = InlineLogger()

    fun setup(start: StartTask) {
        executor.delay {
            bus.emit(Startup)
        }
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
        private const val ENGINE_DELAY = 600L
        private const val MILLI_THRESHOLD = 5
    }
}
package rs.dusk.engine

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.engine.*
import kotlin.concurrent.fixedRateTimer
import kotlin.system.measureTimeMillis

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
class Engine(private val bus: EventBus) {

    private val logger = InlineLogger()

    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception occurred during engine tick!" }
    }

    fun start() = runBlocking {
        supervisorScope {
            launch(Contexts.Engine + handler) {
                bus.emit(Startup)
            }
        }
        var count = 0L
        fixedRateTimer("Engine timer", period = ENGINE_DELAY) {
            count++
            runBlocking {
                val millis = measureTimeMillis {
                    supervisorScope {
                        withContext(Contexts.Engine + handler) {
                            bus.emit(TickInput)
                        }
                        withContext(Contexts.Engine + handler) {
                            bus.emit(TickAction)
                        }
                        withContext(Contexts.Engine + handler) {
                            bus.emit(Tick)
                        }
                        withContext(Contexts.Engine + handler) {
                            bus.emit(TickUpdate)
                        }
                    }
                }
                if (millis >= 5) {
                    logger.info { "Tick $count took ${millis}ms" }
                }
            }
        }
    }

    companion object {
        private const val ENGINE_DELAY = 600L
    }
}
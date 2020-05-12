package rs.dusk.engine

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.event.EventBus
import rs.dusk.utility.get
import rs.dusk.utility.inject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
class Engine {

    private val executor = Executors.newSingleThreadScheduledExecutor()

    class TickTask(private val tasks: EngineTasks) : Runnable {

        private val logger = InlineLogger()
        private val bus: EventBus by inject()

        override fun run() {
            try {
                bus.emit(Tick)
                tasks.forEach {
                    try {
                        it.run()
                    } catch (t: Throwable) {
                        logger.error(t) { "Exception occurred during engine tick! $it" }
                        t.printStackTrace()
                    }
                }
            } catch (t: Throwable) {
                logger.error(t) { "Exception occurred during engine tick!" }
                t.printStackTrace()
            }
        }
    }

    fun start() {
        val tasks: EngineTasks = get()
        val tick = TickTask(tasks)
        executor.scheduleAtFixedRate(tick, ENGINE_DELAY, ENGINE_DELAY, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        executor.shutdownNow()
    }

    companion object {
        private const val ENGINE_DELAY = 600L
    }
}
package rs.dusk.engine

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.utility.get
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
class Engine {

    private val executor = Executors.newSingleThreadScheduledExecutor()

    class Tick(private val tasks: EngineTasks) : Runnable {

        private val logger = InlineLogger()

        override fun run() {
            try {
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
        val tick = Tick(tasks)
        executor.scheduleAtFixedRate(tick, ENGINE_DELAY, ENGINE_DELAY, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        executor.shutdownNow()
    }

    companion object {
        private const val ENGINE_DELAY = 600L
    }
}
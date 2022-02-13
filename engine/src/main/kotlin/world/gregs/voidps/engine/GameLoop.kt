package world.gregs.voidps.engine

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.tick.Scheduler
import world.gregs.voidps.engine.utility.get
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.system.measureNanoTime

class GameLoop(
    private val service: ScheduledExecutorService,
    private val stages: List<Runnable>
) : Runnable {

    private val logger = InlineLogger()

    override fun run() {
        try {
            tick++
            timedLoop(tick)
        } catch (t: Throwable) {
            logger.error(t) { "Error in game loop!" }
        }
    }

    fun start() {
        service.scheduleAtFixedRate(this, 0, ENGINE_DELAY, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        service.shutdown()
    }

    private fun timedLoop(tick: Long) {
        val nano = measureNanoTime { loopTickStages() }
        printTickIfOverThreshold(nano, tick)
    }

    private fun printTickIfOverThreshold(nano: Long, tick: Long) {
        val millis = TimeUnit.NANOSECONDS.toMillis(nano)
//        if (millis >= MILLI_THRESHOLD) {
        logger.info { "Tick $tick took ${millis}ms" }
//        }
    }

    private fun loopTickStages() {
        for (stage in stages) {
            val took = measureNanoTime {
                runBlocking(Contexts.Game) {
                    withTimeout(ENGINE_DELAY) {
                        stage.run()
                    }
                }
            }
            val millis = TimeUnit.NANOSECONDS.toMillis(took)
            if (millis >= MILLI_THRESHOLD) {
                logger.info { "${stage::class.simpleName} took ${millis}ms" }
            }
        }
    }

    companion object {
        var tick: Long = 0L
        private const val ENGINE_DELAY = 600L
        private const val MILLI_THRESHOLD = 5

        suspend fun await(): Long = suspendCancellableCoroutine { continuation ->
            get<Scheduler>().add {
                continuation.resume(it)
            }
        }
    }
}
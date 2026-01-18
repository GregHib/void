package world.gregs.voidps.engine

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class GameLoop(
    private val stages: List<Runnable>,
    private val delay: Long = ENGINE_DELAY,
) {
    private val logger = InlineLogger()

    fun start(scope: CoroutineScope) = scope.launch {
        var start: Long
        var took: Long
        try {
            while (isActive) {
                start = System.nanoTime()
                val stageTimes = tick()
                took = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
                for (listener in tickListeners) {
                    listener.invoke(tick, took, stageTimes)
                }
                if (took > MILLI_WARNING_THRESHOLD) {
                    logger.warn { "Tick $tick took ${took}ms" }
                } else if (took > MILLI_THRESHOLD) {
                    logger.debug { "Tick $tick took ${took}ms" }
                }
                delay(delay - took)
                tick++
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                logger.error(e) { "Error in game loop!" }
            }
        }
    }

    fun tick(): Map<String, Long> {
        val stageTimes = mutableMapOf<String, Long>()
        for (stage in stages) {
            val (name, time) = tick(stage)
            stageTimes[name] = time
        }
        return stageTimes
    }

    fun tick(stage: Runnable): Pair<String, Long> {
        val stageStart = System.nanoTime()
        stage.run()
        val stageTook = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - stageStart)
        if (stageTook > MILLI_WARNING_THRESHOLD) {
            logger.warn { "${stage::class.simpleName} took ${stageTook}ms" }
        } else if (stageTook > MILLI_THRESHOLD) {
            logger.debug { "${stage::class.simpleName} took ${stageTook}ms" }
        }
        return stage::class.simpleName!! to stageTook
    }

    companion object {
        val tickListeners = mutableListOf<(Int, Long, Map<String, Long>) -> Unit>()
        var tick: Int = 0
        private const val ENGINE_DELAY = 600L
        private const val MILLI_THRESHOLD = 25L
        private const val MILLI_WARNING_THRESHOLD = 100L
    }
}

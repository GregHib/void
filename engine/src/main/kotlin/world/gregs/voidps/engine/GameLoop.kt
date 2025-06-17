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
                tick()
                took = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
                if (took > MILLI_WARNING_THRESHOLD) {
                    logger.warn { "Tick $tick took ${took}ms" }
                } else if (took > MILLI_THRESHOLD) {
                    logger.debug { "Tick $tick took ${took}ms" }
                }
                delay(delay - took)
                tick++
            }
        } catch (e: Exception) {
            logger.error(e) { "Error in game loop!" }
        }
    }

    fun tick() {
        for (stage in stages) {
            tick(stage)
        }
    }

    fun tick(stage: Runnable) {
        val stageStart = System.nanoTime()
        stage.run()
        val stageTook = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - stageStart)
        if (stageTook > MILLI_WARNING_THRESHOLD) {
            logger.warn { "${stage::class.simpleName} took ${stageTook}ms" }
        } else if (stageTook > MILLI_THRESHOLD) {
            logger.debug { "${stage::class.simpleName} took ${stageTook}ms" }
        }
    }

    companion object {
        var tick: Int = 0
        private const val ENGINE_DELAY = 600L
        private const val MILLI_THRESHOLD = 0L
        private const val MILLI_WARNING_THRESHOLD = 100L
    }
}

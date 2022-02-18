package world.gregs.voidps.engine

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import world.gregs.voidps.engine.action.Contexts
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class GameLoop(
    private val stages: List<Runnable>,
    override val coroutineContext: CoroutineContext = Contexts.Game
) : CoroutineScope {

    private val logger = InlineLogger()
    private lateinit var job: Job

    fun start() {
        try {
            var start: Long
            var took: Long
            job = launch {
                while (isActive) {
                    start = System.nanoTime()
                    for (stage in stages) {
                        tick(stage)
                        yield()
                    }
                    took = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
                    if (took > MILLI_THRESHOLD) {
                        logger.info { "Tick $tick took ${took}ms" }
                    }
                    delay(ENGINE_DELAY - took)
                    tick++
                }
            }
        } catch (t: Throwable) {
            logger.error(t) { "Error in game loop!" }
        }
    }

    fun tick(stage: Runnable) {
        val stageStart = System.nanoTime()
        stage.run()
        val stageTook = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - stageStart)
        if (stageTook > MILLI_THRESHOLD) {
            logger.info { "${stage::class.simpleName} took ${stageTook}ms" }
        }
    }

    fun stop() {
        job.cancel()
    }

    companion object {
        var tick: Long = 0L
        private const val ENGINE_DELAY = 600L
        private const val MILLI_THRESHOLD = 0
    }
}
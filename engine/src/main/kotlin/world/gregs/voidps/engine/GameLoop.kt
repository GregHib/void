package world.gregs.voidps.engine

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.character.player.Player
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

/**
 * @author GregHib <greg@gregs.world>
 * @since April 22, 2020
 */
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
        if (millis >= MILLI_THRESHOLD) {
            logger.info { "Tick $tick took ${millis}ms" }
        }
    }

    private fun loopTickStages() = runBlocking(Contexts.Game) {
        for (stage in stages) {
            val took = measureNanoTime { stage.run() }
            val millis = TimeUnit.NANOSECONDS.toMillis(took)
            if (millis >= MILLI_THRESHOLD) {
                logger.info { "${stage::class.simpleName} took ${millis}ms" }
            }
            yield()
        }
    }

    companion object {
        var tick: Long = 0L
        var flow = MutableStateFlow(tick)
            private set
        private const val ENGINE_DELAY = 600L
        private const val MILLI_THRESHOLD = 5

        // Work-around for https://github.com/mockk/mockk/issues/481
        fun setTestFlow(flow: MutableStateFlow<Long>) {
            this.flow = flow
        }
    }
}

/**
 * Executes a task after [ticks]
 */
fun delay(ticks: Int = 0, task: (Long) -> Unit) = GlobalScope.launch(Contexts.Game) {
    repeat(ticks) {
        GameLoop.flow.singleOrNull()
    }
    task.invoke(GameLoop.tick)
}

/**
 * Executes a task after [ticks], cancelling if player logs out FIXME - use player events
 */
fun delay(player: Player, ticks: Int = 0, task: (Long) -> Unit) {
    delay(ticks) {
        if (player.action.type != ActionType.Logout) {
            task.invoke(GameLoop.tick)
        }
    }
}

/**
 * Syncs task with the start of the current or next tick
 */
@Suppress("unused")
fun sync(task: (Long) -> Unit) {
    delay(1) {
        task.invoke(GameLoop.tick)
    }
}
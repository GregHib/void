package world.gregs.voidps.engine

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.singleOrNull
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.utility.get
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
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
        var flow = MutableStateFlow(tick)
            private set

        private const val ENGINE_DELAY = 600L
        private const val MILLI_THRESHOLD = 5

        suspend fun await() = flow.singleOrNull()

        // Work-around for https://github.com/mockk/mockk/issues/481
        fun setTestFlow(flow: MutableStateFlow<Long>) {
            this.flow = flow
        }
    }
}

/**
 * Executes a task after [ticks]
 */
fun delay(ticks: Int = 0, loop: Boolean = false, task: suspend (Long) -> Unit) = GlobalScope.launch(Contexts.Game) {
    if (loop) {
        while (isActive) {
            repeat(ticks) {
                GameLoop.await()
            }
            task.invoke(GameLoop.tick)
        }
    } else {
        repeat(ticks) {
            GameLoop.await()
        }
        task.invoke(GameLoop.tick)
    }
}

/**
 * Executes a task after [ticks], cancelling if player logs out
 */
inline fun <reified T : Entity> delay(entity: T, ticks: Int = 0, loop: Boolean = false, noinline task: suspend (Long) -> Unit): Job {
    val job = delay(ticks, loop, task)
    entity.events.on<T, Unregistered> {
        job.cancel()
    }
    return job
}

/**
 * Executes a task after [ticks], cancelling if the character is unregistered
 */
inline fun <reified T : Character> delay(entity: T, ticks: Int = 0, loop: Boolean = false, noinline task: suspend (Long) -> Unit): Job {
    val job = delay(ticks, loop, task)
    entity.events.on<T, Unregistered> {
        job.cancel()
    }
    return job
}

/**
 * Syncs task with the start of the current or next tick
 */
@Suppress("unused")
fun sync(task: suspend (Long) -> Unit) {
    val scheduler: Scheduler = get()
    scheduler.sync(task)
}
package world.gregs.voidps.engine.tick

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.getOrPut
import world.gregs.voidps.engine.utility.get
import java.util.concurrent.PriorityBlockingQueue
import kotlin.coroutines.CoroutineContext

/**
 * A scheduler for launching coroutines that aren't tied to a single action but can still require tick delays
 */
class Scheduler(
    override val coroutineContext: CoroutineContext = Contexts.Game
) : Runnable, CoroutineScope {

    fun launch(block: suspend CoroutineScope.() -> Unit) = launch(context = Contexts.Game, block = block)

    private val queue = PriorityBlockingQueue<Job>()

    fun add(ticks: Int = 0, loop: Boolean, cancelExecution: Boolean = false, block: Job.(Long) -> Unit) = add(ticks, if (loop) ticks else -1, cancelExecution, block)

    fun add(ticks: Int = 0, loop: Int = -1, cancelExecution: Boolean = false, block: Job.(Long) -> Unit): Job {
        val job = Job(GameLoop.tick + ticks, loop, cancelExecution, block)
        queue.offer(job)
        return job
    }

    override fun run() {
        while (queue.isNotEmpty()) {
            val job = queue.peek()
            if (job.tick > GameLoop.tick) {
                break
            }
            if (job.cancelled) {
                queue.poll()
                continue
            }
            try {
                job.block.invoke(job, GameLoop.tick)
                queue.poll()
                if (!job.cancelled) {
                    if (job.loop > 0) {
                        job.tick = GameLoop.tick + job.loop
                    }
                    if (job.tick > GameLoop.tick) {
                        queue.add(job)
                    }
                }
            } catch (e: Throwable) {
                logger.warn(e) { "Error in game loop sync task" }
            }
        }
    }

    fun clear() {
        queue.clear()
    }

    companion object {
        private val logger = InlineLogger()
    }
}

/**
 * Executes a task after [ticks]
 */
fun delay(ticks: Int = 0, loop: Boolean = false, cancelExecution: Boolean = false, task: Job.(Long) -> Unit): Job {
    return get<Scheduler>().add(ticks, loop, cancelExecution, task)
}

/**
 * Executes a task after [ticks], cancelling if player logs out
 */
fun <T : Entity> T.delay(ticks: Int = 0, loop: Boolean = false, cancelExecution: Boolean = false, task: Job.(Long) -> Unit): Job {
    val job = get<Scheduler>().add(ticks, loop, cancelExecution, task)
    getOrPut("delays") { mutableSetOf<Job>() }.add(job)
    return job
}
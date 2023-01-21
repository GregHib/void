package world.gregs.voidps.engine.timer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.tick.Job
import java.util.*

class Timers(private val character: Character) {
    private val queue = PriorityQueue<Job>()
    private val softQueue = PriorityQueue<Job>()

    fun add(ticks: Int = 0, loop: Int = -1, cancelExecution: Boolean = false, block: Job.(Long) -> Unit): Job {
        val job = Job(GameLoop.tick + ticks, loop, cancelExecution, block)
        queue.offer(job)
        return job
    }

    fun addSoft(ticks: Int = 0, loop: Int = -1, cancelExecution: Boolean = false, block: Job.(Long) -> Unit): Job {
        val job = Job(GameLoop.tick + ticks, loop, cancelExecution, block)
        softQueue.offer(job)
        return job
    }

    fun tick() {
        if (!character.hasEffect("delay")) {
            process(queue)
        }
        process(softQueue)
    }

    private fun process(queue: PriorityQueue<Job>) {
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
        for (job in queue) {
            job.cancel()
        }
        for (job in softQueue) {
            job.cancel()
        }
    }

    companion object {
        private val logger = InlineLogger()
    }
}

/**
 * Executes a task after [ticks], cancelling if player logs out
 */
fun Character.timer(ticks: Int = 0, loop: Boolean = false, cancelExecution: Boolean = false, block: Job.(Long) -> Unit) {
    timers.add(ticks, if (loop) ticks else -1, cancelExecution, block)
}

/**
 * Executes a task after [ticks], cancelling if player logs out
 */
fun Character.softTimer(ticks: Int = 0, loop: Boolean = false, cancelExecution: Boolean = false, block: Job.(Long) -> Unit) {
    timers.addSoft(ticks, if (loop) ticks else -1, cancelExecution, block)
}
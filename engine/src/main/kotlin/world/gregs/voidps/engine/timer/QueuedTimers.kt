package world.gregs.voidps.engine.timer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.tick.Job
import java.util.*

class QueuedTimers : Timers {

    private val queue = PriorityQueue<Job>()

    override fun add(ticks: Int, loop: Int, cancelExecution: Boolean, block: Job.(Long) -> Unit): Job {
        val job = Job(GameLoop.tick + ticks, loop, cancelExecution, block)
        queue.offer(job)
        return job
    }

    override fun tick() {
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

    override fun clear() {
        for (job in queue) {
            job.cancel()
        }
    }

    companion object {
        private val logger = InlineLogger()
    }
}
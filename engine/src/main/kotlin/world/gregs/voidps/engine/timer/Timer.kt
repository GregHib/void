package world.gregs.voidps.engine.timer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.tick.Job

class Timer : Timers {

    private var timer: Job? = null

    override fun add(ticks: Int, loop: Int, cancelExecution: Boolean, block: Job.(Long) -> Unit): Job {
        val job = Job(GameLoop.tick + ticks, loop, cancelExecution, block)
        timer = job
        return job
    }

    override fun tick() {
        val job = timer ?: return
        if (job.tick > GameLoop.tick) {
            return
        }
        if (job.cancelled) {
            timer = null
            return
        }
        try {
            job.block.invoke(job, GameLoop.tick)
            timer = null
            if (!job.cancelled) {
                if (job.loop > 0) {
                    job.tick = GameLoop.tick + job.loop
                }
                if (job.tick > GameLoop.tick) {
                    timer = job
                }
            }
        } catch (e: Throwable) {
            logger.warn(e) { "Error in game loop sync task" }
        }
    }

    override fun clear() {
        timer?.cancel()
        timer = null
    }

    companion object {
        private val logger = InlineLogger()
    }
}
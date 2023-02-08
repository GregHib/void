package world.gregs.voidps.engine.timer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

abstract class Timers : Runnable {
    abstract fun add(ticks: Int = 0, loop: Int = -1, cancelExecution: Boolean = false, block: Job.(Long) -> Unit): Job
    abstract fun clear()

    internal abstract fun add(job: Job)
    internal abstract fun poll()

    internal fun tick(job: Job): Boolean {
        if (job.tick > GameLoop.tick) {
            return false
        }
        if (job.cancelled) {
            poll()
            return true
        }
        try {
            job.block.invoke(job, GameLoop.tick)
            poll()
            if (!job.cancelled) {
                if (job.loop > 0) {
                    job.tick = GameLoop.tick + job.loop
                }
                if (job.tick > GameLoop.tick) {
                    add(job)
                }
            }
        } catch (e: Throwable) {
            logger.warn(e) { "Error in timer task" }
        }
        return true
    }

    companion object {
        private val logger = InlineLogger()
    }
}

fun Player.timer(ticks: Int = 0, loop: Boolean = false, cancelExecution: Boolean = false, block: Job.(Long) -> Unit): Job {
    return normalTimers.add(ticks, if (loop) ticks else -1, cancelExecution, block)
}

fun Character.softTimer(ticks: Int = 0, loop: Boolean = false, cancelExecution: Boolean = false, block: Job.(Long) -> Unit): Job {
    return timers.add(ticks, if (loop) ticks else -1, cancelExecution, block)
}
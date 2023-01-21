package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.GameLoop

class Job(
    var tick: Long,
    var loop: Int,
    private val cancelExecution: Boolean = false,
    val block: Job.(Long) -> Unit
) : Comparable<Job> {
    var cancelled = false
        private set

    fun cancel() {
        if (cancelExecution) {
            block.invoke(this, GameLoop.tick)
        }
        tick = 0
        cancelled = true
    }

    override fun compareTo(other: Job): Int {
        return tick.compareTo(other.tick)
    }
}
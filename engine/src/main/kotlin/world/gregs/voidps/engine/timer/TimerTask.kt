package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.GameLoop

/**
 * Stores the [name] of a timer to call every [interval]
 * The [nextTick] to emit a [TimerApi.tick]
 */
data class TimerTask(
    val name: String,
    private val interval: Int,
) : Comparable<TimerTask> {
    var nextTick: Int = GameLoop.tick + interval
        private set

    fun ready() = GameLoop.tick >= nextTick

    fun reset() {
        next(interval)
    }

    fun next(interval: Int = this.interval) {
        nextTick = GameLoop.tick + interval
    }

    override fun compareTo(other: TimerTask): Int = nextTick.compareTo(other.nextTick)

}

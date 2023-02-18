package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.GameLoop

/**
 * Stores the [name] of a timer to call every [interval]
 * The [nextTick] to emit a [TimerTick]
 */
data class Timer(
    val name: String,
    private val interval: Int
) : Comparable<Timer> {
    var nextTick: Long = GameLoop.tick + interval
        private set

    fun ready() = GameLoop.tick >= nextTick

    fun reset() {
        nextTick = GameLoop.tick + interval
    }

    override fun compareTo(other: Timer): Int {
        return nextTick.compareTo(other.nextTick)
    }
}
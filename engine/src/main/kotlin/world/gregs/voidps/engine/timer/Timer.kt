package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.GameLoop

/**
 * which is invoked every [interval].
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

    @Deprecated("")
    fun cancel() {
        nextTick = -1
    }

    override fun compareTo(other: Timer): Int {
        return nextTick.compareTo(other.nextTick)
    }
}
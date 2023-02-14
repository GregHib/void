package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.GameLoop

/**
 * Represents a [block] which is invoked every [interval] ticks until [cancelled].
 * [callOnCancel] optionally invokes [block] when [cancel] is called.
 */
data class Timer(
    private val interval: Int,
    private val callOnCancel: Boolean = false,
    val block: Timer.(Long) -> Unit
) : Comparable<Timer> {
    var cancelled = false
        private set
    var count = 0L
        private set
    var nextTick: Long = GameLoop.tick + interval
        private set

    fun ready() = GameLoop.tick >= nextTick

    fun resume() {
        block.invoke(this, count++)
        nextTick = GameLoop.tick + interval
    }

    fun cancel() {
        nextTick = -1
        cancelled = true
        if (callOnCancel) {
            block.invoke(this, count)
        }
        count = -1
    }

    override fun compareTo(other: Timer): Int {
        return nextTick.compareTo(other.nextTick)
    }
}
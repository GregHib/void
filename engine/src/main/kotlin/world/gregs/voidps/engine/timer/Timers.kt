package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

interface Timers : Runnable {
    fun add(interval: Int, cancelExecution: Boolean = false, block: Timer.(Long) -> Unit): Timer
    fun clear()
}

/**
 * Repeats every [interval] down when not delayed until cancelled
 */
fun Player.timer(interval: Int, cancelExecution: Boolean = false, block: Timer.(Long) -> Unit): Timer {
    return normalTimers.add(interval, cancelExecution, block)
}

/**
 * Repeats every [interval] until cancelled (or logout).
 */
fun Character.softTimer(interval: Int, cancelExecution: Boolean = false, block: Timer.(Long) -> Unit): Timer {
    return timers.add(interval, cancelExecution, block)
}
package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

interface Timers : Runnable {
    fun add(name: String, interval: Int, cancelExecution: Boolean = false, block: Timer.(Long) -> Unit): Timer
    fun contains(name: String): Boolean
    fun clear(name: String)
    fun clearAll()
}

/**
 * Repeats every [interval] down when not delayed until cancelled
 */
fun Player.timer(name: String, interval: Int, cancelExecution: Boolean = false, block: Timer.(Long) -> Unit): Timer {
    return normalTimers.add(name, interval, cancelExecution, block)
}

fun Player.stopTimer(name: String) {
    normalTimers.clear(name)
}

/**
 * Repeats every [interval] until cancelled (or logout).
 */
fun Character.softTimer(name: String, interval: Int, cancelExecution: Boolean = false, block: Timer.(Long) -> Unit): Timer {
    return timers.add(name, interval, cancelExecution, block)
}

fun Character.stopSoftTimer(name: String) {
    timers.clear(name)
}
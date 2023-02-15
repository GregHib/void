package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

interface Timers : Runnable {
    fun start(name: String, interval: Int, cancelExecution: Boolean = false, block: Timer.(Long) -> Unit = {})
    fun contains(name: String): Boolean
    fun stop(name: String)
    fun clearAll()
}

/**
 * Repeats every [interval] down when not delayed until cancelled
 */
fun Player.timer(name: String, interval: Int, cancelExecution: Boolean = false, block: Timer.(Long) -> Unit = {}) {
    timers.start(name, interval, cancelExecution, block)
}

fun Player.stopTimer(name: String) {
    timers.stop(name)
}

/**
 * Repeats every [interval] until cancelled (or logout).
 */
fun Character.softTimer(name: String, interval: Int, cancelExecution: Boolean = false, block: Timer.(Long) -> Unit = {}) {
    softTimers.start(name, interval, cancelExecution, block)
}

fun Character.stopSoftTimer(name: String) {
    softTimers.stop(name)
}
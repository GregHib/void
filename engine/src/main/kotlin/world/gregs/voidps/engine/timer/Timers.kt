package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

interface Timers : Runnable {
    fun restart(name: String) = start(name, restart = true)
    fun start(name: String, restart: Boolean = false): Boolean
    fun contains(name: String): Boolean
    fun stop(name: String)
    fun clearAll()
    fun start(name: String, interval: Int, cancelExecution: Boolean = false, persist: Boolean = false) {

    }
    fun toggle(name: String, interval: Int, cancelExecution: Boolean = false, persist: Boolean = false) {
        if (contains(name)) {
            stop(name)
            return
        }
        start(name)
    }
    fun hasOrStart(name: String, interval: Int, cancelExecution: Boolean = false, persist: Boolean = false) {
        if (contains(name)) {
            return
        }
        start(name)
    }
    fun startIfAbsent(name: String) {
        if (contains(name)) {
            return
        }
        start(name)
    }
}

/**
 * Repeats every [cycles] down when not delayed until cancelled
 */
fun Player.timer(name: String) {
    timers.start(name)
}
fun Player.timer(name: String, cycles: Int, cancelExecution: Boolean = false, persist: Boolean = false, block: Timer.(Long) -> Unit = {}) {
    timers.start(name)
}

fun Player.stopTimer(name: String) {
    timers.stop(name)
}

/**
 * Repeats every [cycles] until cancelled (or logout).
 */
fun Character.softTimer(name: String) {
    softTimers.start(name)
}
fun Character.softTimer(name: String, cycles: Int, cancelExecution: Boolean = false, persist: Boolean = false, block: Timer.(Long) -> Unit = {}) {
    softTimers.start(name)
}

fun Character.stopSoftTimer(name: String) {
    softTimers.stop(name)
}
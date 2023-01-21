package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player

interface Timers {
    fun add(ticks: Int = 0, loop: Int = -1, cancelExecution: Boolean = false, block: Job.(Long) -> Unit): Job
    fun tick()
    fun clear()
}

fun Character.timer(ticks: Int = 0, loop: Boolean = false, cancelExecution: Boolean = false, block: Job.(Long) -> Unit): Job {
    return if (this is Player) {
        normalTimers.add(ticks, if (loop) ticks else -1, cancelExecution, block)
    } else {
        timers.add(ticks, if (loop) ticks else -1, cancelExecution, block)
    }
}

fun Player.softTimer(ticks: Int = 0, loop: Boolean = false, cancelExecution: Boolean = false, block: Job.(Long) -> Unit) {
    timers.add(ticks, if (loop) ticks else -1, cancelExecution, block)
}
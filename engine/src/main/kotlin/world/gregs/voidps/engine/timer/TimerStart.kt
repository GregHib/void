package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class TimerStart(val timer: String, val restart: Boolean = false) : CancellableEvent() {
    var interval: Int = -1

    override fun size() = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_timer_start"
        1 -> timer
        2 -> dispatcher.identifier
        else -> ""
    }
}

fun timerStart(vararg timers: String, override: Boolean = true, block: suspend TimerStart.(Player) -> Unit) {
    for (timer in timers) {
        Events.handle("player_timer_start", timer, "player", override = override, handler = block)
    }
}

fun npcTimerStart(timer: String, npc: String = "*", override: Boolean = true, block: suspend TimerStart.(NPC) -> Unit) {
    Events.handle("npc_timer_start", timer, npc, override = override, handler = block)
}

fun characterTimerStart(timer: String, override: Boolean = true, block: suspend TimerStart.(Character) -> Unit) {
    Events.handle("player_timer_start", timer, "player", override = override, handler = block)
    Events.handle("npc_timer_start", timer, "*", override = override, handler = block)
}

fun worldTimerStart(timer: String, override: Boolean = true, block: suspend TimerStart.(World) -> Unit) {
    Events.handle("world_timer_start", timer, "world", override = override, handler = block)
}

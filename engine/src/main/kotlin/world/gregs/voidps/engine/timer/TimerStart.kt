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

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_timer_start"
        1 -> timer
        2 -> dispatcher.identifier
        else -> null
    }
}

fun timerStart(vararg timers: String, handler: suspend TimerStart.(Player) -> Unit) {
    for (timer in timers) {
        Events.handle("player_timer_start", timer, "player", handler = handler)
    }
}

fun npcTimerStart(timer: String, npc: String = "*", handler: suspend TimerStart.(NPC) -> Unit) {
    Events.handle("npc_timer_start", timer, npc, handler = handler)
}

fun characterTimerStart(timer: String, override: Boolean = true, handler: suspend TimerStart.(Character) -> Unit) {
    Events.handle("player_timer_start", timer, "player", override = override, handler = handler)
    Events.handle("npc_timer_start", timer, "*", handler = handler)
}

fun worldTimerStart(timer: String, handler: suspend TimerStart.(World) -> Unit) {
    Events.handle("world_timer_start", timer, "world", handler = handler)
}

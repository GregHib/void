package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class TimerStop(val timer: String, val logout: Boolean) : Event {

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_timer_stop"
        1 -> timer
        2 -> dispatcher.identifier
        else -> null
    }
}

fun timerStop(vararg timers: String, handler: suspend TimerStop.(Player) -> Unit) {
    for (timer in timers) {
        Events.handle("player_timer_stop", timer, "player", handler = handler)
    }
}

fun npcTimerStop(timer: String, npc: String = "*", handler: suspend TimerStop.(NPC) -> Unit) {
    Events.handle("npc_timer_stop", timer, npc, handler = handler)
}

fun characterTimerStop(timer: String, handler: suspend TimerStop.(Character) -> Unit) {
    Events.handle("player_timer_stop", timer, "player", handler = handler)
    Events.handle("npc_timer_stop", timer, "*", handler = handler)
}

fun worldTimerStop(timer: String, handler: suspend TimerStop.(World) -> Unit) {
    Events.handle("world_timer_stop", timer, "world", handler = handler)
}

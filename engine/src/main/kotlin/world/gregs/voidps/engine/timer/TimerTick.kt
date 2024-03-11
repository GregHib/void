package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class TimerTick(val timer: String) : CancellableEvent() {
    var nextInterval: Int = -1

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_timer_tick"
        1 -> timer
        2 -> dispatcher.identifier
        else -> null
    }
}

fun timerTick(vararg timers: String, handler: suspend TimerTick.(Player) -> Unit) {
    for (timer in timers) {
        Events.handle("player_timer_tick", timer, "player", handler = handler)
    }
}

fun npcTimerTick(timer: String, npc: String = "*", handler: suspend TimerTick.(NPC) -> Unit) {
    Events.handle("npc_timer_tick", timer, npc, handler = handler)
}

fun characterTimerTick(timer: String, handler: suspend TimerTick.(Character) -> Unit) {
    Events.handle("player_timer_tick", timer, "player", handler = handler)
    Events.handle("npc_timer_tick", timer, "*", handler = handler)
}

fun worldTimerTick(timer: String, handler: suspend TimerTick.(World) -> Unit) {
    Events.handle("world_timer_tick", timer, "world", handler = handler)
}

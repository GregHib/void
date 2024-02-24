package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.onWorld
import world.gregs.voidps.engine.event.onCharacter
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals

data class TimerStop(val timer: String, val logout: Boolean) : Event

fun timerStop(vararg timers: String, block: suspend TimerStop.(Player) -> Unit) {
    for (timer in timers) {
        on<TimerStop>({ wildcardEquals(timer, this.timer) }, block = block)
    }
}

fun npcTimerStop(timer: String, npc: String = "*", block: suspend TimerStop.(NPC) -> Unit) {
    if (npc == "*") {
        onNPC<TimerStop>({ wildcardEquals(timer, this.timer) }, block = block)
    } else {
        onNPC<TimerStop>({ wildcardEquals(timer, this.timer) && wildcardEquals(npc, it.id) }, block = block)
    }
}

fun characterTimerStop(timer: String, block: suspend TimerStop.(Character) -> Unit) {
    onCharacter<TimerStop>({ wildcardEquals(timer, this.timer) }, block = block)
}

fun worldTimerStop(timer: String, block: suspend TimerStop.() -> Unit) {
    onWorld<TimerStop>({ wildcardEquals(timer, this.timer) }) {
        block.invoke(this)
    }
}

package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.onWorld
import world.gregs.voidps.engine.event.onCharacter
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals

data class TimerTick(val timer: String) : CancellableEvent() {
    var nextInterval: Int = -1
}

fun timerTick(vararg timers: String, block: suspend TimerTick.(Player) -> Unit) {
    for (timer in timers) {
        on<TimerTick>({ wildcardEquals(timer, this.timer) }, block = block)
    }
}

fun npcTimerTick(timer: String, npc: String = "*", block: suspend TimerTick.(NPC) -> Unit) {
    if (npc == "*") {
        onNPC<TimerTick>({ wildcardEquals(timer, this.timer) }, block = block)
    } else {
        onNPC<TimerTick>({ wildcardEquals(timer, this.timer) && wildcardEquals(npc, it.id) }, block = block)
    }
}

fun characterTimerTick(timer: String, block: suspend TimerTick.(Character) -> Unit) {
    onCharacter<TimerTick>({ wildcardEquals(timer, this.timer) }, block = block)
}

fun worldTimerTick(timer: String, block: suspend TimerTick.() -> Unit) {
    onWorld<TimerTick>({ wildcardEquals(timer, this.timer) }) { _: World ->
        block.invoke(this)
    }
}

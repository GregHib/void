package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.onCharacter
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals

data class TimerStart(val timer: String, val restart: Boolean = false) : CancellableEvent() {
    var interval: Int = -1
}

fun timerStart(vararg timers: String, block: suspend TimerStart.(Player) -> Unit) {
    for (timer in timers) {
        on<TimerStart>({ wildcardEquals(timer, this.timer) }, block = block)
    }
}

fun npcTimerStart(timer: String, npc: String = "*", block: suspend TimerStart.(NPC) -> Unit) {
    if (npc == "*") {
        onNPC<TimerStart>({ wildcardEquals(timer, this.timer) }, block = block)
    } else {
        onNPC<TimerStart>({ wildcardEquals(timer, this.timer) && wildcardEquals(npc, it.id) }, block = block)
    }
}

fun characterTimerStart(timer: String, block: suspend TimerStart.(Character) -> Unit) {
    onCharacter<TimerStart>({ wildcardEquals(timer, this.timer) }, block = block)
}

fun worldTimerStart(timer: String, block: suspend TimerStart.() -> Unit) {
    on<TimerStart>({ wildcardEquals(timer, this.timer) }) { _: World ->
        block.invoke(this)
    }
}

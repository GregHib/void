package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class TimerStart(val timer: String, val restart: Boolean = false) : CancellableEvent() {
    var interval: Int = -1
}

fun timerStart(vararg timers: String, block: suspend TimerStart.(Player) -> Unit) {
    for (timer in timers) {
        on<TimerStart>({ wildcardEquals(timer, this.timer) }) { character: Player ->
            block.invoke(this, character)
        }
    }
}

fun npcTimerStart(timer: String, npc: String = "*", block: suspend TimerStart.(NPC) -> Unit) {
    if (npc == "*") {
        on<TimerStart>({ wildcardEquals(timer, this.timer) }) { character: NPC ->
            block.invoke(this, character)
        }
    } else {
        on<TimerStart>({ wildcardEquals(timer, this.timer) && wildcardEquals(npc, it.id) }) { character: NPC ->
            block.invoke(this, character)
        }
    }
}

fun characterTimerStart(timer: String, block: suspend TimerStart.(Character) -> Unit) {
    on<TimerStart>({ wildcardEquals(timer, this.timer) }) { character: Character ->
        block.invoke(this, character)
    }
}

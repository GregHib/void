package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class TimerTick(val timer: String) : CancellableEvent() {
    var nextInterval: Int = -1
}

fun timerTick(timer: String, block: suspend TimerTick.(Player) -> Unit) {
    on<TimerTick>({ wildcardEquals(timer, this.timer) }) { character: Player ->
        block.invoke(this, character)
    }
}

fun characterTimerTick(timer: String, block: suspend TimerTick.(Character) -> Unit) {
    on<TimerTick>({ wildcardEquals(timer, this.timer) }) { character: Character ->
        block.invoke(this, character)
    }
}

fun npcTimerTick(timer: String, npc: String = "*", block: suspend TimerTick.(NPC) -> Unit) {
    if (npc == "*") {
        on<TimerTick>({ wildcardEquals(timer, this.timer) }) { character: NPC ->
            block.invoke(this, character)
        }
    } else {
        on<TimerTick>({ wildcardEquals(timer, this.timer) && wildcardEquals(npc, it.id) }) { character: NPC ->
            block.invoke(this, character)
        }
    }
}
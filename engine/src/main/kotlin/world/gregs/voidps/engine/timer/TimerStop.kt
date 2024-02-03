package world.gregs.voidps.engine.timer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class TimerStop(val timer: String, val logout: Boolean) : Event

fun timerStop(filter: TimerStop.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend TimerStop.(Player) -> Unit) {
    on<TimerStop>(filter, priority, block)
}

fun characterTimerStop(filter: TimerStop.(Character) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend TimerStop.(Character) -> Unit) {
    on<TimerStop>(filter, priority, block)
}

fun npcTimerStop(filter: TimerStop.(NPC) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend TimerStop.(NPC) -> Unit) {
    on<TimerStop>(filter, priority, block)
}

fun timerStop(timer: String, block: suspend TimerStop.(Player) -> Unit) {
    on<TimerStop>({ wildcardEquals(timer, this.timer) }) { character: Player ->
        block.invoke(this, character)
    }
}

fun characterTimerStop(timer: String, block: suspend TimerStop.(Character) -> Unit) {
    on<TimerStop>({ wildcardEquals(timer, this.timer) }) { character: Character ->
        block.invoke(this, character)
    }
}

fun npcTimerStop(timer: String, npc: String = "*", block: suspend TimerStop.(NPC) -> Unit) {
    if (npc == "*") {
        on<TimerStop>({ wildcardEquals(timer, this.timer) }) { character: NPC ->
            block.invoke(this, character)
        }
    } else {
        on<TimerStop>({ wildcardEquals(timer, this.timer) && wildcardEquals(npc, it.id) }) { character: NPC ->
            block.invoke(this, character)
        }
    }
}
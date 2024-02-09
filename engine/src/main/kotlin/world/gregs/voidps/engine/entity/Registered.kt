package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

object Registered : Event

fun playerSpawn(priority: Priority = Priority.MEDIUM, block: suspend Registered.(Player) -> Unit) {
    on<Registered>(priority = priority, block = block)
}

fun npcSpawn(npc: String = "*", block: suspend (NPC) -> Unit) {
    if (npc == "*") {
        on<Registered> { character: NPC ->
            block.invoke(character)
        }
    } else {
        on<Registered>({ wildcardEquals(npc, it.id) }) { character: NPC ->
            block.invoke(character)
        }
    }
}

fun characterSpawn(block: suspend Registered.(Character) -> Unit) {
    on<Registered>(block = block)
}

fun worldSpawn(block: suspend () -> Unit) {
    on<Registered> { _: World ->
        block.invoke()
    }
}

package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

object Registered : Event

fun playerSpawn(priority: Priority = Priority.MEDIUM, block: suspend (Player) -> Unit) {
    on<Registered>(priority = priority) { character: Player ->
        block.invoke(character)
    }
}

fun npcSpawn(npc: String = "*", priority: Priority = Priority.MEDIUM, block: suspend (NPC) -> Unit) {
    if (npc == "*") {
        on<Registered>(priority = priority) { character: NPC ->
            block.invoke(character)
        }
    } else {
        on<Registered>({ wildcardEquals(npc, it.id) }, priority) { character: NPC ->
            block.invoke(character)
        }
    }
}

fun worldSpawn(priority: Priority = Priority.MEDIUM, block: suspend () -> Unit) {
    on<Registered>(priority = priority) { _: World ->
        block.invoke()
    }
}

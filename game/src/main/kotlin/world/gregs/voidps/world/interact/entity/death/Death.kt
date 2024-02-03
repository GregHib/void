package world.gregs.voidps.world.interact.entity.death

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

object Death : Event

fun playerDeath(filter: Death.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend Death.(Player) -> Unit) {
    on<Death>(filter, priority, block)
}

fun npcDeath(filter: Death.(NPC) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend Death.(NPC) -> Unit) {
    on<Death>(filter, priority, block)
}

fun playerDeath(priority: Priority = Priority.MEDIUM, block: suspend (Player) -> Unit) {
    on<Death>(priority = priority) { character: Player ->
        block.invoke(character)
    }
}

fun npcDeath(npc: String = "*", priority: Priority = Priority.MEDIUM, block: suspend (NPC) -> Unit) {
    if (npc == "*") {
        on<Death>(priority = priority) { character: NPC ->
            block.invoke(character)
        }
    } else {
        on<Death>({ wildcardEquals(npc, it.id) }, priority) { character: NPC ->
            block.invoke(character)
        }
    }
}
package world.gregs.voidps.world.interact.entity.death

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

object Death : Event

fun playerDeath(block: suspend Death.(Player) -> Unit) {
    on<Death>(block = block)
}

fun npcDeath(npc: String = "*", block: suspend Death.(NPC) -> Unit) {
    if (npc == "*") {
        on<Death>(block = block)
    } else {
        on<Death>({ wildcardEquals(npc, it.id) }, block = block)
    }
}

fun characterDeath(block: suspend Death.(Character) -> Unit) {
    on<Death>(block = block)
}
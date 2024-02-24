package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class HuntNPC(
    val mode: String,
    val target: NPC
) : Event

fun huntNPC(npc: String = "*", targetNpc: String = "*", mode: String = "*", block: suspend HuntNPC.(npc: NPC) -> Unit) {
    on<HuntNPC>({ wildcardEquals(npc, it.id) && wildcardEquals(targetNpc, target.id) && wildcardEquals(mode, this.mode) }, block = block)
}

fun huntNPCModes(vararg modes: String, block: suspend HuntNPC.(npc: NPC) -> Unit) {
    for(mode in modes) {
        on<HuntNPC>({ wildcardEquals(mode, this.mode) }, block = block)
    }
}
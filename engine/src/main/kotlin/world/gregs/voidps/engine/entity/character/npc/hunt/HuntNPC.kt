package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals

data class HuntNPC(
    val mode: String,
    val target: NPC
) : Event

fun huntNPC(npc: String = "*", targetNpc: String = "*", mode: String = "*", block: suspend HuntNPC.(npc: NPC) -> Unit) {
    onNPC<HuntNPC>({ wildcardEquals(npc, it.id) && wildcardEquals(targetNpc, target.id) && wildcardEquals(mode, this.mode) }, block = block)
}

fun huntNPCModes(vararg modes: String, block: suspend HuntNPC.(npc: NPC) -> Unit) {
    for(mode in modes) {
        onNPC<HuntNPC>({ wildcardEquals(mode, this.mode) }, block = block)
    }
}
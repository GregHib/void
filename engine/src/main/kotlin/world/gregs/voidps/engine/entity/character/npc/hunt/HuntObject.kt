package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals

data class HuntObject(
    val mode: String,
    val target: GameObject
) : Event

fun huntObject(npc: String = "*", gameObject: String = "*", mode: String = "*", block: suspend HuntObject.(npc: NPC) -> Unit) {
    onNPC<HuntObject>({ wildcardEquals(npc, it.id) && wildcardEquals(gameObject, target.id) && wildcardEquals(mode, this.mode) }, block = block)
}
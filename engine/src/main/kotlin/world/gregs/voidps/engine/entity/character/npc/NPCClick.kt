package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.event.Event

/**
 * NPC click before the attempt to walk within interact distance
 */
data class NPCClick(val npc: NPC, val option: String?) : Event {
    var cancel = false
}
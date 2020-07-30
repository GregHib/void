package rs.dusk.world.interact.npc.spawn

import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.npc.NPCEvent
import rs.dusk.engine.event.EventCompanion

data class NPCDespawn(override val npc: NPC) : NPCEvent() {
    companion object : EventCompanion<NPCDespawn>
}
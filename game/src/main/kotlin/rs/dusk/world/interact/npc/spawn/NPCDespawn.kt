package rs.dusk.world.interact.npc.spawn

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.character.npc.NPC
import rs.dusk.engine.model.entity.character.npc.NPCEvent

data class NPCDespawn(override val npc: NPC) : NPCEvent() {
    companion object : EventCompanion<NPCDespawn>
}
package rs.dusk.engine.model.entity.index.npc

import rs.dusk.engine.event.EventCompanion

data class NPCRegistered(override val npc: NPC) : NPCEvent() {
    companion object : EventCompanion<NPCRegistered>
}
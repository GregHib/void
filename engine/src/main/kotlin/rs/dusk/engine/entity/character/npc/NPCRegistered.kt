package rs.dusk.engine.entity.character.npc

import rs.dusk.engine.event.EventCompanion

data class NPCRegistered(override val npc: NPC) : NPCEvent() {
    companion object : EventCompanion<NPCRegistered>
}
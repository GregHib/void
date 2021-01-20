package world.gregs.void.engine.entity.character.npc

import world.gregs.void.engine.event.EventCompanion

data class NPCRegistered(override val npc: NPC) : NPCEvent() {
    companion object : EventCompanion<NPCRegistered>
}
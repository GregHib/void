package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.event.EventCompanion

data class NPCRegistered(override val npc: NPC) : NPCEvent() {
    companion object : EventCompanion<NPCRegistered>
}
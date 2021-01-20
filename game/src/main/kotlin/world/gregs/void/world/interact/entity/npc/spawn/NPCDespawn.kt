package world.gregs.void.world.interact.entity.npc.spawn

import world.gregs.void.engine.entity.character.npc.NPC
import world.gregs.void.engine.entity.character.npc.NPCEvent
import world.gregs.void.engine.event.EventCompanion

data class NPCDespawn(override val npc: NPC) : NPCEvent() {
    companion object : EventCompanion<NPCDespawn>
}
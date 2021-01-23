package world.gregs.voidps.world.interact.entity.npc.spawn

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCEvent
import world.gregs.voidps.engine.event.EventCompanion

data class NPCDespawn(override val npc: NPC) : NPCEvent() {
    companion object : EventCompanion<NPCDespawn>
}
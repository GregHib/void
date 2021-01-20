package world.gregs.void.engine.entity.character.move

import world.gregs.void.engine.entity.character.npc.NPC
import world.gregs.void.engine.entity.character.npc.NPCEvent
import world.gregs.void.engine.event.EventCompanion
import world.gregs.void.engine.map.Tile

data class NPCMoved(override val npc: NPC, val from: Tile, val to: Tile) : NPCEvent() {
    companion object : EventCompanion<NPCMoved>
}
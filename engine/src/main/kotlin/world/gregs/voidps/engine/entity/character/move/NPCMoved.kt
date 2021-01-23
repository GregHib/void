package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCEvent
import world.gregs.voidps.engine.event.EventCompanion
import world.gregs.voidps.engine.map.Tile

data class NPCMoved(override val npc: NPC, val from: Tile, val to: Tile) : NPCEvent() {
    companion object : EventCompanion<NPCMoved>
}
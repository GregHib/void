package rs.dusk.engine.entity.character.move

import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.npc.NPCEvent
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.map.Tile

data class NPCMoved(override val npc: NPC, val from: Tile, val to: Tile) : NPCEvent() {
    companion object : EventCompanion<NPCMoved>
}
package rs.dusk.world.interact.npc.spawn

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.character.npc.NPC
import rs.dusk.engine.model.world.Tile

data class NPCSpawn(val id: Int, val tile: Tile, val direction: Direction) : Event<NPC>() {
    companion object : EventCompanion<NPCSpawn>
}
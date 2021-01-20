package world.gregs.void.world.interact.entity.npc.spawn

import world.gregs.void.engine.entity.Direction
import world.gregs.void.engine.entity.character.npc.NPC
import world.gregs.void.engine.event.Event
import world.gregs.void.engine.event.EventCompanion
import world.gregs.void.engine.map.Tile

data class NPCSpawn(val id: Int, val tile: Tile, val direction: Direction) : Event<NPC>() {
    companion object : EventCompanion<NPCSpawn>
}
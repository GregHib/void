package world.gregs.voidps.world.interact.entity.npc.spawn

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventCompanion
import world.gregs.voidps.engine.map.Tile

data class NPCSpawn(val id: Int, val tile: Tile, val direction: Direction) : Event<NPC>() {
    companion object : EventCompanion<NPCSpawn>
}
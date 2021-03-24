package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.npc.NPCEvent
import world.gregs.voidps.engine.map.Tile

data class NPCMoved(val from: Tile, val to: Tile) : NPCEvent
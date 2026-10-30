package world.gregs.voidps.engine.entity.character.npc

import kotlinx.serialization.Serializable
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

@Serializable
class NPCSpawn(val id: String, val tile: Tile, val direction: Direction)

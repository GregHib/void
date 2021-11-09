package world.gregs.voidps.engine.map.spawn

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile

data class NPCSpawn(
    val id: String,
    val tile: Tile,
    val delay: Int,
    val direction: Direction
) {

    companion object {
        fun fromMap(it: Map<String, Any>) = NPCSpawn(
            id = it["id"] as String,
            tile = Tile.fromMap(it),
            delay = it["delay"] as? Int ?: 60,
            direction = Direction.fromMap(it)
        )
    }
}
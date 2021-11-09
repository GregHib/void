package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile

data class NPCSpawn(
    val id: String,
    val tile: Tile,
    val delay: Int = 0,
    val direction: Direction = Direction.NONE
) {

    companion object {
        fun fromMap(it: Map<String, Any>) = NPCSpawn(
            id = it["id"] as String,
            tile = Tile(it["x"] as Int, it["y"] as Int, it["z"] as? Int ?: it["plane"] as? Int ?: 0),
            delay = it["delay"] as? Int ?: 60,
            direction = Direction.valueOf(it["direction"] as? String ?: "NONE")
        )
    }
}
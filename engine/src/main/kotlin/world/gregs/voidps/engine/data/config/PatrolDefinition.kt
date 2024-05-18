package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.type.Tile

data class PatrolDefinition(
    override var stringId: String = "",
    val waypoints: List<Pair<Tile, Int>> = emptyList(),
    override var extras: Map<String, Any>? = null
) : Extra {
    companion object {
        operator fun invoke(key: String, map: Map<String, Any>): PatrolDefinition {
            val extras = map.toMutableMap()
            val points = extras.remove("points") as? List<Map<String, Any>> ?: emptyList()
            val waypoints = points.map {
                val delay = it["delay"] as? Int ?: 0
                Tile(it["x"] as Int, it["y"] as Int, it["level"] as? Int ?: 0) to delay
            }
            return PatrolDefinition(key, waypoints, extras)
        }
    }
}
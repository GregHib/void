package world.gregs.voidps.engine.data.config

import world.gregs.voidps.cache.definition.Parameterized
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.type.Tile

data class PatrolDefinition(
    override var stringId: String = "",
    val waypoints: List<Pair<Tile, Int>> = emptyList(),
    override var params: Map<Int, Any>? = null,
) : Parameterized {
    companion object {
        operator fun invoke(key: String, map: Map<Int, Any>): PatrolDefinition {
            val params = map.toMutableMap()
            val points = params.remove(Params.POINTS) as? List<Map<String, Any>> ?: emptyList()
            val waypoints = points.map {
                val delay = it["delay"] as? Int ?: 0
                Tile(it["x"] as Int, it["y"] as Int, it["level"] as? Int ?: 0) to delay
            }
            return PatrolDefinition(key, waypoints, params)
        }
    }
}

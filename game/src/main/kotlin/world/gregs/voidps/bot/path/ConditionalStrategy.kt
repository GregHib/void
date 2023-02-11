package world.gregs.voidps.bot.path

import world.gregs.voidps.bot.navigation.graph.NavigationGraph
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.MapArea

class ConditionalStrategy(
    val graph: NavigationGraph,
    val block: (MapArea) -> Boolean
) : NodeTargetStrategy() {

    var area: MapArea? = null

    override fun reached(node: Any): Boolean {
        if (node !is Tile) {
            return false
        }
        for (area in graph.areas(node)) {
            if (block(area)) {
                this.area = area
                return true
            }
        }
        return false
    }
}
package world.gregs.voidps.bot.path

import world.gregs.voidps.bot.navigation.graph.NavigationGraph
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.type.Tile

class ConditionalStrategy(
    val graph: NavigationGraph,
    val block: (AreaDefinition) -> Boolean
) : NodeTargetStrategy() {

    var area: AreaDefinition? = null

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
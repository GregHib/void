package world.gregs.voidps.bot.path

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area

class AreaStrategy(
    val area: Area
) : NodeTargetStrategy() {

    override fun reached(node: Any): Boolean {
        return node is Tile && node in area
    }
}
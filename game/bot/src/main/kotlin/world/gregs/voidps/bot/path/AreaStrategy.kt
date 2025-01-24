package world.gregs.voidps.bot.path

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Area

class AreaStrategy(
    val area: Area
) : NodeTargetStrategy() {

    override fun reached(node: Any): Boolean {
        return node is Tile && node in area
    }
}
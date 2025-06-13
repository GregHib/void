package content.bot.interact.path

import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

class AreaStrategy(
    val area: Area,
) : NodeTargetStrategy() {

    override fun reached(node: Any): Boolean = node is Tile && node in area
}

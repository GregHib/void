package world.gregs.voidps.bot

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area

data class Task(
    val name: String,
    val block: suspend Bot.() -> Unit,
    val area: Area? = null,
    var spaces: Int = 1,
    val requirements: Collection<Bot.() -> Boolean> = emptySet()
) {
    fun full() = spaces <= 0

    fun distanceTo(tile: Tile): Int {
        return if (area == null) 0 else tile.distanceTo(area.random())
    }
}
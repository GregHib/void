package content.bot.interact.navigation.graph

import world.gregs.config.ConfigReader
import world.gregs.voidps.type.Tile

fun ConfigReader.readTile(): Tile {
    var x = 0
    var y = 0
    var level = 0
    while (nextEntry()) {
        when (val key = key()) {
            "x" -> x = int()
            "y" -> y = int()
            "level" -> level = int()
            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
        }
    }
    return Tile(x, y, level)
}

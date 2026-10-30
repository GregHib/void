package world.gregs.voidps.engine.entity.item.floor

import com.github.michaelbull.logging.InlineLogger
import world.gregs.config.Config
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Tile

private val logger = InlineLogger()

fun loadItemSpawns(paths: List<String>) {
    timedLoad("item spawn") {
        val membersWorld = World.members
        var count = 0
        for (path in paths) {
            Config.fileReader(path) {
                while (nextPair()) {
                    require(key() == "spawns")
                    while (nextElement()) {
                        var id = ""
                        var amount = 1
                        var x = 0
                        var y = 0
                        var level = 0
                        var delay = 60
                        var members = false
                        while (nextEntry()) {
                            when (val key = key()) {
                                "id" -> id = string()
                                "x" -> x = int()
                                "y" -> y = int()
                                "level" -> level = int()
                                "amount", "charges" -> amount = int()
                                "delay" -> delay = int()
                                "members" -> members = boolean()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        if (!membersWorld && members) {
                            continue
                        }
                        val tile = Tile(x, y, level)
                        if (ItemDefinitions.getOrNull(id) == null) {
                            logger.warn { "Invalid item spawn id '$id' in $path." }
                        }
                        FloorItems.add(tile, id, amount, respawnTicks = delay)
                        count++
                    }
                }
            }
        }
        count
    }
}

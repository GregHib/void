package world.gregs.voidps.engine.entity.item.floor

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.timedLoad

class ItemSpawns(
    private val zones: MutableMap<Int, ItemSpawn> = Int2ObjectOpenHashMap()
) {
    val size: Int
        get() = zones.size

    fun set(tile: Tile, spawn: ItemSpawn) {
        zones[tile.id] = spawn
    }

    fun get(tile: Tile): ItemSpawn? = zones[tile.id]

    fun clear() {
        zones.clear()
    }
}

fun loadItemSpawns(
    items: FloorItems,
    spawns: ItemSpawns,
    paths: List<String>
) {
    timedLoad("item spawn") {
        spawns.clear()
        val membersWorld = World.members
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
                        spawns.set(tile, ItemSpawn(id, amount, delay))
                        items.add(tile, id, amount)
                    }
                }
            }
        }
        spawns.size
    }
}
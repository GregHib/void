package world.gregs.voidps.world.interact.world.spawn

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class Teleports {

    private lateinit var teleports: Map<Int, Map<String, Teleport>>

    fun get(id: Int, tile: Tile, option: String): Teleport? {
        val teleport = teleports[tile.id]?.get(option) ?: return null
        if (teleport.id != id) {
            return null
        }
        return teleport
    }

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = getProperty("teleportsPath")): Teleports {
        timedLoad("teleport") {
            val config = object : YamlReaderConfiguration() {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    val map = value as Map<String, Any>
                    val tile = map["tile"] as Tile
                    val optionMap = createMap()
                    val option = map["option"] as String
                    optionMap[option] = Teleport(
                        id = map["id"] as Int,
                        option = option,
                        tile = tile,
                        delta = map["delta"] as? Delta ?: Delta.EMPTY,
                        to = map["to"] as? Tile ?: Tile.EMPTY,
                    )
                    super.add(list, tile.id to optionMap, parentMap)
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    super.set(map, key, when (key) {
                        "delta" -> Delta.fromMap(value as Map<String, Any>)
                        "tile", "to" -> Tile.fromMap(value as Map<String, Any>)
                        else -> value
                    }, indent, parentMap)
                }
            }
            val data: List<Pair<Int, MutableMap<String, Teleport>>> = yaml.load(path, config)
            val teleports = Int2ObjectOpenHashMap<MutableMap<String, Teleport>>()
            for ((tile, map) in data) {
                teleports[tile] = teleports.get(tile)?.apply { putAll(map) } ?: map
            }
            this.teleports = teleports
            data.size
        }
        return this
    }

    data class Teleport(
        val id: Int,
        val option: String,
        val tile: Tile,
        val delta: Delta = Delta.EMPTY,
        val to: Tile = Tile.EMPTY
    ) {
        fun apply(tile: Tile): Tile {
            return if (delta != Delta.EMPTY) {
                tile.add(delta)
            } else if (to != Tile.EMPTY) {
                to
            } else {
                tile
            }
        }
    }
}
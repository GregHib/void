package world.gregs.voidps.world.interact.world.spawn

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.koin.dsl.module
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.YamlParser
import world.gregs.yaml.config.FastUtilConfiguration

val stairsModule = module {
    single(createdAtStart = true) { Stairs(get()).load() }
}

class Stairs(
    private val parser: YamlParser
) {

    private lateinit var teleports: Map<Int, Map<String, Teleport>>

    fun get(id: Int, tile: Tile, option: String): Teleport? {
        val teleport = teleports[tile.id]?.get(option) ?: return null
        if (teleport.id != id) {
            return null
        }
        return teleport
    }

    @Suppress("UNCHECKED_CAST")
    fun load(path: String = getProperty("stairsPath")): Stairs {
        timedLoad("stair") {
            val config = object : FastUtilConfiguration() {
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
            val teleports: List<Pair<Int, Map<String, Teleport>>> = parser.load(path, config)
            this.teleports = Int2ObjectOpenHashMap(teleports.toMap())
            teleports.size
        }
        return this
    }

    data class Teleport(val id: Int, val option: String, val tile: Tile, val delta: Delta = Delta.EMPTY, val to: Tile = Tile.EMPTY) {
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
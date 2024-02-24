package world.gregs.voidps.world.interact.entity.obj

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * Object interaction teleports
 */
class Teleports {

    private lateinit var teleports: Map<Int, Map<String, TeleportDefinition>>

    suspend fun teleport(objectOption: ObjectOption, option: String = objectOption.option): Boolean {
        val id = objectOption.def.stringId.ifEmpty { objectOption.def.id.toString() }
        val definition = teleports[objectOption.target.tile.id]?.get(option) ?: return false
        if (definition.id != id) {
            return false
        }
        val player = objectOption.player
        val teleport = Teleport(player, definition.id, definition.tile, objectOption.def, definition.option)
        player.emit(teleport)
        if (teleport.cancelled) {
            return false
        }
        val tile = when {
            definition.delta != Delta.EMPTY -> player.tile.add(definition.delta)
            definition.to != Tile.EMPTY -> definition.to
            else -> player.tile
        }
        val delay = teleport.delay
        if (delay != null) {
            objectOption.delay(delay)
        }
        player.tele(tile)
        teleport.land = true
        player.emit(teleport)
        return true
    }

    fun contains(id: String, tile: Tile, option: String): Boolean {
        val teleport = teleports[tile.id]?.get(option) ?: return false
        return teleport.id == id
    }

    fun get(id: String, option: String): List<TeleportDefinition> {
        return teleports.values.mapNotNull { it[option] }.filter { it.id == id }
    }

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = getProperty("teleportsPath")): Teleports {
        timedLoad("object teleport") {
            val config = object : YamlReaderConfiguration() {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    val map = value as Map<String, Any>
                    val tile = map["tile"] as Tile
                    val optionMap = createMap()
                    val option = map["option"] as String
                    val id = (map["id"] as? Int)?.toString() ?: map["id"] as String
                    optionMap[option] = TeleportDefinition(
                        id = id,
                        option = option,
                        tile = tile,
                        delta = map["delta"] as? Delta ?: Delta.EMPTY,
                        to = map["to"] as? Tile ?: Tile.EMPTY
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
            val data: List<Pair<Int, MutableMap<String, TeleportDefinition>>> = yaml.load(path, config)
            val teleports = Int2ObjectOpenHashMap<MutableMap<String, TeleportDefinition>>()
            for ((tile, map) in data) {
                teleports[tile] = teleports.get(tile)?.apply { putAll(map) } ?: map
            }
            this.teleports = teleports
            data.size
        }
        return this
    }

    data class TeleportDefinition(
        val id: String,
        val option: String,
        val tile: Tile,
        val delta: Delta = Delta.EMPTY,
        val to: Tile = Tile.EMPTY
    )
}
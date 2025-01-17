package world.gregs.voidps.world.interact.entity.obj

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.get
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

    private lateinit var teleports: Map<String, Map<Int, TeleportDefinition>>

    suspend fun teleport(objectOption: ObjectOption<Player>, option: String = objectOption.option): Boolean {
        return teleport(objectOption, objectOption.character, objectOption.def, objectOption.target.tile, option)
    }

    suspend fun teleport(context: CharacterContext<Player>, player: Player, def: ObjectDefinition, targetTile: Tile, option: String): Boolean {
        val id = def.stringId.ifEmpty { def.id.toString() }
        val definition = teleports[option]?.get(targetTile.id) ?: return false
        if (definition.id != id) {
            return false
        }
        val teleport = Teleport(player, definition.id, definition.tile, def, definition.option)
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
            context.delay(delay)
        }
        player.tele(tile)
        teleport.land = true
        player.emit(teleport)
        return true
    }

    fun contains(id: String, tile: Tile, option: String): Boolean {
        val teleport = teleports[option]?.get(tile.id) ?: return false
        return teleport.id == id
    }

    fun get(id: String, option: String): List<TeleportDefinition> {
        return teleports[option]?.values?.filter { it.id == id } ?: emptyList()
    }

    fun get(option: String): Map<Int, TeleportDefinition> {
        return teleports[option] ?: emptyMap()
    }

    fun options(): Set<String> {
        return teleports.keys
    }

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["map.teleports"]): Teleports {
        timedLoad("object teleport") {
            val config = object : YamlReaderConfiguration() {
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    val map = value as Map<String, Any>
                    val tile = map["tile"] as Tile
                    val option = map["option"] as String
                    val id = (map["id"] as? Int)?.toString() ?: map["id"] as String
                    val definition = TeleportDefinition(
                        id = id,
                        option = option,
                        tile = tile,
                        delta = map["delta"] as? Delta ?: Delta.EMPTY,
                        to = map["to"] as? Tile ?: Tile.EMPTY
                    )
                    super.add(list, definition, parentMap)
                }

                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    super.set(map, key, when (key) {
                        "delta" -> Delta.fromMap(value as Map<String, Any>)
                        "tile", "to" -> Tile.fromMap(value as Map<String, Any>)
                        else -> value
                    }, indent, parentMap)
                }
            }
            val data: List<TeleportDefinition> = yaml.load(path, config)
            val teleports = Object2ObjectOpenHashMap<String, Int2ObjectOpenHashMap<TeleportDefinition>>()
            for (definition in data) {
                teleports.getOrPut(definition.option) { Int2ObjectOpenHashMap() }.put(definition.tile.id, definition)
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
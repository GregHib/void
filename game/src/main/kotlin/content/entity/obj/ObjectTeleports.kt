package content.entity.obj

import content.bot.interact.navigation.graph.readTile
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile

/**
 * Object interaction teleports
 */
class ObjectTeleports {

    private lateinit var teleports: Map<String, Map<Int, TeleportDefinition>>

    suspend fun teleport(objectOption: ObjectOption<Player>, option: String = objectOption.option): Boolean {
        return teleport(objectOption, objectOption.character, objectOption.def, objectOption.target.tile, option)
    }

    suspend fun teleport(context: SuspendableContext<Player>, player: Player, def: ObjectDefinition, targetTile: Tile, option: String): Boolean {
        val id = def.stringId.ifEmpty { def.id.toString() }
        val definition = teleports[option]?.get(targetTile.id) ?: return false
        if (definition.id != id) {
            return false
        }
        val teleport = ObjectTeleport(player, definition.id, definition.tile, def, definition.option)
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

    fun load(paths: List<String>): ObjectTeleports {
        val teleports = Object2ObjectOpenHashMap<String, Int2ObjectOpenHashMap<TeleportDefinition>>()
        timedLoad("object teleport") {
            var counter = 0
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        var option = ""
                        var tile = Tile.EMPTY
                        var to = Tile.EMPTY
                        var delta = Delta.EMPTY
                        while (nextPair()) {
                            when (val key = key()) {
                                "option" -> option = string()
                                "tile" -> tile = readTile()
                                "delta" -> {
                                    var x = 0
                                    var y = 0
                                    var level = 0
                                    while (nextEntry()) {
                                        when (val k = key()) {
                                            "x" -> x = int()
                                            "y" -> y = int()
                                            "level" -> level = int()
                                            else -> throw IllegalArgumentException("Unexpected key: '$k' ${exception()}")
                                        }
                                    }
                                    delta = Delta(x, y, level)
                                }
                                "to" -> to = readTile()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        val definition = TeleportDefinition(stringId, option, tile, delta, to)
                        teleports.getOrPut(option) { Int2ObjectOpenHashMap() }.put(tile.id, definition)
                        counter++
                    }
                }
            }
            this.teleports = teleports
            counter
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
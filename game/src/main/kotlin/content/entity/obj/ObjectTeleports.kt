package content.entity.obj

import content.bot.behaviour.navigation.NavigationGraph.Companion.readTile
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.snapshot.Snapshot
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile

/**
 * Object interaction teleports
 */
class ObjectTeleports {

    private lateinit var teleports: Map<String, Map<Int, TeleportDefinition>>

    suspend fun teleport(player: Player, target: GameObject, option: String, def: ObjectDefinition = target.def(player)): Boolean {
        val definition = teleports[option]?.get(target.tile.id) ?: return false
        val id = def.stringId.ifEmpty { def.id.toString() }
        if (definition.id != id) {
            return false
        }
        return teleport(player, target, definition)
    }

    suspend fun teleport(player: Player, target: GameObject, definition: TeleportDefinition): Boolean {
        val delay = Teleport.takeOff(player, target, definition.option)
        if (delay == Teleport.CANCEL) {
            return false
        }
        if (target.id.contains("gangplank")) {
            player.walkOverDelay(target.tile)
        }
        teleportContinue(player, definition, target, delay)
        return true
    }

    suspend fun teleportContinue(player: Player, definition: TeleportDefinition, target: GameObject, delay: Int = Teleport.CONTINUE) {
        val tile = teleportTile(player, definition)
        if (delay != Teleport.CONTINUE) {
            player.delay(delay)
        }
        player.tele(tile)
        Teleport.land(player, target, definition.option)
    }

    fun teleportTile(player: Player, definition: TeleportDefinition): Tile = when {
        definition.delta != Delta.EMPTY && definition.to != Tile.EMPTY ->
            Distance.nearest(definition.to, definition.delta.x, definition.delta.y, player.tile)
        definition.delta != Delta.EMPTY -> player.tile.add(definition.delta)
        definition.to != Tile.EMPTY -> definition.to
        else -> player.tile
    }

    fun contains(id: String, tile: Tile, option: String): Boolean {
        val teleport = teleports[option]?.get(tile.id) ?: return false
        return teleport.id == id
    }

    fun get(id: String, option: String): List<TeleportDefinition> = teleports[option]?.values?.filter { it.id == id } ?: emptyList()

    fun get(option: String): Map<Int, TeleportDefinition> = teleports[option] ?: emptyMap()

    fun options(): Set<String> = teleports.keys

    fun load(paths: List<String>, snapshot: Snapshot? = null): ObjectTeleports {
        val teleports = Object2ObjectOpenHashMap<String, Int2ObjectOpenHashMap<TeleportDefinition>>()
        timedLoad("object teleport") {
            if (snapshot != null && restore(snapshot)) {
                return@timedLoad teleports.values.sumOf { it.size }
            }
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
                                "near" -> {
                                    var x = 0
                                    var y = 0
                                    var level = 0
                                    var width = 1
                                    var height = 1
                                    while (nextEntry()) {
                                        when (val k = key()) {
                                            "x" -> x = int()
                                            "y" -> y = int()
                                            "width" -> width = int()
                                            "height" -> height = int()
                                            "level" -> level = int()
                                            else -> throw IllegalArgumentException("Unexpected key: '$k' ${exception()}")
                                        }
                                    }
                                    to = Tile(x, y, level)
                                    delta = Delta(width, height)
                                }
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
            snapshot?.let { save(it) }
            counter
        }
        return this
    }

    private fun save(snapshot: Snapshot) = snapshot.write {
        int(teleports.size)
        for ((option, definitions) in teleports) {
            string(option)
            int(definitions.size)
            for (definition in definitions.values) {
                string(definition.id)
                string(definition.option)
                int(definition.tile.id)
                long(definition.delta.id)
                int(definition.to.id)
            }
        }
    }

    private fun restore(snapshot: Snapshot): Boolean = snapshot.restore { reader ->
        val size = reader.int()
        val teleports = Object2ObjectOpenHashMap<String, Int2ObjectOpenHashMap<TeleportDefinition>>(size, Hash.VERY_FAST_LOAD_FACTOR)
        repeat(size) {
            val option = reader.string()
            val count = reader.int()
            val definitions = Int2ObjectOpenHashMap<TeleportDefinition>(count, Hash.VERY_FAST_LOAD_FACTOR)
            repeat(count) {
                val definition = TeleportDefinition(reader.string(), reader.string(), Tile(reader.int()), Delta(reader.long()), Tile(reader.int()))
                definitions[definition.tile.id] = definition
            }
            teleports[option] = definitions
        }
        this.teleports = teleports
    }

    data class TeleportDefinition(
        val id: String,
        val option: String,
        val tile: Tile,
        val delta: Delta = Delta.EMPTY,
        val to: Tile = Tile.EMPTY,
    )
}

package world.gregs.voidps.tools

import com.displee.cache.CacheLibrary
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.pearx.kasechange.toSnakeCase
import world.gregs.config.Config
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.voidps.engine.data.find
import world.gregs.voidps.engine.data.list
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import java.io.File

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val files = configFiles()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val categories = CategoryDefinitions().load(files.find(Settings["definitions.categories"]))
        val ammo = AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"]))
        val parameters = ParameterDefinitions(categories, ammo).load(files.find(Settings["definitions.parameters"]))
        val definitions = NPCDecoder(true, parameters).load(cache)
        val decoder = NPCDefinitions(definitions).load(files.getValue(Settings["definitions.npcs"]))
        val renderAnimations = RenderAnimationDecoder().load(cache)
        val file = File("C:\\Users\\Greg\\Downloads\\rs3-spawns\\map_npcs.json")

        val objectMapper = ObjectMapper()
        val map: List<Map<String, Int>> = objectMapper.readValue(file)

        val baseMaps = mutableListOf<Pair<Rectangle, String>>()

        val base: List<Map<String, Any>> = objectMapper.readValue(File("C:\\Users\\Greg\\Downloads\\rs3-spawns\\basemaps.json"))
        for (map in base) {
            val bounds = map["bounds"] as List<List<Int>>
            val shape = if (bounds.size == 2) {
                Rectangle(bounds[0][0], bounds[0][1], bounds[1][0], bounds[1][1])
            } else {
                continue
            }
            if (map["name"] == "default" || map["name"] == "asgarnia_ice_cave" || map["name"] == "RuneScape Surface") {
                continue
            }
            baseMaps.add(shape to (map["name"] as String).toSnakeCase().replace("'", ""))
        }

        val existing = spawns(configFiles().list(Settings["spawns.npcs"]))
        val current = existing.groupBy { it.tile }
        println(existing.size)

        val unique = mutableMapOf<Int, String>()
        val names = mutableSetOf<String>()
        val grouped = mutableMapOf<String, MutableList<String>>()
        val npcs = File("C:\\Users\\Greg\\Downloads\\rs3-spawns\\npcids\\").listFiles()!!
        val rs3Defs: List<Map<String, Any>> = objectMapper.readValue(File("C:\\Users\\Greg\\Documents\\Void\\data\\rs3-bestiary.json"))
        val rs3Definitions = rs3Defs.associateBy { it["id"] as Int }
        val npcDefs = mutableMapOf<String, MutableList<String>>()
        val valid = mutableSetOf<Int>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val tiles = cache.data(Index.MAPS, "m${regionX}_${regionY}")
                val objects = cache.data(Index.MAPS, "l${regionX}_${regionY}")
                if (tiles != null || objects != null) {
                    valid.add(Region.id(regionX, regionY))
                }
            }
        }
        for (file in npcs) {
            val list: List<Map<String, Any>> = objectMapper.readValue(file)

            for ((tile, spawn) in list.groupBy { spawn ->
                val x = spawn["x"] as Int
                val y = spawn["y"] as Int
                val p = spawn["p"] as Int
                Tile(x, y, p)
            }) {
                val filtered = spawn.mapNotNull { spawn -> decoder.getOrNull(spawn["id"] as Int) }
//                val name = spawn["name"] as String
//                val id = spawn["id"] as Int
//                val x = spawn["x"] as Int
//                val y = spawn["y"] as Int
//                val p = spawn["p"] as Int
                if (filtered.isEmpty()) {
                    continue
                }
                if (!valid.contains(tile.region.id)) {
                    continue
                }
//                if (def.name != name) {
//                    val transforms = def.transforms?.map { decoder.getOrNull(it)?.name?.lowercase() }?.filterNotNull() ?: emptyList()
//                    if (!transforms.contains(name.lowercase())) {
////                        println("Different ${def.id} ${def.name} $transforms $name")
//                        continue
//                    }
//                }
                val expected = spawn.map { it["id"] as Int }
                if (current[tile] != null) {
                    val def = current[tile]!!.map { decoder.get(it.id) }
                    val difference = expected.subtract(def.map { it.id })
                    if (difference.isNotEmpty()) {
//                        println("Difference: x = ${tile.x}, y = ${tile.y}${if (tile.level == 0) "" else ", level = ${tile.level}"} ${difference.map { decoder.get(it).stringId }} ${expected.map { val def = decoder.get(it);def.name }} ${def.map { it.name }}")
                    }
                } else {
                    println("Missing: tele ${tile.x} ${tile.y}${if (tile.level == 0) "" else " ${tile.level}"} ${expected.map { val def = decoder.get(it);"$it ${def.stringId} ${def.name} ${def.transforms?.filter { it != -1 }?.map { it to decoder.get(it).stringId }}" }}")
                }
            }
        }

    }

    data class NPCSpawn(val id: String, val tile: Tile, val direction: Direction, val members: Boolean)

    fun spawns(paths: List<String>): List<NPCSpawn> {
        val list = mutableListOf<NPCSpawn>()
        val membersWorld = true
        for (path in paths) {
            Config.fileReader(path) {
                while (nextPair()) {
                    require(key() == "spawns")
                    while (nextElement()) {
                        var id = ""
                        var direction = Direction.NONE
                        var x = 0
                        var y = 0
                        var level = 0
                        var members = false
                        while (nextEntry()) {
                            when (val key = key()) {
                                "id" -> id = string()
                                "x" -> x = int()
                                "y" -> y = int()
                                "level" -> level = int()
                                "direction" -> direction = Direction.valueOf(string())
                                "members" -> members = boolean()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        if (!membersWorld && members) {
                            continue
                        }
                        val tile = Tile(x, y, level)
                        list.add(NPCSpawn(id, tile, direction, members))
                    }
                }
            }
        }
        return list
    }
}

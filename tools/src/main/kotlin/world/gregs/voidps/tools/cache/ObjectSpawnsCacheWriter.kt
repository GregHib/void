package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.config.Config
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.cache.definition.encoder.MapObjectEncoder
import world.gregs.voidps.tools.map.MapDecoder
import world.gregs.voidps.type.Region
import java.io.File

/**
 * Reads obj-spawns.toml and writes object spawns to the cache (Index.MAPS)
 * This is a standalone tool for development - not included in runtime to avoid ~100MB+ RAM overhead
 */
object ObjectSpawnsCacheWriter {

    /**
     * Writes object spawns from obj-spawns.toml to the cache
     * @param cachePath Path to the cache directory
     * @param spawnsPath Path to the obj-spawns.toml file
     * @param xteas XTEA keys for map encryption
     */
    fun writeToCache(cachePath: String, spawnsPath: String, xteas: Xteas = Xteas()) {
        val cache = CacheLibrary(cachePath)
        val delegate = CacheDelegate(cache)
        val mapDecoder = MapDecoder(xteas)
        val mapEncoder = MapObjectEncoder()
        val objectDecoder = ObjectDecoder(member = true, lowDetail = false)

        val definitionsArray = mapDecoder.load(delegate)
        val maxObjectId = objectDecoder.size(delegate)

        // Load object definitions for name-to-id lookup
        val objectDefinitions = objectDecoder.load(delegate)

        val definitions = definitionsArray.toMutableList()

        val spawnsFile = File(spawnsPath)
        if (!spawnsFile.exists()) {
            println("Spawns file not found: $spawnsPath")
            return
        }

        Config.fileReader(spawnsPath) {
            while (nextPair()) {
                require(key() == "spawns")
                while (nextElement()) {
                    var id = ""
                    var rotation = 0
                    var x = 0
                    var y = 0
                    var level = 0
                    var type = 10
                    var members = false
                    while (nextEntry()) {
                        when (val key = key()) {
                            "id" -> id = string()
                            "x" -> x = int()
                            "y" -> y = int()
                            "level" -> level = int()
                            "rotation" -> rotation = int()
                            "type" -> type = int()
                            "members" -> members = boolean()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }

                    val regionX = x ushr 6
                    val regionY = y ushr 6
                    val localX = x and 0x3f
                    val localY = y and 0x3f
                    val regionId = Region.id(regionX, regionY)

                    var regionDef = definitions.find { it.id == regionId }
                    if (regionDef == null) {
                        regionDef = MapDefinition(id = regionId)
                        definitions.add(regionDef)
                    }

                    val definition = objectDefinitions.find { it.name == id }
                    if (definition == null) {
                        println("Object definition not found for name: $id")
                        continue
                    }
                    if (definition.id >= maxObjectId) {
                        println("Invalid object id ${definition.id} (max: $maxObjectId)")
                        continue
                    }

                    regionDef.objects.add(
                        MapObject(
                            id = definition.id,
                            x = localX,
                            y = localY,
                            level = level,
                            shape = type,
                            rotation = rotation
                        )
                    )
                }
            }
        }

        val writer = ArrayWriter(45_000)
        var rewritten = 0
        for (definition in definitions) {
            if (definition.id == -1) continue
            writer.clear()
            with(mapEncoder) {
                writer.encode(definition)
            }
            val data = writer.toArray()
            val regionX = definition.id shr 8
            val regionY = definition.id and 0xff
            cache.put(Index.MAPS, "l${regionX}_$regionY", data)
            rewritten++
        }

        cache.update()
        println("Wrote $rewritten region maps to cache from $spawnsPath")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size < 2) {
            println("Usage: ObjectSpawnsCacheWriter <cachePath> <spawnsPath> [xteasPath]")
            println("Example: ObjectSpawnsCacheWriter ./data/cache/ ./data/obj-spawns.toml ./tools/src/main/resources/xteas.dat")
            return
        }

        val cachePath = args[0]
        val spawnsPath = args[1]
        val xteas = if (args.size >= 3) {
            Xteas().load(args[2], Xteas.DEFAULT_KEY, Xteas.DEFAULT_VALUE)
        } else {
            Xteas()
        }

        writeToCache(cachePath, spawnsPath, xteas)
    }
}
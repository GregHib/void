package world.gregs.voidps.tools.map

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.cache.definition.encoder.MapObjectEncoder
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.ObjectSpawn
import world.gregs.voidps.engine.entity.obj.readObjectSpawns
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import java.io.File

/**
 * Applies object spawn edits from a TOML file to map archives in a writable cache.
 *
 * Usage: ./gradlew :tools:updateObjectSpawns -Pargs="path/to/obj-spawns.toml [cache-path]"
 */
object UpdateObjectSpawns {
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.isNotEmpty()) { "Usage: UpdateObjectSpawns <obj-spawns.toml> [cache-path]" }
        Settings.load()
        val spawnPath = File(args[0])
        require(spawnPath.isFile) { "Object spawn file does not exist: ${spawnPath.path}" }
        val cache = CacheDelegate(args.getOrNull(1) ?: Settings["storage.cache.path"])
        try {
            val files = configFiles()
            ObjectDefinitions.init(ObjectDecoder(member = true, lowDetail = false).load(cache))
                .load(files.list(Settings["definitions.objects"]))
            val spawns = readObjectSpawns(listOf(spawnPath.path))
            val regions = apply(cache, spawns)
            cache.update()
            invalidateServerCaches()
            println("Applied ${spawns.size} object spawn change(s) to $regions region(s).")
        } finally {
            cache.close()
        }
    }

    private fun apply(cache: CacheDelegate, spawns: List<ObjectSpawn>): Int {
        val byRegion = spawns.groupBy { Region(Tile(it.x, it.y, it.level).region.id) }
        val encoder = MapObjectEncoder()
        val writer = ArrayWriter(45_000)
        var changedRegions = 0
        for ((region, entries) in byRegion) {
            val archive = "l${region.x}_${region.y}"
            if (cache.data(world.gregs.voidps.cache.Index.MAPS, archive) == null) {
                println("Skipping missing map archive $archive")
                continue
            }
            val definition = MapDefinition(region.id)
            MapObjectDefinitionDecoder().decode(cache, definition, modified = false)
            for (spawn in entries) {
                val objectId = ObjectDefinitions.getOrNull(spawn.id)?.id
                if (objectId == null) {
                    println("Skipping unknown object id '${spawn.id}'")
                    continue
                }
                val localX = spawn.x and 0x3f
                val localY = spawn.y and 0x3f
                definition.objects.removeAll {
                    it.x == localX && it.y == localY && it.level == spawn.level && it.shape == spawn.type
                }
                if (!spawn.remove) {
                    definition.objects.add(MapObject(objectId, localX, localY, spawn.level, spawn.type, spawn.rotation and 3))
                }
            }
            writer.clear()
            with(encoder) {
                writer.encode(definition)
            }
            cache.write(world.gregs.voidps.cache.Index.MAPS, archive, writer.toArray())
            changedRegions++
        }
        return changedRegions
    }

    private fun invalidateServerCaches() {
        if (!Settings["storage.caching.active", false]) {
            return
        }
        val path = Settings["storage.caching.path"]
        File(path, Settings["storage.caching.objects"]).delete()
        File(path, Settings["storage.caching.collisions"]).delete()
    }
}

package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.decoder.MapTileDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.cache.memory.load.FileCacheLoader
import world.gregs.voidps.cache.memory.load.MemoryCacheLoader
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.obj.ObjectsReader
import world.gregs.voidps.engine.map.obj.ObjectsRotatedReader
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone
import world.gregs.yaml.Yaml

/**
 * Loads map collision and objects directly, quicker than [MapDecoder]
 *
 *  Note: this is the only place we store the cache; for dynamic zone loading.
 */
class MapDefinitionsNew(
    private val collisions: CollisionReader,
    definitions: ObjectDefinitions,
    private val objects: GameObjects,
    private val cache: Cache
) {
    private val logger = InlineLogger()

    private val reader = ObjectsReader(objects, definitions)
    private val rotationReader = ObjectsRotatedReader(objects, definitions)

    fun loadCache(xteas: Map<Int, IntArray>? = null): MapDefinitionsNew {
        val start = System.currentTimeMillis()
        var regions = 0
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val tiles = loadTiles(cache, regionX, regionY) ?: continue
                collisions.read(tiles, regionX shl 6, regionY shl 6)
                val keys = if (xteas != null) xteas[Region.id(regionX, regionY)] else null
                reader.loadObjects(cache, tiles, regionX, regionY, keys)
                regions++
            }
        }
        logger.info { "Loaded $regions maps ${objects.size} ${"object".plural(objects.size)} in ${System.currentTimeMillis() - start}ms" }
        return this
    }

    /*
        TODO
            Load tiles array and do loop over zone coords with applied rotation
            Load all objects and skip over those with x/y +/- size in the zone and apply rotation
     */
    fun loadZone(from: Zone, to: Zone, rotation: Int, xteas: Map<Int, IntArray>? = null) {
        val start = System.currentTimeMillis()
        val regionX = from.region.x
        val regionY = from.region.x
        val tiles = loadTiles(cache, regionX, regionY) ?: return
        collisions.read(tiles, regionX shl 6, regionY shl 6)
        val keys = if (xteas != null) xteas[Region.id(regionX, regionY)] else null
        rotationReader.loadObjects(cache, tiles, regionX, regionY, to.region.x, to.region.y, rotation, keys)
        val took = System.currentTimeMillis() - start
        if (took > 5) {
            logger.info { "Loaded zone $from -> $to $rotation in ${took}ms" }
        }
    }

    private fun loadTiles(cache: Cache, regionX: Int, regionY: Int): LongArray? {
        val archive = cache.getArchiveId(Index.MAPS, "m${regionX}_$regionY")
        if (archive == -1) {
            return null
        }
        val data = cache.getFile(Index.MAPS, archive) ?: return null
        val buffer = BufferReader(data)
        val tiles = LongArray(16384) // TODO faster to remake or fill or parallel?
        MapTileDecoder.loadTiles(buffer, tiles)
        return tiles
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val path = "./data/cache/"

            var start = System.currentTimeMillis()
            val cache1 = CacheDelegate(path)
            println("Cache1 loaded in ${System.currentTimeMillis() - start}ms")
            start = System.currentTimeMillis()
            val cache2 = MemoryCacheLoader().load(path)
            println("Cache2 loaded in ${System.currentTimeMillis() - start}ms")
            start = System.currentTimeMillis()
            val cache3 = FileCacheLoader().load(path)
            println("Cache3 loaded in ${System.currentTimeMillis() - start}ms")
            for (cache in listOf(cache2, cache3, cache1)) {
                val collisions = Collisions()
                val objectDefinitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).loadCache(cache1))
                    .load(Yaml(), "./data/definitions/objects.yml")
                val objects = GameObjects(GameObjectCollision(collisions), ZoneBatchUpdates(), objectDefinitions, storeUnused = true)
                val mapDefinitions = MapDefinitionsNew(CollisionReader(collisions), objectDefinitions, objects, cache)
                mapDefinitions.loadCache()
            }
        }
    }
}
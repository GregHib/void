package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.*
import world.gregs.voidps.cache.definition.decoder.MapTileDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.obj.MapObjectsDecoder
import world.gregs.voidps.engine.map.obj.MapObjectsRotatedDecoder
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone
import world.gregs.yaml.Yaml

/**
 * Loads map collision and objects directly, quicker than [MapDecoder]
 *
 *  Note: this is the only place we store the cache; for dynamic zone loading.
 */
class MapDefinitions(
    private val collisions: CollisionDecoder,
    definitions: ObjectDefinitions,
    private val objects: GameObjects,
    private val cache: Cache
) {
    private val logger = InlineLogger()

    private val decoder = MapObjectsDecoder(objects, definitions)
    private val rotationDecoder = MapObjectsRotatedDecoder(objects, definitions)

    fun loadCache(xteas: Map<Int, IntArray>? = null): MapDefinitions {
        val start = System.currentTimeMillis()
        var regions = 0
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val tiles = loadTiles(cache, regionX, regionY) ?: continue
                collisions.decode(tiles, regionX shl 6, regionY shl 6)
                val keys = if (xteas != null) xteas[Region.id(regionX, regionY)] else null
                decoder.decode(cache, tiles, regionX, regionY, keys)
                regions++
            }
        }
        logger.info { "Loaded $regions maps ${objects.size} ${"object".plural(objects.size)} in ${System.currentTimeMillis() - start}ms" }
        return this
    }

    fun loadZone(from: Zone, to: Zone, rotation: Int, xteas: Map<Int, IntArray>? = null) {
        val start = System.currentTimeMillis()
        val tiles = loadTiles(cache, from.region.x, from.region.y) ?: return
        collisions.decode(tiles, from, to, rotation)
        val keys = if (xteas != null) xteas[from.region.id] else null
        rotationDecoder.decode(cache, tiles, from, to, rotation, keys)
        val took = System.currentTimeMillis() - start
        if (took > 5) {
            logger.info { "Loaded zone $from -> $to $rotation in ${took}ms" }
        }
    }

    private fun loadTiles(cache: Cache, regionX: Int, regionY: Int): LongArray? {
        val archive = cache.archiveId(Index.MAPS, "m${regionX}_$regionY")
        if (archive == -1) {
            return null
        }
        val data = cache.data(Index.MAPS, archive) ?: return null
        val buffer = BufferReader(data)
        val tiles = LongArray(16384)
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
            val cache2 = MemoryCache(path)
            println("Cache2 loaded in ${System.currentTimeMillis() - start}ms")
            start = System.currentTimeMillis()
            val cache3 = FileCache(path)
            println("Cache3 loaded in ${System.currentTimeMillis() - start}ms")
            for (cache in listOf(cache3, cache1, cache2)) {
                val collisions = Collisions()
                val objectDefinitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).load(cache1))
                    .load(Yaml(), "./data/definitions/objects.yml")
                val objects = GameObjects(GameObjectCollision(collisions), ZoneBatchUpdates(), objectDefinitions, storeUnused = true)
                val mapDefinitions = MapDefinitions(CollisionDecoder(collisions), objectDefinitions, objects, cache)
                mapDefinitions.loadCache()
            }
        }
    }
}
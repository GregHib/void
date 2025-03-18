package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.FileCache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.decoder.MapTileDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.engine.map.obj.MapObjectsDecoder
import world.gregs.voidps.engine.map.obj.MapObjectsRotatedDecoder
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone
import java.io.File
import kotlin.system.exitProcess
import kotlin.time.measureTimedValue

/**
 * Loads map collision and objects fast and direct
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
        try {
            val start = System.currentTimeMillis()
            var regions = 0
            val settings = ByteArray(16384)
            for (regionX in 0 until 256) {
                for (regionY in 0 until 256) {
                    if (!loadSettings(cache, regionX, regionY, settings)) {
                        continue
                    }
                    collisions.decode(settings, regionX shl 6, regionY shl 6)
                    val keys = if (xteas != null) xteas[Region.id(regionX, regionY)] else null
                    decoder.decode(cache, settings, regionX, regionY, keys)
                    regions++
                }
            }
            logger.info { "Loaded $regions maps ${objects.size} ${"object".plural(objects.size)} in ${System.currentTimeMillis() - start}ms" }
            return this
        } catch (e: ArrayIndexOutOfBoundsException) {
            logger.error(e) { "Error loading map definition; do you have the latest cache?" }
            exitProcess(1)
        }
    }

    fun loadZone(from: Zone, to: Zone, rotation: Int, xteas: Map<Int, IntArray>? = null) {
        val start = System.currentTimeMillis()
        val settings = loadSettings(cache, from.region.x, from.region.y) ?: return
        collisions.decode(settings, from, to, rotation)
        val keys = if (xteas != null) xteas[from.region.id] else null
        rotationDecoder.decode(cache, settings, from, to, rotation, keys)
        val took = System.currentTimeMillis() - start
        if (took > 5) {
            logger.info { "Loaded zone $from -> $to $rotation in ${took}ms" }
        }
    }

    private fun loadSettings(cache: Cache, regionX: Int, regionY: Int, settings: ByteArray): Boolean {
        val archive = cache.archiveId(Index.MAPS, "m${regionX}_$regionY")
        if (archive == -1) {
            return false
        }
        val data = cache.data(Index.MAPS, archive) ?: return false
        MapTileDecoder.loadTiles(data, settings)
        return true
    }

    private fun loadSettings(cache: Cache, regionX: Int, regionY: Int): ByteArray? {
        val archive = cache.archiveId(Index.MAPS, "m${regionX}_$regionY")
        if (archive == -1) {
            return null
        }
        val settings = ByteArray(16384)
        val data = cache.data(Index.MAPS, archive) ?: return null
        MapTileDecoder.loadTiles(data, settings)
        return settings
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val properties = Settings.load(File("./game/src/main/resources/game.properties").inputStream())
//            properties["storage.cache.path"] = "./data/cache-old/"
            val (cache, duration) = measureTimedValue { FileCache.load(properties) }
            println("Loaded cache in ${duration.inWholeMilliseconds}ms")
            val definitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).load(cache)).load(properties.getProperty("definitions.objects"))
            val collisions = Collisions()
            val add = GameObjectCollisionAdd(collisions)
            val remove = GameObjectCollisionRemove(collisions)
            val defs = MapDefinitions(CollisionDecoder(collisions), definitions, GameObjects(add, remove, ZoneBatchUpdates(), definitions, storeUnused = true), cache).loadCache()
        }
    }
}
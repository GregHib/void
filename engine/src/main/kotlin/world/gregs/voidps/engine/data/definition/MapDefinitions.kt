package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.decoder.MapTileDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.obj.MapObjectsDecoder
import world.gregs.voidps.engine.map.obj.MapObjectsRotatedDecoder
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone
import java.io.File
import kotlin.system.exitProcess

/**
 * Loads map collision and objects fast and direct
 *
 *  Note: this is the only place we store the cache; for dynamic zone loading.
 */
class MapDefinitions(
    private val collisions: CollisionDecoder,
    private val cache: Cache,
) {
    private val logger = InlineLogger()

    private val decoder = MapObjectsDecoder()
    private val rotationDecoder = MapObjectsRotatedDecoder()

    fun load(configFiles: ConfigFiles, xteas: Map<Int, IntArray>? = null): MapDefinitions {
        try {
            if (!Settings["storage.caching.active", false]) {
                loadCache(xteas)
                return this
            }
            val path = Settings["storage.caching.path"]
            File(path).mkdirs()
            val objectsFile = File("${path}${Settings["storage.caching.objects"]}")
            val collisionsFile = File("${path}${Settings["storage.caching.collisions"]}")
            if (objectsFile.exists() && collisionsFile.exists() && !configFiles.cacheUpdate) {
                val start = System.currentTimeMillis()
                val zones = collisions.load(collisionsFile)
                if (zones >= 0) {
                    GameObjects.load(objectsFile)
                    logger.info { "Loaded all maps $zones zones ${GameObjects.size} ${"object".plural(GameObjects.size)} in ${System.currentTimeMillis() - start}ms" }
                    return this
                }
                Collisions.clear()
            }
            loadCache(xteas)
            val start = System.currentTimeMillis()
            collisions.save(collisionsFile)
            GameObjects.save(objectsFile)
            logger.info { "Cached maps in ${System.currentTimeMillis() - start}ms" }
            return this
        } catch (e: ArrayIndexOutOfBoundsException) {
            logger.error(e) { "Error loading map definition; do you have the latest cache?" }
            exitProcess(1)
        }
    }

    private fun loadCache(xteas: Map<Int, IntArray>? = null) {
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
        logger.info { "Loaded $regions maps ${GameObjects.size} ${"object".plural(GameObjects.size)} in ${System.currentTimeMillis() - start}ms" }
    }

    // Zones are loaded one at a time but usually a whole region at once, so keep the last region decoded
    private val regionSettings = ByteArray(16384)
    private var regionId = -1
    private var regionKeys: Map<Int, IntArray>? = null
    private var regionObjects: ByteArray? = null

    fun loadZone(from: Zone, to: Zone, rotation: Int, xteas: Map<Int, IntArray>? = null) {
        val start = System.currentTimeMillis()
        val region = from.region
        if (regionId != region.id || regionKeys !== xteas) {
            regionId = -1
            if (!loadSettings(cache, region.x, region.y, regionSettings)) {
                return
            }
            val keys = if (xteas != null) xteas[region.id] else null
            regionObjects = cache.data(Index.MAPS, "l${region.x}_${region.y}", xtea = keys)
            regionKeys = xteas
            regionId = region.id
        }
        collisions.decode(regionSettings, from, to, rotation)
        val objects = regionObjects
        if (objects != null) {
            rotationDecoder.decode(objects, regionSettings, from, to, rotation)
        }
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

    private fun loadSettings(cache: Cache, regionX: Int, regionY: Int): Boolean {
        val archive = cache.archiveId(Index.MAPS, "m${regionX}_$regionY")
        if (archive == -1) {
            return false
        }
        val settings = ByteArray(16384)
        val data = cache.data(Index.MAPS, archive) ?: return false
        MapTileDecoder.loadTiles(data, settings)
        return true
    }
}

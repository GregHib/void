package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.cache.definition.decoder.MapTileDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.engine.map.obj.MapObjectsDecoder
import world.gregs.voidps.engine.map.obj.MapObjectsRotatedDecoder
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone

/**
 * The `MapDefinitions` class is responsible for managing game map data,
 * including collision decoding, object decoding, and handling map caches.
 * It supports loading map regions and zones with or without encryption keys (XTEAs),
 * and integrates with multiple underlying decoders and game object handling systems.
 *
 * @property collisions The collision decoder for handling blocked tiles and collision flags.
 * @property objects The collection of game objects present in the map.
 * @property cache The data cache used to store and retrieve map-related data.
 */
class MapDefinitions(
    private val collisions: CollisionDecoder,
    definitions: ObjectDefinitions,
    private val objects: GameObjects,
    private val cache: Cache
) {
    /**
     * Logger instance for use within the MapDefinitions class to provide logging functionality
     * for actions and events related to map data processing and handling.
     */
    private val logger = InlineLogger()

    /**
     * The `decoder` is an instance of `MapObjectsDecoder`. It is used to decode and manage
     * map objects while associating them with their relevant definitions. The decoder handles:
     * - Decoding map objects data from the cache.
     * - Adding objects to the game world with their defined properties like shape, rotation, and level.
     * - Managing collision data for tiles except for specific types like bridges.
     *
     * It encapsulates logic for parsing and setting objects into the game map, leveraging object
     * data and definitions provided by `GameObjects` and `ObjectDefinitions`.
     */
    private val decoder = MapObjectsDecoder(objects, definitions)
    /**
     * Decoder used to transform and decode map objects from a specified zone into another zone with rotation applied.
     * This instance utilizes the `MapObjectsRotatedDecoder` class to handle decoding logic and object transformations.
     * It works with game object data (`objects`) and their definitions (`definitions`) for processing.
     */
    private val rotationDecoder = MapObjectsRotatedDecoder(objects, definitions)

    /**
     * Loads the map data and tiles cache for the current region. Decodes map tiles,
     * collision data, and other related information, using optional XTEA keys.
     *
     * @param xteas Optional map of region IDs to XTEA key arrays for decryption.
     *              If null, regions are loaded without decryption.
     * @return The current instance of [MapDefinitions].
     */
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

    /**
     * Loads a zone from one region to another, decodes collision data, and processes rotation and optional XTEA keys.
     *
     * @param from The source zone to be loaded.
     * @param to The destination zone where the data will be applied.
     * @param rotation The rotation value to apply during decoding.
     * @param xteas Optional mapping of XTEA keys used for decoding.
     */
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

    /**
     * Loads the tiles for a specified map region using the provided cache.
     *
     * @param cache The cache to look up the map data from.
     * @param regionX The x-coordinate of the map region to load.
     * @param regionY The y-coordinate of the map region to load.
     * @return A LongArray containing the tile data for the specified region, or null if the archive is not found
     *         or the data cannot be loaded.
     */
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
}
package rs.dusk.engine.map

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import rs.dusk.cache.Cache
import rs.dusk.engine.map.collision.CollisionLoader
import rs.dusk.engine.map.location.LocationLoader
import rs.dusk.engine.map.location.Xteas
import rs.dusk.engine.model.Region
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class MapLoader {

    val collisions: CollisionLoader by inject()
    val locations: LocationLoader by inject()
    val tiles: TileLoader by inject()
    val xteas: Xteas by inject()
    val cache: Cache by inject()
    private val logger = InlineLogger()

    val loading = mutableMapOf<Region, Deferred<Boolean>>()

    fun load(region: Region): Boolean = runBlocking {
        loading.getOrPut(region) { loadAsync(region) }.await()
    }

    fun loadAsync(region: Region): Deferred<Boolean> = GlobalScope.async {
        val regionX = region.x
        val regionY = region.y
        val start = System.currentTimeMillis()
        val mapData = cache.getFile(5, "m${regionX}_${regionY}") ?: return@async false
        val xtea = xteas[region]
        val locationData = cache.getFile(5, "l${regionX}_${regionY}", xtea)

        if (locationData == null) {
            logger.info { "Missing xteas for region ${region.id} [${xtea?.toList()}]." }
            return@async false
        }
        val settings = tiles.load(mapData)
        val col = async { collisions.load(region, settings) }
        val loc = async { locations.load(locationData, settings) }
        col.await()
        loc.await()
        println("Region ${region.id} loaded in ${System.currentTimeMillis() - start}ms")
        true
    }
}
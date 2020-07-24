package rs.dusk.engine.model.world.map

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import org.koin.dsl.module
import rs.dusk.cache.Cache
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.map.collision.CollisionReader
import rs.dusk.engine.model.world.map.obj.GameObjectMapReader
import rs.dusk.engine.model.world.map.obj.Xteas
import rs.dusk.utility.inject
import kotlin.system.measureTimeMillis

val mapModule = module {
    single { MapReader() }
}

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class MapReader {

    val bus: EventBus by inject()
    val collisions: CollisionReader by inject()
    val objects: GameObjectMapReader by inject()
    val tiles: TileReader by inject()
    val xteas: Xteas by inject()
    val cache: Cache by inject()
    private val logger = InlineLogger()
    private val scope = CoroutineScope(newSingleThreadContext("MapReader"))

    val loading = mutableMapOf<Region, Deferred<Boolean>>()

    fun load(region: Region): Boolean = runBlocking {
        loading.getOrPut(region) { loadAsync(region) }.await()
    }

    fun loadAsync(region: Region): Deferred<Boolean> = scope.async {
        val regionX = region.x
        val regionY = region.y
        val time = measureTimeMillis {
            val mapData = cache.getFile(5, "m${regionX}_${regionY}") ?: return@async false
            val xtea = xteas[region]
            val locationData = cache.getFile(5, "l${regionX}_${regionY}", xtea)

            if (locationData == null) {
                logger.info { "Missing xteas for region ${region.id} [${xtea?.toList()}]." }
                return@async false
            }
            val settings = tiles.read(mapData)
            val col = async { collisions.read(region, settings) }
            val loc = async { objects.read(region.tile, locationData, settings) }
            col.await()
            loc.await()
            bus.emit(MapLoaded(region))
        }
        logger.info { "Region ${region.id} loaded in ${time}ms" }
        true
    }
}
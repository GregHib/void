package rs.dusk.engine.map.region

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import org.koin.dsl.module
import rs.dusk.cache.Cache
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.map.collision.CollisionReader
import rs.dusk.engine.map.region.obj.GameObjectMapReader
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.tile.TileReader
import kotlin.system.measureTimeMillis

val regionModule = module {
    single { RegionReader(get(), get(), get(), get(), get(), get()) }
}

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
class RegionReader(
    val bus: EventBus,
    val collisions: CollisionReader,
    val objects: GameObjectMapReader,
    val tiles: TileReader,
    val xteas: Xteas,
    val cache: Cache
) {

    private val logger = InlineLogger()
    private val scope = CoroutineScope(newSingleThreadContext("RegionReader"))

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
            bus.emit(RegionLoaded(region))
        }
        logger.info { "Region ${region.id} loaded in ${time}ms" }
        true
    }
}
package world.gregs.voidps.engine.map.region

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import org.koin.dsl.module
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import kotlin.system.measureTimeMillis

val regionModule = module {
    single { RegionReader(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { MapDecoder(get(), get<Xteas>()) }
}

class RegionReader(
    private val collisions: CollisionReader,
    private val objects: Objects,
    private val collision: GameObjectCollision,
    private val customs: CustomObjects,
    private val objectFactory: GameObjectFactory,
    private val decoder: MapDecoder,
    private val areas: Areas,
    private val definitions: ObjectDefinitions
) {

    private val logger = InlineLogger()
    private val scope = CoroutineScope(newSingleThreadContext("RegionReader"))

    val loading = mutableMapOf<Region, Deferred<Boolean>>()

    fun load(region: Region): Boolean = runBlocking {
        loading.getOrPut(region) { loadAsync(region) }.await()
    }

    fun loadAsync(region: Region): Deferred<Boolean> = scope.async {
        val time = measureTimeMillis {
            val def = decoder.getOrNull(region.id) ?: return@async false
            val col = async { collisions.read(region, def) }
            val loc = async { loadObjects(region.tile, def) }
            col.await()
            loc.await()
            areas.load(region)
            World.events.emit(RegionLoaded(region))
            customs.load(region)
        }
        logger.info { "Region ${region.id} loaded in ${time}ms" }
        true
    }

    private fun loadObjects(region: Tile, map: MapDefinition) {
        map.objects.forEach { location ->
            // Valid object
            val gameObject = objectFactory.spawn(
                definitions.getId(location.id),
                Tile(region.x + location.x, region.y + location.y, location.plane),
                location.type,
                location.rotation
            )
            objects.add(gameObject)
            collision.modifyCollision(gameObject, GameObjectCollision.ADD_MASK)
            gameObject.events.emit(Registered)
        }
    }

    fun clear() {
        loading.clear()
    }
}
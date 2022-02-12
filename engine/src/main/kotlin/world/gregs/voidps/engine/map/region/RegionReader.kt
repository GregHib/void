package world.gregs.voidps.engine.map.region

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
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
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.spawn.ItemSpawns
import world.gregs.voidps.engine.map.spawn.NPCSpawns
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

val regionModule = module {
    single { RegionReader(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { MapDecoder(get(), get<Xteas>()) }
}

class RegionReader(
    private val collisions: CollisionReader,
    private val objects: Objects,
    private val collision: GameObjectCollision,
    private val customs: CustomObjects,
    private val objectFactory: GameObjectFactory,
    private val decoder: MapDecoder,
    private val npcSpawns: NPCSpawns,
    private val itemSpawns: ItemSpawns,
    private val definitions: ObjectDefinitions,
    override val coroutineContext: CoroutineContext = Dispatchers.IO
) : CoroutineScope {

    private val logger = InlineLogger()

    private val loading = IntOpenHashSet()

    fun unload(region: Region) = loading.remove(region.id)

    fun load(region: Region) {
        if (!loading.contains(region.id)) {
            loading.add(region.id)
            runBlocking {
                val time = measureTimeMillis {
                    val def = decoder.getOrNull(region.id) ?: return@runBlocking
                    val col = async { collisions.read(region, def) }
                    val loc = async { loadObjects(region.tile, def) }
                    col.await()
                    loc.await()
                    npcSpawns.load(region)
                    itemSpawns.load(region)
                    World.events.emit(RegionLoaded(region))
                    customs.load(region)
                }
                logger.info { "Region ${region.id} loaded in ${time}ms" }
            }
        }
    }

    private fun loadObjects(region: Tile, map: MapDefinition) {
        map.objects.forEach { location ->
            // Valid object
            val gameObject = objectFactory.spawn(
                definitions.get(location.id).stringId,
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
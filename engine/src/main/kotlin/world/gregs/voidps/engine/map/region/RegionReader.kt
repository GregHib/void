package world.gregs.voidps.engine.map.region

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.spawn.ItemSpawns
import world.gregs.voidps.engine.map.spawn.NPCSpawns
import world.gregs.voidps.engine.utility.get
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

val regionModule = module {
    single { RegionReader(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { MapDecoder(get(), get<Xteas>()) }
}

class RegionReader(
    private val collisionReader: CollisionReader,
    private val objects: Objects,
    private val collision: GameObjectCollision,
    private val customs: CustomObjects,
    private val objectFactory: GameObjectFactory,
    private val decoder: MapDecoder,
    private val npcSpawns: NPCSpawns,
    private val itemSpawns: ItemSpawns,
    private val definitions: ObjectDefinitions,
    private val collisions: Collisions,
    override val coroutineContext: CoroutineContext = Dispatchers.IO
) : CoroutineScope {

    private val logger = InlineLogger()

    private val loading = IntOpenHashSet()

    fun start() {
        val xteas: Xteas = get()
        val time = loadOld(xteas)
        logger.info { "${xteas.size} regions loaded in ${time}ms" }
    }

    private fun loadNew(): Long {
        return measureTimeMillis {
            val file = File("map.dat").readBytes()
            val reader = BufferReader(file)
            reader.startBitAccess()
            var count = 0
            repeat(reader.readBits(12)) {
                val region = Region(reader.readBits(16))
                for (plane in 0 until 4) {
                    for (x in 0 until 64) {
                        for (y in 0 until 64) {
                            if (reader.readBits(1) == 1) {
                                collisions.add(region.tile.x + x, region.tile.y + y, plane, CollisionFlag.WATER)
                                count++
                            }
                        }
                    }
                }
                val objectCount = reader.readBits(14)
                repeat(objectCount) {
                    val id = reader.readBits(16)
                    val x = reader.readBits(6)
                    val y = reader.readBits(6)
                    val plane = reader.readBits(2)
                    val type = reader.readBits(5)
                    val rotation = reader.readBits(3)

                    val def = definitions.get(id)
                    val tile = Tile(region.tile.x + x, region.tile.y + y, plane)
                    if (def.options != null) {
                        val gameObject = objectFactory.spawn(
                            id,
                            tile,
                            type,
                            rotation
                        )
                        objects.add(gameObject)
                        gameObject.events.emit(Registered)
                    }
                    collision.modifyCollision(def, tile, type, rotation, GameObjectCollision.ADD_MASK)
                }
                loadEntities(region)
            }
        }
    }

    private fun loadOld(xteas: Xteas): Long {
        return measureTimeMillis {
            for (id in xteas.keys) {
                val def = decoder.getOrNull(id) ?: continue
                val region = Region(id)
                collisionReader.read(region, def)
                loadObjects(region.tile, def)
                loadEntities(region)
            }
        }
    }

    fun unload(region: Region) = loading.remove(region.id)

    fun load(region: Region) {
        if (!loading.contains(region.id)) {
            loading.add(region.id)
            val def = decoder.getOrNull(region.id) ?: return
            collisionReader.read(region, def)
            loadObjects(region.tile, def)
            loadEntities(region)
        }
    }

    fun loadEntities(region: Region) {
        npcSpawns.load(region)
        itemSpawns.load(region)
        World.events.emit(RegionLoaded(region))
        customs.load(region)
    }

    private fun loadObjects(region: Tile, map: MapDefinition) {
        map.objects.forEach { location ->
            // Valid object
            val def = definitions.get(location.id)
            val tile = Tile(region.x + location.x, region.y + location.y, location.plane)
            if (def.options != null) {
                val gameObject = objectFactory.spawn(
                    location.id,
                    tile,
                    location.type,
                    location.rotation
                )
                objects.add(gameObject)
                gameObject.events.emit(Registered)
            }
            collision.modifyCollision(def, tile, location.type, location.rotation, GameObjectCollision.ADD_MASK)
        }
    }

    fun clear() {
        loading.clear()
    }
}
package world.gregs.voidps.tools.map.obj

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.cacheConfigModule
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.tools.map.view.graph.MutableNavigationGraph

/**
 * Finds links between objects e.g ladders, stairs, entrances, exits
 */
object WorldMapLinkIdentifier {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, cacheConfigModule,
                module {
                    allowOverride(true)
                    single { ObjectDecoder(get(), member = true, lowDetail = false) }
                    single(createdAtStart = true) { ObjectDefinitions(get()).load(path = getProperty("objectDefinitionsPath")) }
                    single { GameObjects(get(), ChunkBatchUpdates(), get()) }
                    single { Collisions() }
                    single { MapDecoder(get(), get<Xteas>()) }
                    single(createdAtStart = true) {
                        Xteas(mutableMapOf()).apply {
                            XteaLoader().load(this, getProperty("xteaPath"), getPropertyOrNull("xteaJsonKey"), getPropertyOrNull("xteaJsonValue"))
                        }
                    }
                }
            )

        }.koin
        val objects: GameObjects = koin.get()
        val cache: Cache = koin.get()
        val definitions: ObjectDefinitions = koin.get()
        val collisionReader = CollisionReader(koin.get())
        val mapDecoder: MapDecoder = koin.get()
        val collisions: Collisions = koin.get()
        val graph = MutableNavigationGraph()
        val linker = ObjectLinker(collisions)
        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.getFile(5, "m${regionX}_${regionY}") ?: continue
                regions.add(Region(regionX, regionY))
            }
        }
        val start = System.currentTimeMillis()
        val objCollision = GameObjectCollision(collisions)
        val list = mutableListOf<GameObject>()
        for (region in regions) {
            val def = mapDecoder.getOrNull(region.id) ?: continue
            def.objects.forEach { loc ->
                val tile = Tile(region.tile.x + loc.x, region.tile.y + loc.y, loc.plane)
                val obj = GameObject(loc.id, tile, loc.shape, loc.rotation)
                list.add(obj)
                objects.add(obj)
                objCollision.modify(obj, add = true)
            }
            collisionReader.read(region, def)
        }
        val cacheLinks = mutableListOf<Pair<Tile, Tile>>()
        val dungeons = WorldMapDungeons(get(), get(), get(), get())
        val mapLinks = WorldMapLinks(get())
        cacheLinks.addAll(dungeons.getLinks())
        cacheLinks.addAll(mapLinks.getLinks())
        val compare = ObjectIdentifier(linker, cacheLinks, graph)
        compare.compare(list)
        println("${graph.adjacencyList.values.sumOf { it.size }} total links found.")
        MutableNavigationGraph.save(graph, "./navgraph.json")
        println("${regions.size} regions loaded in ${System.currentTimeMillis() - start}ms")
    }

}
package world.gregs.void.tools.map.obj

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.void.cache.Cache
import world.gregs.void.engine.client.cacheConfigModule
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule
import world.gregs.void.engine.data.file.fileLoaderModule
import world.gregs.void.engine.entity.definition.load.ObjectDefinitionLoader
import world.gregs.void.engine.entity.obj.GameObject
import world.gregs.void.engine.entity.obj.GameObjectFactory
import world.gregs.void.engine.entity.obj.Objects
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.map.collision.CollisionReader
import world.gregs.void.engine.map.collision.Collisions
import world.gregs.void.engine.map.collision.GameObjectCollision
import world.gregs.void.engine.map.region.Region
import world.gregs.void.engine.map.region.Xteas
import world.gregs.void.engine.map.region.xteaModule
import world.gregs.void.tools.map.view.graph.GraphIO
import world.gregs.void.tools.map.view.graph.NavigationGraph
import world.gregs.void.utility.get
import world.gregs.void.cache.definition.decoder.MapDecoder
import world.gregs.void.cache.definition.decoder.ObjectDecoder

/**
 * Finds links between objects e.g ladders, stairs, entrances, exits
 */
object WorldMapLinkIdentifier {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, cacheConfigModule, xteaModule, fileLoaderModule,
                module {
                    single(override = true) { ObjectDecoder(get(), true, false, false) }
                    single(createdAtStart = true) { ObjectDefinitionLoader(get(), get()).run(getProperty("objectDefinitionsPath")) }
                    single { Objects() }
                    single { Collisions() }
                    single { MapDecoder(get(), get<Xteas>()) }
                }
            )

        }.koin
        val objects: Objects = koin.get()
        val cache: Cache = koin.get()
        val collisionReader = CollisionReader(koin.get())
        val mapDecoder: MapDecoder = koin.get()
        val collisions: Collisions = koin.get()
        val graph = NavigationGraph()
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
        val factory = GameObjectFactory(collisions)
        val list = mutableListOf<GameObject>()
        for (region in regions) {
            val def = mapDecoder.getOrNull(region.id) ?: continue
            def.objects.forEach { loc ->
                val tile = Tile(region.tile.x + loc.x, region.tile.y + loc.y, loc.plane)
                val obj = factory.spawn(loc.id, tile, loc.type, loc.rotation)
                list.add(obj)
                objects.add(obj)
                objCollision.modifyCollision(obj, GameObjectCollision.ADD_MASK)
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
        println("${graph.links.size} total links found.")
        GraphIO(graph, "./navgraph.json").save()
        println("${regions.size} regions loaded in ${System.currentTimeMillis() - start}ms")
    }

}
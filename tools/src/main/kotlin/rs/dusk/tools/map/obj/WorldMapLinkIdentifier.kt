package rs.dusk.tools.map.obj

import org.koin.core.context.startKoin
import org.koin.dsl.module
import rs.dusk.cache.Cache
import rs.dusk.cache.definition.decoder.MapDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.client.cacheConfigModule
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.fileLoaderModule
import rs.dusk.engine.entity.definition.load.ObjectDefinitionLoader
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.GameObjectFactory
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.CollisionReader
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.map.collision.GameObjectCollision
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.xteaModule
import rs.dusk.tools.map.view.graph.GraphIO
import rs.dusk.tools.map.view.graph.NavigationGraph
import rs.dusk.utility.get

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
                    single { MapDecoder(get(), get()) }
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
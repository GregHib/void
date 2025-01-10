package world.gregs.voidps.tools.map.obj

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.*
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.tools.map.view.graph.MutableNavigationGraph
import world.gregs.voidps.tools.property
import world.gregs.voidps.tools.propertyOrNull
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml

/**
 * Finds links between objects e.g ladders, stairs, entrances, exits
 */
object WorldMapLinkIdentifier {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val xteas: Xteas = Xteas().load(property("storage.xteas"), propertyOrNull("xteaJsonKey") ?: Xteas.DEFAULT_KEY, propertyOrNull("xteaJsonValue") ?: Xteas.DEFAULT_VALUE)
        val worldMapDetailsDecoder = WorldMapDetailsDecoder().load(cache)
        val worldMapIconDecoder = WorldMapIconDecoder().load(cache)
        val definitions: ObjectDefinitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).load(cache)).load(Yaml(), property("definitions.objects"))
        val mapDecoder = MapDecoder(xteas).load(cache)
        val collisions = Collisions()
        val collisionDecoder = CollisionDecoder(collisions)
        val graph = MutableNavigationGraph()
        val linker = ObjectLinker(collisions)
        val clientScriptDecoder = ClientScriptDecoder().load(cache)
        val objects = GameObjects(GameObjectCollisionAdd(collisions), GameObjectCollisionRemove(collisions), ZoneBatchUpdates(), definitions)
        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.data(5, "m${regionX}_${regionY}") ?: continue
                regions.add(Region(regionX, regionY))
            }
        }
        startKoin {
            modules(module {
                single { definitions }
            })
        }
        val start = System.currentTimeMillis()
        val objCollision = GameObjectCollisionAdd(collisions)
        val list = mutableListOf<GameObject>()
        for (region in regions) {
            val def = mapDecoder.getOrNull(region.id) ?: continue
            def.objects.forEach { loc ->
                val tile = Tile(region.tile.x + loc.x, region.tile.y + loc.y, loc.level)
                val obj = GameObject(loc.id, tile, loc.shape, loc.rotation)
                list.add(obj)
                objects.add(obj)
                objCollision.modify(obj)
            }
            collisionDecoder.decode(region, def)
        }
        val cacheLinks = mutableListOf<Pair<Tile, Tile>>()
        val dungeons = WorldMapDungeons(worldMapDetailsDecoder, worldMapIconDecoder, clientScriptDecoder, cache)
        val mapLinks = WorldMapLinks(clientScriptDecoder)
        cacheLinks.addAll(dungeons.getLinks())
        cacheLinks.addAll(mapLinks.getLinks())
        val compare = ObjectIdentifier(linker, cacheLinks, graph)
        compare.compare(list)
        println("${graph.adjacencyList.values.sumOf { it.size }} total links found.")
        MutableNavigationGraph.save(graph, "./navgraph.json")
        println("${regions.size} regions loaded in ${System.currentTimeMillis() - start}ms")
    }

}
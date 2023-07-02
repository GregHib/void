package world.gregs.voidps.tools.map.obj

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.tools.map.view.graph.MutableNavigationGraph
import world.gregs.voidps.tools.property
import world.gregs.voidps.tools.propertyOrNull
import world.gregs.yaml.Yaml

/**
 * Finds links between objects e.g ladders, stairs, entrances, exits
 */
object WorldMapLinkIdentifier {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val xteas: Xteas = Xteas(mutableMapOf()).apply {
            XteaLoader().load(this, property("xteaPath"), propertyOrNull("xteaJsonKey"), propertyOrNull("xteaJsonValue"))
        }
        val worldMapDetailsDecoder = WorldMapDetailsDecoder().loadCache(cache)
        val worldMapIconDecoder = WorldMapIconDecoder().loadCache(cache)
        val definitions: ObjectDefinitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).loadCache(cache)).load(Yaml(), property("objectDefinitionsPath"), null)
        val mapDecoder = MapDecoder(xteas)
        val collisions = Collisions()
        val collisionReader = CollisionReader(collisions)
        val graph = MutableNavigationGraph()
        val linker = ObjectLinker(collisions)
        val clientScriptDecoder = ClientScriptDecoder(revision634 = true).loadCache(cache)
        val objects = GameObjects(GameObjectCollision(collisions), ChunkBatchUpdates(), definitions)
        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.getFile(5, "m${regionX}_${regionY}") ?: continue
                regions.add(Region(regionX, regionY))
            }
        }
        startKoin {
            modules(module {
                single { definitions }
            })
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
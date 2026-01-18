package world.gregs.voidps.tools.map.obj

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.MapTile
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.*
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.tools.map.MapDecoder
import world.gregs.voidps.tools.map.view.graph.MutableNavigationGraph
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

/**
 * Finds links between objects e.g ladders, stairs, entrances, exits
 */
object WorldMapLinkIdentifier {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val xteas: Xteas = Xteas().load(Settings["storage.xteas"], Settings["xteaJsonKey", Xteas.DEFAULT_KEY], Settings["xteaJsonValue", Xteas.DEFAULT_VALUE])
        val worldMapDetailsDecoder = WorldMapDetailsDecoder().load(cache)
        val worldMapIconDecoder = WorldMapIconDecoder().load(cache)
        ObjectDefinitions.init(ObjectDecoder(member = true, lowDetail = false).load(cache)).load(configFiles().getValue(Settings["definitions.objects"]))
        val mapDecoder = MapDecoder(xteas).load(cache)
        val collisionDecoder = CollisionDecoder()
        val graph = MutableNavigationGraph()
        val linker = ObjectLinker()
        val clientScriptDecoder = ClientScriptDecoder().load(cache)
        val objects = GameObjects(ZoneBatchUpdates())
        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.data(5, "m${regionX}_$regionY") ?: continue
                regions.add(Region(regionX, regionY))
            }
        }
        val start = System.currentTimeMillis()
        val objCollision = GameObjectCollisionAdd()
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
            val x = region.tile.x
            val y = region.tile.y
            collisionDecoder.decode(def.tiles.map { MapTile.settings(it).toByte() }.toByteArray(), x, y)
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

package rs.dusk.tools.map.obj

import org.koin.core.context.startKoin
import org.koin.dsl.module
import rs.dusk.cache.Cache
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
import rs.dusk.engine.map.collision.CollisionFlag
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.map.collision.GameObjectCollision
import rs.dusk.engine.map.collision.add
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.obj.objectMapDecoderModule
import rs.dusk.engine.map.region.obj.xteaModule
import rs.dusk.engine.map.region.tile.BLOCKED_TILE
import rs.dusk.engine.map.region.tile.BRIDGE_TILE
import rs.dusk.engine.map.region.tile.TileData
import rs.dusk.engine.map.region.tile.TileDecoder
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
            modules(cacheModule, cacheDefinitionModule, cacheConfigModule, xteaModule, objectMapDecoderModule, fileLoaderModule,
                module {
                    single(override = true) { ObjectDecoder(get(), true, false, false) }
                    single(createdAtStart = true) { ObjectDefinitionLoader(get(), get()).run(getProperty("objectDefinitionsPath")) }
                    single { Objects() }
                    single { Collisions() }
                }
            )

        }.koin
        val objects: Objects = koin.get()
        val cache: Cache = koin.get()
        val xteas: Xteas = koin.get()
        val tileDecoder = TileDecoder()
        val mapObjDecoder: GameObjectMapDecoder = koin.get()
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
            // TODO also duplicate
            val mapData = cache.getFile(5, "m${region.x}_${region.y}") ?: continue
            val tiles = tileDecoder.read(mapData)
            val xtea = xteas[region.id]
            val locationData = cache.getFile(5, "l${region.x}_${region.y}", xtea)

            if (locationData == null) {
//            println("Missing xteas for region ${region.id} [${xtea?.toList()}].")
                continue
            }

            val locations = mapObjDecoder.read(region.x, region.y, locationData, tiles)
            locations?.forEach { loc ->
                val tile = Tile(loc.x, loc.y, loc.plane)
                val obj = factory.spawn(loc.id, tile, loc.type, loc.rotation)
                list.add(obj)
                objects.add(obj)
                objCollision.modifyCollision(obj, GameObjectCollision.ADD_MASK)
            }
            // TODO Duplicate of CollisionReader
            val x = region.tile.x
            val y = region.tile.y
            for (plane in tiles.indices) {
                for (localX in tiles[plane].indices) {
                    for (localY in tiles[plane][localX].indices) {
                        val blocked = tiles.isTile(plane, localX, localY, BLOCKED_TILE)
                        val bridge = tiles.isTile(1, localX, localY, BRIDGE_TILE)
                        if (blocked && !bridge) {
                            collisions.add(x + localX, y + localY, plane, CollisionFlag.FLOOR)
                        }
                    }
                }
            }
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

    fun Array<Array<Array<TileData?>>>.isTile(plane: Int, localX: Int, localY: Int, flag: Int): Boolean {
        return (this[plane][localX][localY]?.settings?.toInt() ?: return false) and flag == flag
    }

}
package rs.dusk.tools.map.process

import org.koin.core.context.startKoin
import org.koin.dsl.module
import rs.dusk.cache.Cache
import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.cache.definition.decoder.ClientScriptDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.cache.definition.decoder.WorldMapDetailsDecoder
import rs.dusk.cache.definition.decoder.WorldMapIconDecoder
import rs.dusk.engine.client.cacheConfigModule
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.entity.definition.ObjectDefinitions
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.GameObjectFactory
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
import rs.dusk.tools.Pipeline
import rs.dusk.tools.map.view.graph.GraphIO
import rs.dusk.tools.map.view.graph.NavigationGraph

object WorldMapDataDumper {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, cacheConfigModule, xteaModule, objectMapDecoderModule,
                module {
                    single(override = true) { ObjectDecoder(get(), true, false, false) }
                    single { ObjectDefinitions(get(), emptyMap(), emptyMap()) }
                }
            )

        }.koin

        val objectDecoder: ObjectDecoder = koin.get()
        val cache: Cache = koin.get()
        val xteas: Xteas = koin.get()
        val tileDecoder = TileDecoder()
        val mapObjDecoder: GameObjectMapDecoder = koin.get()
        val mapInfoDecoder: WorldMapInfoDecoder = koin.get()
        val scriptDecoder: ClientScriptDecoder = koin.get()
        val mapIconDecoder: WorldMapIconDecoder = koin.get()
        val mapDetailsDecoder: WorldMapDetailsDecoder = koin.get()
        val pipeline = Pipeline<Map<Tile, List<GameObject>>>()
        val collisions = Collisions()
        val graph = NavigationGraph()
        pipeline.add(WorldMapLinks(graph, objectDecoder, scriptDecoder, mapDetailsDecoder, mapIconDecoder, cache))
        pipeline.add(LadderProcessor(graph, objectDecoder, mapInfoDecoder, collisions))
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
        val map = mutableMapOf<Tile, MutableList<GameObject>>()
        regions.forEach { region ->
            // TODO also duplicate
            val mapData = cache.getFile(5, "m${region.x}_${region.y}") ?: return@forEach
            val tiles = tileDecoder.read(mapData)
            val xtea = xteas[region.id]
            val locationData = cache.getFile(5, "l${region.x}_${region.y}", xtea)

            if (locationData == null) {
//            println("Missing xteas for region ${region.id} [${xtea?.toList()}].")
                return@forEach
            }

            val objects = mapObjDecoder.read(region.x, region.y, locationData, tiles)
            objects?.forEach { loc ->
                val tile = Tile(loc.x, loc.y, loc.plane)
                val obj = factory.spawn(loc.id, tile, loc.type, loc.rotation)
                map.getOrPut(tile) {
                    mutableListOf()
                }.add(obj)
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
        pipeline.process(map)
        GraphIO(graph, "./navgraph.json").save()
        println("${regions.size} regions loaded in ${System.currentTimeMillis() - start}ms")
    }

    fun Array<Array<Array<TileData?>>>.isTile(plane: Int, localX: Int, localY: Int, flag: Int): Boolean {
        return (this[plane][localX][localY]?.settings?.toInt() ?: return false) and flag == flag
    }


}
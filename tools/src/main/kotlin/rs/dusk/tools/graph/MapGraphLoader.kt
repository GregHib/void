package rs.dusk.tools.graph

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.eventModule
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.map.collision.collisionModule
import rs.dusk.engine.map.region.RegionReader
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.obj.objectMapModule
import rs.dusk.engine.map.region.obj.xteaModule
import rs.dusk.engine.map.region.regionModule
import rs.dusk.engine.map.region.tile.tileModule

object MapGraphLoader {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            properties(mapOf(
                "cachePath" to "./cache/data/cache/",
                "xteaPath" to "./cache/data/xteas.dat",
                "fsRsaPrivate" to "1",
                "fsRsaModulus" to "1"
            ))
            modules(eventModule, collisionModule, objectMapModule, tileModule, xteaModule,
                cacheModule,
                cacheDefinitionModule, entityListModule, regionModule)
        }.koin
        val reader: RegionReader = koin.get()
        val collisions: Collisions = koin.get()
        val objects: Objects = koin.get()
        val xteas: Xteas = koin.get()
        val cache: Cache = koin.get()
        val graph = MapGraph(reader, collisions, objects, xteas, cache)
        graph.load(12342)
    }
}
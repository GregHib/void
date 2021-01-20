package world.gregs.void.tools.graph

import org.koin.core.context.startKoin
import world.gregs.void.cache.Cache
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule
import world.gregs.void.engine.entity.list.entityListModule
import world.gregs.void.engine.entity.obj.Objects
import world.gregs.void.engine.event.eventModule
import world.gregs.void.engine.map.collision.Collisions
import world.gregs.void.engine.map.collision.collisionModule
import world.gregs.void.engine.map.region.RegionReader
import world.gregs.void.engine.map.region.Xteas
import world.gregs.void.engine.map.region.regionModule
import world.gregs.void.engine.map.region.xteaModule

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
            modules(eventModule, collisionModule, xteaModule, cacheModule, cacheDefinitionModule, entityListModule, regionModule)
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
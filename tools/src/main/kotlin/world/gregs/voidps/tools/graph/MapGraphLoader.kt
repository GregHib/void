package world.gregs.voidps.tools.graph

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.collisionModule
import world.gregs.voidps.engine.map.collision.strategy.LandCollision
import world.gregs.voidps.engine.map.region.RegionReader
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.engine.map.region.regionModule
import world.gregs.voidps.engine.map.region.xteaModule

object MapGraphLoader {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            properties(mapOf(
                "cachePath" to "./data/cache/",
                "xteaPath" to "./data/xteas.dat"
            ))
            modules(eventModule, collisionModule, xteaModule, cacheModule, cacheDefinitionModule, entityListModule, regionModule)
        }.koin
        val reader: RegionReader = koin.get()
        val collisions: Collisions = koin.get()
        val objects: Objects = koin.get()
        val xteas: Xteas = koin.get()
        val cache: Cache = koin.get()
        val graph = MapGraph(reader, objects, xteas, cache, LandCollision(collisions))
        graph.load(12342)
    }
}
package world.gregs.voidps.tools.graph

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas

object MapGraphLoader {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            properties(mapOf(
                "cachePath" to "./data/cache/",
                "xteaPath" to "./data/xteas.dat"
            ))
            modules(module {
                single { EventHandlerStore() }
                single { Objects() }
                single(createdAtStart = true) {
                    Xteas(mutableMapOf()).apply {
                        XteaLoader().load(this, getProperty("xteaPath"), getPropertyOrNull("xteaJsonKey"), getPropertyOrNull("xteaJsonValue"))
                    }
                }
                single(createdAtStart = true) { GameObjectCollision(get()) }
                single { Collisions() }
            }, cacheModule, cacheDefinitionModule)
        }.koin
        val collisions: Collisions = koin.get()
        val objects: Objects = koin.get()
        val xteas: Xteas = koin.get()
        val cache: Cache = koin.get()
        val graph = MapGraph(objects, xteas, cache, collisions)
        graph.load(12342)
    }
}
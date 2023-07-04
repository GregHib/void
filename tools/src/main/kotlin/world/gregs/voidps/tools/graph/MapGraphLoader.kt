package world.gregs.voidps.tools.graph

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.tools.property
import world.gregs.voidps.tools.propertyOrNull
import world.gregs.yaml.Yaml

object MapGraphLoader {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val collisions: Collisions = Collisions()
        val objectDefinitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).loadCache(cache))
            .load(Yaml(), property("objectDefinitionsPath"), null)
        val objects = GameObjects(GameObjectCollision(Collisions()), ChunkBatchUpdates(), objectDefinitions)
        val xteas: Xteas = Xteas(mutableMapOf()).apply {
            XteaLoader().load(this, property("xteaPath"), propertyOrNull("xteaJsonKey"), propertyOrNull("xteaJsonValue"))
        }
        val graph = MapGraph(objects, xteas, cache, collisions)
        graph.load(12342)
    }
}
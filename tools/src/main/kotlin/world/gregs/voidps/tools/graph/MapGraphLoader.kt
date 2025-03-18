package world.gregs.voidps.tools.graph

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.tools.property

object MapGraphLoader {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val collisions: Collisions = Collisions()
        val objectDefinitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).load(cache))
            .load(property("definitions.objects"))
        val objects = GameObjects(GameObjectCollisionAdd(collisions), GameObjectCollisionRemove(collisions), ZoneBatchUpdates(), objectDefinitions)
        val xteas = Xteas()//.load("./xteas.json")
        val graph = MapGraph(objects, xteas, cache, collisions)
        graph.load(12342)
    }
}
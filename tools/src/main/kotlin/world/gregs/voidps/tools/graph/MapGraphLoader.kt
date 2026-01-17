package world.gregs.voidps.tools.graph

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.tools.cache.Xteas

object MapGraphLoader {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val objectDefinitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).load(cache))
            .load(configFiles().getValue(Settings["definitions.objects"]))
        val objects = GameObjects(GameObjectCollisionAdd(), GameObjectCollisionRemove(), ZoneBatchUpdates(), objectDefinitions)
        val xteas = Xteas() // .load("./xteas.json")
        val graph = MapGraph(objects, xteas, cache)
        graph.load(12342)
    }
}

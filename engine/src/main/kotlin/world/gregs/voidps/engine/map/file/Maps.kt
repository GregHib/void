package world.gregs.voidps.engine.map.file

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.engine.utility.getProperty
import java.io.File

class Maps(
    cache: Cache,
    xteas: Xteas,
    definitions: ObjectDefinitions,
    factory: GameObjectFactory,
    objects: Objects,
    collision: GameObjectCollision,
    private val mapExtract: MapExtract,
    private val collisions: Collisions
) {
    private val decoder = MapDecoder(cache, xteas)
    private val objectLoader = MapObjectLoader(definitions, factory, objects, collision)

    fun load(compress: Boolean = getProperty("compressMaps") == "true", mapPath: String = getProperty("mapPath")) {
        val mapFile = File(mapPath)
        if (!compress || !mapFile.exists()) {
            cacheLoad()
            if (compress) {
                compress(mapFile)
            }
        } else {
            extract(mapFile)
        }
    }

    private fun extract(map: File) {
        mapExtract.loadMap(map)
    }

    private fun cacheLoad() {
        MapLoader(decoder, CollisionReader(collisions), objectLoader).run()
    }

    private fun compress(map: File) {
        MapCompress(map, collisions, decoder).run()
    }
}
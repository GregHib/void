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

    fun load(compress: Boolean = getProperty("compressMaps") == "true", mapPath: String = getProperty("mapPath"), indicesPath: String = getProperty("mapIndexPath")) {
        val mapFile = File(mapPath)
        val indexFile = File(indicesPath)
        if (!compress || !mapFile.exists() || !indexFile.exists()) {
            cacheLoad()
            if (compress) {
                compress(mapFile, indexFile)
            }
        } else {
            extract(mapFile, indexFile)
        }
    }

    private fun extract(map: File, indices: File) {
        mapExtract.run(map, indices)
    }

    private fun cacheLoad() {
        MapLoader(decoder, CollisionReader(collisions), objectLoader).run()
    }

    private fun compress(map: File, indices: File) {
        MapCompress(map, indices, collisions, decoder).run()
    }
}
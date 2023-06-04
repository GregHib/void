package world.gregs.voidps.engine.map.file

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Xteas
import java.io.File

class Maps(
    cache: Cache,
    xteas: Xteas,
    private val definitions: ObjectDefinitions,
    private val objects: GameObjects,
    private val mapExtract: MapExtract,
    private val collisions: Collisions
) {
    private val decoder = MapDecoder(cache, xteas)

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
        MapLoader(decoder, CollisionReader(collisions), definitions, objects).run()
    }

    private fun compress(map: File) {
        MapCompress(map, collisions, decoder).run()
    }
}
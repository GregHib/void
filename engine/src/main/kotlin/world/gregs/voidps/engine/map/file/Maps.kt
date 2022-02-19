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
import java.io.File

class Maps(
    cache: Cache,
    xteas: Xteas,
    definitions: ObjectDefinitions,
    factory: GameObjectFactory,
    objects: Objects,
    collision: GameObjectCollision,
    private val collisions: Collisions
) {
    private val decoder = MapDecoder(cache, xteas)
    private val objectLoader = MapObjectLoader(definitions, factory, objects, collision)

    fun load(compress: Boolean, path: String) {
        val file = File(path)
        if (!compress || !file.exists()) {
            load()
            if (compress) {
                compress(file)
            }
        } else {
            extract(file)
        }
    }

    fun extract(file: File) {
        MapExtract(file, collisions, objectLoader).run()
    }

    fun load() {
        MapLoader(decoder, CollisionReader(collisions), objectLoader).run()
    }

    fun compress(file: File) {
        MapCompress(file, collisions, decoder).run()
    }
}
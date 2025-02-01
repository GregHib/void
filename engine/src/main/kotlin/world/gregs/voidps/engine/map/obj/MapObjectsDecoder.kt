package world.gregs.voidps.engine.map.obj

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.decoder.MapObjectDecoder
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects

/**
 * Adds collision for all blocked tiles except bridges
 */
class MapObjectsDecoder(
    private val objects: GameObjects,
    private val definitions: ObjectDefinitions
) : MapObjectDecoder() {

    fun decode(cache: Cache, settings: ByteArray, regionX: Int, regionY: Int, keys: IntArray?) {
        val objectData = cache.data(Index.MAPS, "l${regionX}_${regionY}", xtea = keys) ?: return
        val zoneTileX = regionX shl 6
        val zoneTileY = regionY shl 6
        super.decode(objectData, settings, zoneTileX, zoneTileY)
    }

    override fun add(objectId: Int, localX: Int, localY: Int, level: Int, shape: Int, rotation: Int, regionTileX: Int, regionTileY: Int) {
        if (objectId > definitions.definitions.size) {
            return
        }
        objects.set(objectId, regionTileX + localX, regionTileY + localY, level, shape, rotation, definitions.getValue(objectId))
    }
}

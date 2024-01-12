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
class ObjectsRotatedReader(
    private val objects: GameObjects,
    private val definitions: ObjectDefinitions
) : MapObjectDecoder() {

    private var zoneRotation: Int = 0

    fun loadObjects(cache: Cache, tiles: LongArray, sourceX: Int, sourceY: Int, regionX: Int, regionY: Int, rotation: Int, keys: IntArray?) {
        zoneRotation = rotation
        val objectData = cache.getFile(Index.MAPS, "l${sourceX}_${sourceY}", xtea = keys) ?: return
        val reader = BufferReader(objectData)
        super.loadObjects(reader, tiles, regionX, regionY)
    }

    /**
     * TODO only add objects within zone
     */
    override fun add(objectId: Int, localX: Int, localY: Int, level: Int, shape: Int, rotation: Int, regionX: Int, regionY: Int) {
        if (objectId > definitions.definitions.size) {
            return
        }
        val def = definitions.getValue(objectId)
        val objRotation = (rotation + zoneRotation) and 0x3
        val rotX = (regionX shl 6) + rotateX(localX, localY, def.sizeX, def.sizeY, objRotation, zoneRotation)
        val rotY = (regionY shl 6) + rotateY(localX, localY, def.sizeX, def.sizeY, objRotation, zoneRotation)
        objects.set(objectId, rotX, rotY, level, shape, objRotation, def)
    }

    companion object {
        private fun rotateX(
            objX: Int,
            objY: Int,
            sizeX: Int,
            sizeY: Int,
            objRotation: Int,
            zoneRotation: Int
        ): Int {
            var x = sizeX
            var y = sizeY
            val rotation = zoneRotation and 0x3
            if (objRotation and 0x1 == 1) {
                val temp = x
                x = y
                y = temp
            }
            if (rotation == 0) {
                return objX
            }
            if (rotation == 1) {
                return objY
            }
            return if (rotation == 2) 7 - objX - x + 1 else 7 - objY - y + 1
        }

        private fun rotateY(
            objX: Int,
            objY: Int,
            sizeX: Int,
            sizeY: Int,
            objRotation: Int,
            zoneRotation: Int
        ): Int {
            val rotation = zoneRotation and 0x3
            var x = sizeY
            var y = sizeX
            if (objRotation and 0x1 == 1) {
                val temp = y
                y = x
                x = temp
            }
            if (rotation == 0) {
                return objY
            }
            if (rotation == 1) {
                return 7 - objX - y + 1
            }
            return if (rotation == 2) 7 - objY - x + 1 else objX
        }
    }
}

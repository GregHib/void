package world.gregs.voidps.engine.map.file

import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Region

/**
 * Adds collision data and game objects into the world
 * Used in both cache [MapLoader] and file [MapExtract]
 */
class MapObjectLoader(
    private val definitions: ObjectDefinitions,
    private val factory: GameObjectFactory,
    private val objects: Objects,
    private val collision: GameObjectCollision
) {
    fun load(region: Region, location: MapObject) {
        load(location.id, Tile(region.tile.x + location.x, region.tile.y + location.y, location.plane), location.type, location.rotation)
    }

    private fun interactive(definition: ObjectDefinition) = definition.options != null || definition.name.equals("table", ignoreCase = true)

    fun load(chunk: Chunk, id: Int, x: Int, y: Int, type: Int, rotation: Int, chunkRotation: Int) {
        val def = definitions.get(id)
        val tile = chunk.tile.add(
            rotateX(x, y, def.sizeX, def.sizeY, rotation, chunkRotation),
            rotateY(x, y, def.sizeX, def.sizeY, rotation, chunkRotation)
        )
        load(id, tile, type, rotation)
    }

    fun load(id: Int, tile: Tile, type: Int, rotation: Int) {
        val def = definitions.get(id)
        if (interactive(def)) {
            val gameObject = factory.spawn(
                id,
                tile,
                type,
                rotation
            )
            objects.add(gameObject)
            gameObject.events.emit(Registered)
        }
        collision.modifyCollision(def, tile, type, rotation, GameObjectCollision.ADD_MASK)
    }

    companion object {
        private fun rotateX(
            objX: Int,
            objY: Int,
            sizeX: Int,
            sizeY: Int,
            objRotation: Int,
            chunkRotation: Int
        ): Int {
            var x = sizeX
            var y = sizeY
            val rotation = chunkRotation and 0x3
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
            chunkRotation: Int
        ): Int {
            val rotation = chunkRotation and 0x3
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
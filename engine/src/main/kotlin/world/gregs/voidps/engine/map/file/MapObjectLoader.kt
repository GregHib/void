package world.gregs.voidps.engine.map.file

import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.Tile
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
        load(region, location.id, location.x, location.y, location.plane, location.type, location.rotation)
    }

    fun interactive(definition: ObjectDefinition) = definition.options != null || definition.name.equals("table", ignoreCase = true)

    fun load(region: Region, id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int) {
        val def = definitions.get(id)
        val tile = Tile(region.tile.x + x, region.tile.y + y, plane)
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
}
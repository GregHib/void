package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile

class GameObjectFactory(
    private val store: EventHandlerStore,
    private val definitions: ObjectDefinitions
) {

    fun spawn(objectId: Int, tile: Tile, type: Int, rotation: Int, owner: String? = null): GameObject {
        val def = definitions.get(objectId)
        val gameObject = GameObject(def.stringId, tile, type, rotation, owner)
        setup(gameObject, def)
        return gameObject
    }

    fun spawn(objectId: String, tile: Tile, type: Int, rotation: Int, owner: String? = null): GameObject {
        val gameObject = GameObject(objectId, tile, type, rotation, owner)
        setup(gameObject, definitions.get(objectId))
        return gameObject
    }

    fun setup(gameObject: GameObject, def: ObjectDefinition) {
        gameObject.def = def
        gameObject.size = Size(if (gameObject.rotation and 0x1 == 1) def.sizeY else def.sizeX, if (gameObject.rotation and 0x1 == 1) def.sizeX else def.sizeY)
        store.populate(gameObject)
    }
}
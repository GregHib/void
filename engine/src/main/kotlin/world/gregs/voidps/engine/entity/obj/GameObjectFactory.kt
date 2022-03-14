package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.strat.DecorationTargetStrategy
import world.gregs.voidps.engine.path.strat.EntityTileTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.path.strat.WallTargetStrategy

class GameObjectFactory(
    private val collisions: Collisions,
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
        gameObject.interactTarget = when (gameObject.type) {
            in 0..2, 9 -> WallTargetStrategy(collisions, gameObject)
            in 3..8 -> DecorationTargetStrategy(collisions, gameObject)
            10, 11, 22 -> {
                var flag: Int = gameObject.def.blockFlag
                flag = (0xf and flag shl gameObject.rotation) + (flag shr -gameObject.rotation + 4)
                RectangleTargetStrategy(collisions, entity = gameObject, blockFlag = flag, allowUnder = true)
            }
            else -> EntityTileTargetStrategy(gameObject)
        }
        store.populate(gameObject)
    }
}
package world.gregs.void.engine.entity.obj

import org.koin.dsl.module
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.map.collision.Collisions
import world.gregs.void.engine.path.strat.DecorationTargetStrategy
import world.gregs.void.engine.path.strat.EntityTileTargetStrategy
import world.gregs.void.engine.path.strat.RectangleTargetStrategy
import world.gregs.void.engine.path.strat.WallTargetStrategy

/**
 * @author GregHib <greg@gregs.world>
 * @since August 22, 2020
 */
class GameObjectFactory(private val collisions: Collisions) {

    fun spawn(objectId: Int, tile: Tile, type: Int, rotation: Int, owner: String? = null): GameObject {
        val gameObject = GameObject(objectId, tile, type, rotation, owner)
        gameObject.interactTarget = when (gameObject.type) {
            in 0..2, 9 -> WallTargetStrategy(collisions, gameObject)
            in 3..8 -> DecorationTargetStrategy(collisions, gameObject)
            10, 11, 22 -> {
                var flag: Int = gameObject.def.blockFlag
                flag = (0xf and flag shl gameObject.rotation) + (flag shr -gameObject.rotation + 4)
                RectangleTargetStrategy(collisions, entity = gameObject, blockFlag = flag)
            }
            else -> EntityTileTargetStrategy(gameObject)
        }
        return gameObject
    }
}

val objectFactoryModule = module {
    single { GameObjectFactory(get()) }
}
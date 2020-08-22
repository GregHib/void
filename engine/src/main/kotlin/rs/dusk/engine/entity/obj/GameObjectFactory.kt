package rs.dusk.engine.entity.obj

import org.koin.dsl.module
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.path.strat.DecorationTargetStrategy
import rs.dusk.engine.path.strat.EntityTileTargetStrategy
import rs.dusk.engine.path.strat.RectangleTargetStrategy
import rs.dusk.engine.path.strat.WallTargetStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since August 22, 2020
 */
class GameObjectFactory(private val collisions: Collisions) {

    fun spawn(objectId: Int, tile: Tile, type: Int, rotation: Int, owner: String? = null): GameObject {
        val gameObject = GameObject(objectId, tile, type, rotation, owner)
        gameObject.interactTarget = when (gameObject.type) {
            in 0..2, 9 -> WallTargetStrategy(collisions, gameObject)
            in 3..8 -> DecorationTargetStrategy(collisions, gameObject)
            10, 11, 22 -> RectangleTargetStrategy(collisions, entity = gameObject, blockFlag = gameObject.def.blockFlag)
            else -> EntityTileTargetStrategy(gameObject)
        }
        return gameObject
    }
}

val objectFactoryModule = module {
    single { GameObjectFactory(get()) }
}
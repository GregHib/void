package world.gregs.voidps.engine.entity.character.target

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile

object TargetStrategies {

    fun <T : Any> get(entity: T): TargetStrategy = when (entity) {
        is Tile -> TileTargetStrategy(entity)
        is GameObject -> ObjectTargetStrategy(entity)
        is Entity -> EntityTargetStrategy(entity)
        else -> DefaultTargetStrategy
    }
}
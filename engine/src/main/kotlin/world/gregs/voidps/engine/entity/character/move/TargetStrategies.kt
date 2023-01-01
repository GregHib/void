package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.strat.SingleTileTargetStrategy
import world.gregs.voidps.engine.path.strat.TileTargetStrategy

/**
 * Determines the correct strategy to use to reach a target [Entity] or [Tile]
 */
object TargetStrategies {
    @Throws(IllegalArgumentException::class)
    fun getStrategy(any: Any): TileTargetStrategy {
        return when (any) {
            is Tile -> SingleTileTargetStrategy(any)
            is Entity -> getEntityStrategy(any)
            else -> throw IllegalArgumentException("No target strategy found for $any")
        }
    }

    private fun getEntityStrategy(entity: Entity): TileTargetStrategy {
        return when (entity) {
            is Character -> entity.interactTarget
            is GameObject -> entity.interactTarget
            is FloorItem -> entity.interactTarget
            else -> SingleTileTargetStrategy(entity.tile, entity.size)
        }
    }
}
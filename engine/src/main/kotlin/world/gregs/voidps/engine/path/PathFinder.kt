package world.gregs.voidps.engine.path

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.strat.SingleTileTargetStrategy
import world.gregs.voidps.engine.path.strat.TileTargetStrategy

/**
 * Determines the correct strategy to use to reach a target [Entity] or [Tile]
 */
class PathFinder {

    fun find(source: Character, path: Path, type: PathType, ignore: Boolean): PathResult {
        if (path.strategy.reached(source.tile, source.size)) {
            return PathResult.Success(source.tile)
        }
//        return algorithm.find(source.tile, source.size, path, source.traversal, if (ignore) this.ignored else source.collision)
        return PathResult.Failure
    }

    companion object {
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
}
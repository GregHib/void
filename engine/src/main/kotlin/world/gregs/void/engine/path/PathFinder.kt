package world.gregs.void.engine.path

import org.koin.dsl.module
import world.gregs.void.engine.entity.Entity
import world.gregs.void.engine.entity.character.Character
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.item.FloorItem
import world.gregs.void.engine.entity.obj.GameObject
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.algorithm.AxisAlignment
import world.gregs.void.engine.path.algorithm.BreadthFirstSearch
import world.gregs.void.engine.path.algorithm.DirectDiagonalSearch
import world.gregs.void.engine.path.algorithm.DirectSearch
import world.gregs.void.engine.path.strat.EntityTileTargetStrategy
import world.gregs.void.engine.path.strat.TileTargetStrategy

val pathFindModule = module {
    single { DirectSearch() }
    single { DirectDiagonalSearch() }
    single { AxisAlignment() }
    single { BreadthFirstSearch() }
    single { PathFinder(get(), get(), get(), get()) }
}

/**
 * Determines the correct strategy to use to reach a target [Entity] or [Tile]
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 21, 2020
 */
class PathFinder(
    private val aa: AxisAlignment,
    private val ds: DirectSearch,
    private val dd: DirectDiagonalSearch,
    private val bfs: BreadthFirstSearch
) {

    fun find(source: Character, tile: Tile, smart: Boolean = true): PathResult {
        val strategy = getStrategy(tile)
        return find(source, strategy, smart)
    }

    fun find(source: Character, target: Entity, smart: Boolean = true): PathResult {
        return find(source, getEntityStrategy(target), smart)
    }

    fun find(source: Character, strategy: TargetStrategy, smart: Boolean = true): PathResult {
        if (strategy.reached(source.tile, source.size)) {
            return PathResult.Success(source.tile)
        }
        val algorithm = getAlgorithm(source, smart)
        return algorithm.find(source.tile, source.size, source.movement, strategy, source.movement.traversal)
    }

    fun getAlgorithm(source: Character, smart: Boolean): PathAlgorithm {
        return if (source is Player) {
            if (smart) bfs else dd
        } else {
            aa
        }
    }

    companion object {
        @Throws(IllegalArgumentException::class)
        fun getStrategy(any: Any): TargetStrategy {
            return when (any) {
                is Tile -> TileTargetStrategy(any)
                is Entity -> getEntityStrategy(any)
                else -> throw IllegalArgumentException("No target strategy found for $any")
            }
        }

        fun getEntityStrategy(entity: Entity): TargetStrategy {
            return when (entity) {
                is Character -> entity.interactTarget
                is GameObject -> entity.interactTarget
                is FloorItem -> entity.interactTarget
                else -> EntityTileTargetStrategy(entity)
            }
        }
    }
}
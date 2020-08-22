package rs.dusk.engine.path

import org.koin.dsl.module
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.item.FloorItem
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import rs.dusk.engine.path.find.AxisAlignment
import rs.dusk.engine.path.find.BreadthFirstSearch
import rs.dusk.engine.path.find.DirectDiagonalSearch
import rs.dusk.engine.path.find.DirectSearch
import rs.dusk.engine.path.strat.EntityTileTargetStrategy
import rs.dusk.engine.path.strat.TileTargetStrategy

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
            return PathResult.Success.Complete(source.tile)
        }
        val finder = getFinder(source, smart)
        return finder.find(source.tile, source.size, source.movement, strategy, source.movement.traversal)
    }

    fun getFinder(source: Character, smart: Boolean): Finder {
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
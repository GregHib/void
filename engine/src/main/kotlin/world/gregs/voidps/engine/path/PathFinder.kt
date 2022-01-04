package world.gregs.voidps.engine.path

import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.collision
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.algorithm.*
import world.gregs.voidps.engine.path.strat.EntityTileTargetStrategy
import world.gregs.voidps.engine.path.strat.SingleTileTargetStrategy
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.traversal

val pathFindModule = module {
    single { AvoidAlgorithm() }
    single { DirectSearch() }
    single { DirectDiagonalSearch() }
    single { AxisAlignment() }
    single {
        BreadthFirstSearch(object : DefaultPool<BreadthFirstSearchFrontier>(10) {
            override fun produceInstance() = BreadthFirstSearchFrontier()
        })
    }
    single {
        val size = get<NavigationGraph>().size
        Dijkstra(
            get(),
            object : DefaultPool<DijkstraFrontier>(10) {
                override fun produceInstance() = DijkstraFrontier(size)
            }
        )
    }
    single { PathFinder(get(), get(), get(), get()) }
}

/**
 * Determines the correct strategy to use to reach a target [Entity] or [Tile]
 */
class PathFinder(
    private val aa: AxisAlignment,
    private val ds: DirectSearch,
    private val dd: DirectDiagonalSearch,
    private val bfs: BreadthFirstSearch,
) {

    fun find(source: Character, tile: Tile, smart: Boolean = true): PathResult {
        val strategy = getStrategy(tile)
        return find(source, Path(strategy), smart)
    }

    fun find(source: Character, target: Entity, smart: Boolean = true): PathResult {
        return find(source, Path(getEntityStrategy(target)), smart)
    }

    fun find(source: Character, path: Path, smart: Boolean): PathResult {
        if (path.strategy.reached(source.tile, source.size)) {
            return PathResult.Success(source.tile)
        }
        val algorithm = getAlgorithm(source, smart)
        return algorithm.find(source.tile, source.size, path, source.traversal, source.collision)
    }

    fun getAlgorithm(source: Character, smart: Boolean): TilePathAlgorithm {
        return if (source is Player) {
            if (smart) bfs else dd
        } else {
            aa
        }
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

        fun getEntityStrategy(entity: Entity): TileTargetStrategy {
            return when (entity) {
                is Character -> entity.interactTarget
                is GameObject -> entity.interactTarget
                is FloorItem -> entity.interactTarget
                else -> EntityTileTargetStrategy(entity)
            }
        }
    }
}
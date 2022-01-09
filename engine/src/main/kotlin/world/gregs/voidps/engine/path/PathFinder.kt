package world.gregs.voidps.engine.path

import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.algorithm.*
import world.gregs.voidps.engine.path.strat.EntityTileTargetStrategy
import world.gregs.voidps.engine.path.strat.SingleTileTargetStrategy
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.traversal

val pathFindModule = module {
    single { RetreatAlgorithm() }
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
    single { PathFinder(get(), get(), get(), get(), get(), get()) }
}

/**
 * Determines the correct strategy to use to reach a target [Entity] or [Tile]
 */
class PathFinder(
    private val aa: AxisAlignment,
    private val ds: DirectSearch,
    private val dd: DirectDiagonalSearch,
    private val bfs: BreadthFirstSearch,
    private val retreat: RetreatAlgorithm,
    private val provider: CollisionStrategyProvider
) {

    fun find(source: Character, tile: Tile, type: PathType, ignore: Boolean = true): PathResult {
        val strategy = getStrategy(tile)
        return find(source, Path(strategy), type, ignore)
    }

    fun find(source: Character, target: Entity, type: PathType, ignore: Boolean = true): PathResult {
        return find(source, Path(getEntityStrategy(target)), type, ignore)
    }

    fun find(source: Character, path: Path, type: PathType, ignore: Boolean): PathResult {
        if (path.strategy.reached(source.tile, source.size)) {
            return PathResult.Success(source.tile)
        }
        val algorithm = getAlgorithm(type)
        return algorithm.find(source.tile, source.size, path, source.traversal, provider.get(source, ignore = ignore))
    }

    fun getAlgorithm(type: PathType): TilePathAlgorithm {
        return when (type) {
            PathType.Dumb -> aa
            PathType.Follow -> dd
            PathType.Smart -> bfs
            PathType.Retreat -> retreat
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
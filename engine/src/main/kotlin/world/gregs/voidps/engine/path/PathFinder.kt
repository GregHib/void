package world.gregs.voidps.engine.path

import kotlinx.io.pool.DefaultPool
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.strategy.IgnoredCollision
import world.gregs.voidps.engine.map.nav.NavigationGraph
import world.gregs.voidps.engine.path.algorithm.*
import world.gregs.voidps.engine.path.strat.SingleTileTargetStrategy
import world.gregs.voidps.engine.path.strat.TileTargetStrategy

val pathFindModule = module {
    single { RetreatAlgorithm() }
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
    single { PathFinder(get(), get(), get(), get(), get()) }
}

/**
 * Determines the correct strategy to use to reach a target [Entity] or [Tile]
 */
class PathFinder(
    private val axis: AxisAlignment,
    private val direct: DirectDiagonalSearch,
    private val bfs: BreadthFirstSearch,
    private val retreat: RetreatAlgorithm,
    private val ignored: IgnoredCollision
) {

    fun find(source: Character, path: Path, type: PathType, ignore: Boolean): PathResult {
        if (path.strategy.reached(source.tile, source.size)) {
            return PathResult.Success(source.tile)
        }
        val algorithm = getAlgorithm(type)
        return algorithm.find(source.tile, source.size, path, source.traversal, if (ignore) this.ignored else source.collision)
    }

    private fun getAlgorithm(type: PathType): TilePathAlgorithm = when (type) {
        PathType.Dumb -> axis
        PathType.Follow -> direct
        PathType.Smart -> bfs
        PathType.Retreat -> retreat
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
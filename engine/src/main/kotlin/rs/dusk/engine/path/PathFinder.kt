package rs.dusk.engine.path

import org.koin.dsl.module
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.item.FloorItem
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.path.find.AxisAlignment
import rs.dusk.engine.path.find.BreadthFirstSearch
import rs.dusk.engine.path.find.DirectSearch
import rs.dusk.engine.path.target.*

val pathFindModule = module {
    single { DirectSearch() }
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
    private val collisions: Collisions,
    private val aa: AxisAlignment,
    private val ds: DirectSearch,
    private val bfs: BreadthFirstSearch
) {

    fun find(source: Character, tile: Tile): PathResult {
        val strategy = TileTargetStrategy(tile = tile)
        if(strategy.reached(source.tile, source.size)) {
            return PathResult.Success.Complete(source.tile)
        }
        val finder = getFinder(source)
        return finder.find(source.tile, source.size, source.movement, strategy, source.movement.traversal)
    }

    fun find(source: Character, target: Entity): PathResult {
        val strategy = getStrategy(target)
        if(strategy.reached(source.tile, source.size)) {
            return PathResult.Success.Complete(source.tile)
        }
        val finder = getFinder(source)
        return finder.find(source.tile, source.size, source.movement, strategy, source.movement.traversal)
    }

    fun getFinder(source: Character): Finder {
        return if (source is Player) {
            bfs
        } else {
            aa
        }
    }

    fun getStrategy(target: Entity) = when (target) {
        is GameObject -> when (target.type) {
            in 0..2, 9 -> WallTargetStrategy(
                collisions,
                tile = target.tile,
                size = target.size,
                rotation = target.rotation,
                type = target.type
            )
            in 3..8 -> DecorationTargetStrategy(
                collisions,
                tile = target.tile,
                size = target.size,
                rotation = target.rotation,
                type = target.type
            )
            10, 11, 22 -> RectangleTargetStrategy(
                collisions,
                tile = target.tile,
                size = target.size,
                blockFlag = target.def.blockFlag
            )
            else -> TileTargetStrategy(tile = target.tile)
        }
        is FloorItem -> PointTargetStrategy(
            tile = target.tile,
            size = target.size
        )
        is Character -> RectangleTargetStrategy(
            collisions,
            tile = target.tile,
            size = target.size
        )
        else -> TileTargetStrategy(tile = target.tile)
    }
}
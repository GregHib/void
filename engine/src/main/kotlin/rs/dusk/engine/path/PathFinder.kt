package rs.dusk.engine.path

import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.Indexed
import rs.dusk.engine.model.entity.item.FloorItem
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.path.find.BreadthFirstSearch
import rs.dusk.engine.path.obstruction.LargeObstruction
import rs.dusk.engine.path.obstruction.MediumObstruction
import rs.dusk.engine.path.obstruction.SmallObstruction
import rs.dusk.engine.path.target.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 21, 2020
 */
class PathFinder(
    private val collisions: Collisions,
    private val bfs: BreadthFirstSearch,
    private val small: SmallObstruction,
    private val medium: MediumObstruction,
    private val large: LargeObstruction
) {

    fun find(source: Indexed, tile: Tile): PathResult {
        val obs = getObstructions(source.size)
        val strategy = TileTargetStrategy(tile = tile)
        val finder = getFinder(source)
        return finder.find(source.tile, source.size, source.movement, strategy, obs)
    }

    fun find(source: Indexed, target: Entity): PathResult {
        val obs = getObstructions(source.size)
        val strategy = getStrategy(target)
        val finder = getFinder(source)
        return finder.find(source.tile, source.size, source.movement, strategy, obs)
    }

    fun getFinder(source: Indexed): Finder {
        // TODO
        return bfs
    }

    fun getObstructions(size: Size) = when {
        size.width == 1 && size.height == 1 -> small
        size.width == 2 && size.height == 2 -> medium
        else -> large
    }

    fun getStrategy(target: Entity) = when (target) {
        is Location -> when (target.type) {
            0, 1, 2, 9 -> WallTargetStrategy(
                collisions,
                tile = target.tile,
                size = target.size,
                rotation = target.rotation,
                type = target.type
            )
            3, 4, 5, 6, 7, 8 -> DecorationTargetStrategy(
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
        is Indexed -> RectangleTargetStrategy(
            collisions,
            tile = target.tile,
            size = target.size
        )
        else -> TileTargetStrategy(tile = target.tile)
    }
}
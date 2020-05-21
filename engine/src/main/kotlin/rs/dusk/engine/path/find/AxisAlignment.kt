package rs.dusk.engine.path.find

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.index.Movement
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.block
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.path.Finder
import rs.dusk.engine.path.ObstructionStrategy
import rs.dusk.engine.path.PathResult
import rs.dusk.engine.path.TargetStrategy
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 20, 2020
 */
class AxisAlignment(private val collisions: Collisions) : Finder {

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        obstruction: ObstructionStrategy
    ): PathResult {
        val queue = LinkedList<Direction>()// TODO switch out queue with entities movement
        val delta = strategy.tile.delta(tile)
        val direction = toDirection(delta)
        horizontal(queue, tile, size, direction.horizontal(), direction.vertical(), strategy)
        return PathResult.Success
    }


    fun horizontal(
        queue: Deque<Direction>,
        tile: Tile,
        size: Size,
        horizontal: Direction,
        vertical: Direction,
        strategy: TargetStrategy
    ) {
        var offset = 0
        if (horizontal != Direction.NONE) {
            while (!strategy.reached(tile.x + offset, tile.y, tile.plane, size)) {
                if (collisions.check(tile.x + offset, tile.y, tile.plane, horizontal.block())) {
                    break
                }
                offset += horizontal.delta.x
                queue.add(horizontal)
            }
        }
        if (vertical != Direction.NONE &&
            !strategy.reached(tile.x + offset, tile.y, tile.plane, size) &&
            !collisions.check(tile.x + offset, tile.y + vertical.delta.y, tile.plane, vertical.block())
        ) {
            vertical(queue, tile.add(x = offset), size, horizontal, vertical, strategy)
        }
    }

    fun vertical(
        queue: Deque<Direction>,
        tile: Tile,
        size: Size,
        horizontal: Direction,
        vertical: Direction,
        strategy: TargetStrategy
    ) {
        var offset = 0
        if (vertical != Direction.NONE) {
            while (!strategy.reached(tile.x, tile.y + offset, tile.plane, size)) {
                if (collisions.check(tile.x, tile.y + offset, tile.plane, vertical.block())) {
                    break
                }
                offset += vertical.delta.x
                queue.add(vertical)
            }
        }
        if (horizontal != Direction.NONE &&
            !strategy.reached(tile.x, tile.y + offset, tile.plane, size) &&
            !collisions.check(tile.x + vertical.delta.x, tile.y + offset, tile.plane, vertical.block())
        ) {
            horizontal(queue, tile.add(y = offset), size, horizontal, vertical, strategy)
        }
    }

    fun toDirection(delta: Tile) = when {
        delta.x > 0 -> when {
            delta.y > 0 -> Direction.NORTH_EAST
            delta.y < 0 -> Direction.SOUTH_EAST
            else -> Direction.EAST
        }
        delta.x < 0 -> when {
            delta.y > 0 -> Direction.NORTH_WEST
            delta.y < 0 -> Direction.SOUTH_WEST
            else -> Direction.WEST
        }
        else -> when {
            delta.y > 0 -> Direction.NORTH
            delta.y < 0 -> Direction.SOUTH
            else -> Direction.NONE
        }
    }
}
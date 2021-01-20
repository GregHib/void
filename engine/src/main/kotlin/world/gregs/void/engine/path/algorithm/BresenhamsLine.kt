package world.gregs.void.engine.path.algorithm

import org.koin.dsl.module
import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.move.Movement
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.map.collision.CollisionFlag
import world.gregs.void.engine.map.collision.Collisions
import world.gregs.void.engine.map.collision.check
import world.gregs.void.engine.path.PathAlgorithm
import world.gregs.void.engine.path.PathResult
import world.gregs.void.engine.path.TargetStrategy
import world.gregs.void.engine.path.TraversalStrategy
import kotlin.math.abs


@Suppress("USELESS_CAST")
val lineOfSightModule = module {
    single { BresenhamsLine(get()) }
}

/**
 * Checks points along a line between source and target to see if blocked
 */
class BresenhamsLine(
    private val collisions: Collisions
) : PathAlgorithm {

    private fun blocked(x: Int, y: Int, plane: Int, flip: Boolean, flag: Int): Boolean {
        return if (flip) {
            collisions.check(y, x, plane, flag)
        } else {
            collisions.check(x, y, plane, flag)
        }
    }

    override fun find(
        tile: Tile,
        size: Size,
        movement: Movement,
        strategy: TargetStrategy,
        traversal: TraversalStrategy
    ): PathResult {
        return withinSight(tile, strategy.tile)
    }

    /**
     * Checks line of sight in both directions
     */
    fun withinSight(
        tile: Tile,
        other: Tile
    ): PathResult {
        val result = canSee(tile, other)
        if (result is PathResult.Success) {
            val reverse = canSee(other, tile)
            if (reverse !is PathResult.Success) {
                return reverse
            }
        }
        return result
    }

    /**
     * A variation of Bresenham's line algorithm which marches from starting point [tile]
     * alternating axis until reaching a blockage or target [other]
     * @return whether there is nothing blocking between the two points
     */
    private fun canSee(
        tile: Tile,
        other: Tile
    ): PathResult {
        if (tile.plane != other.plane) {
            return PathResult.Failure
        }
        if (tile.x == other.x && tile.y == other.y) {
            return PathResult.Success(tile)
        }

        var dx = other.x - tile.x
        var dy = other.y - tile.y
        var dxAbs = abs(dx)
        val dyAbs = abs(dy)

        val flip = dxAbs <= dyAbs

        var horizontalFlag = if (dx < 0) CollisionFlag.SKY_BLOCK_EAST else CollisionFlag.SKY_BLOCK_WEST
        var verticalFlag = if (dy < 0) CollisionFlag.SKY_BLOCK_NORTH else CollisionFlag.SKY_BLOCK_SOUTH

        if (flip) {
            var temp = dx
            dx = dy
            dy = temp
            dxAbs = dyAbs
            temp = horizontalFlag
            horizontalFlag = verticalFlag
            verticalFlag = temp
        }

        var shifted: Int = shift(if (flip) tile.x else tile.y)
        shifted += shiftedHalfTile
        if (needsRounding(dy)) {
            shifted--
        }

        var position: Int = if (flip) tile.y else tile.x
        val target = if (flip) other.y else other.x
        val plane = tile.plane

        val direction = if (dx < 0) -1 else 1
        val slope = shift(dy) / dxAbs
        while (position != target) {

            position += direction
            val value = revert(shifted)
            if (blocked(position, value, plane, flip, horizontalFlag)) {
                return PathResult.Partial(Tile(position, value, plane))
            }

            shifted += slope
            val next = revert(shifted)
            if (next != value && blocked(position, next, plane, flip, verticalFlag)) {
                return PathResult.Partial(Tile(position, next, plane))
            }
        }

        return PathResult.Success(other)
    }

    /**
     * Shift values to avoid rounding errors
     */
    private fun shift(value: Int) = value shl 16

    private fun revert(value: Int) = value ushr 16

    private fun needsRounding(value: Int) = value < 0

    companion object {
        private const val shiftedHalfTile = 0x8000
    }
}
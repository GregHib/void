package world.gregs.voidps.engine.path.algorithm

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.strategy.PlayerCollision
import world.gregs.voidps.engine.map.collision.strategy.ProjectileCollision
import kotlin.math.abs


@Suppress("USELESS_CAST")
val lineOfSightModule = module {
    single { BresenhamsLine(get(), get()) }
}

/**
 * Checks points along a line between source and target to see if blocked
 */
class BresenhamsLine(
    private val projectile: ProjectileCollision,
    private val collision: PlayerCollision
) {

    fun withinSight(
        tile: Tile,
        target: Tile,
        walls: Boolean = false
    ): Boolean = withinSight(tile.x, tile.y, tile.plane, target.x, target.y, target.plane, walls)

    /**
     * Checks line of sight in both directions
     */
    fun withinSight(
        x: Int,
        y: Int,
        plane: Int,
        targetX: Int,
        targetY: Int,
        targetPlane: Int,
        walls: Boolean = false
    ): Boolean {
        val result = canSee(x, y, plane, targetX, targetY, targetPlane, walls)
        if (result) {
            val reverse = canSee(targetX, targetY, targetPlane, x, y, plane, walls)
            if (!reverse) {
                return reverse
            }
        }
        return result
    }

    /**
     * A variation of Bresenham's line algorithm which marches from starting point [x], [y]
     * alternating axis until reaching a blockage or target [otherX], [otherY]
     * @return true if there is nothing blocking between the two points
     */
    private fun canSee(
        x: Int,
        y: Int,
        plane: Int,
        otherX: Int,
        otherY: Int,
        otherPlane: Int,
        walls: Boolean
    ): Boolean {
        if (plane != otherPlane) {
            return false
        }
        if (x == otherX && y == otherY) {
            return true
        }

        var dx = otherX - x
        var dy = otherY - y
        var dxAbs = abs(dx)
        val dyAbs = abs(dy)

        val flip = dxAbs <= dyAbs

        var horizontal = if (dx < 0) Direction.EAST else Direction.WEST
        var vertical = if (dy < 0) Direction.NORTH else Direction.SOUTH

        if (flip) {
            val temp = dx
            dx = dy
            dy = temp
            dxAbs = dyAbs
            val dir = horizontal
            horizontal = vertical
            vertical = dir
        }

        var shifted: Int = shift(if (flip) x else y)
        shifted += shiftedHalfTile
        if (needsRounding(dy)) {
            shifted--
        }

        var position: Int = if (flip) y else x
        val target = if (flip) otherY else otherX

        val direction = if (dx < 0) -1 else 1
        val slope = shift(dy) / dxAbs
        val strategy = if (walls) collision else projectile
        while (position != target) {
            position += direction
            val value = revert(shifted)
            if (strategy.blocked(if (flip) value else position, if (flip) position else value, plane, horizontal)) {
                return false
            }

            shifted += slope
            val next = revert(shifted)
            if (next != value && strategy.blocked(if (flip) next else position, if (flip) position else next, plane, vertical)) {
                return false
            }
        }

        return true
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
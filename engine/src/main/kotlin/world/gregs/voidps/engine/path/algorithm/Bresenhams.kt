package world.gregs.voidps.engine.path.algorithm

import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import kotlin.math.abs


/**
 * Checks points along a line between source and target to see if blocked
 */
class Bresenhams(
    private val collisions: Collisions
) {

    private fun blocked(x: Int, y: Int, plane: Int, flip: Boolean, flag: Int): Boolean {
        return if (flip) {
            collisions.check(y, x, plane, flag)
        } else {
            collisions.check(x, y, plane, flag)
        }
    }

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
     * A variation of Bresenham's line algorithm which marches from starting point [tile]
     * alternating axis until reaching a blockage or target [other]
     * @return whether there is nothing blocking between the two points
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

        var horizontalFlag = if (walls) {
            if (dx < 0) CollisionFlag.LAND_WALL_EAST else CollisionFlag.LAND_WALL_WEST
        } else {
            CollisionFlag.IGNORED or if (dx < 0) CollisionFlag.SKY_BLOCK_EAST else CollisionFlag.SKY_BLOCK_WEST
        }
        var verticalFlag = if (walls) {
            if (dy < 0) CollisionFlag.LAND_WALL_NORTH else CollisionFlag.LAND_WALL_SOUTH
        } else {
            CollisionFlag.IGNORED or if (dy < 0) CollisionFlag.SKY_BLOCK_NORTH else CollisionFlag.SKY_BLOCK_SOUTH
        }

        if (flip) {
            var temp = dx
            dx = dy
            dy = temp
            dxAbs = dyAbs
            temp = horizontalFlag
            horizontalFlag = verticalFlag
            verticalFlag = temp
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
        while (position != target) {
            position += direction
            val value = revert(shifted)
            if (blocked(position, value, plane, flip, horizontalFlag)) {
                return false
            }

            shifted += slope
            val next = revert(shifted)
            if (next != value && blocked(position, next, plane, flip, verticalFlag)) {
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
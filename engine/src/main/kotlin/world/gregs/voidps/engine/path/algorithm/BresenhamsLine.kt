package world.gregs.voidps.engine.path.algorithm

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.strategy.LandCollision
import world.gregs.voidps.engine.map.collision.strategy.SkyCollision
import kotlin.math.abs


@Suppress("USELESS_CAST")
val lineOfSightModule = module {
    single { BresenhamsLine(get(), get()) }
}

/**
 * Checks points along a line between source and target to see if blocked
 */
class BresenhamsLine(
    private val sky: SkyCollision,
    private val land: LandCollision
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
        val delta = Delta(otherX - x, otherY - y)
        val absX = abs(delta.x)
        val absY = abs(delta.y)
        val flip = absX <= absY
        val direction = delta.toDirection().inverse()
        val horizontal = direction.horizontal()
        val vertical = direction.vertical()
        val strategy = if (walls) land else sky
        return if (flip) {
            isLineFree(strategy, y, x, plane, otherY, delta.y, delta.x, absY, flip, vertical, horizontal)
        } else {
            isLineFree(strategy, x, y, plane, otherX, delta.x, delta.y, absX, flip, horizontal, vertical)
        }
    }

    private fun isLineFree(strategy: CollisionStrategy, x: Int, y: Int, plane: Int, target: Int, dx: Int, dy: Int, abs: Int, flip: Boolean, horizontal: Direction, vertical: Direction): Boolean {
        var shifted: Int = shift(y)
        shifted += shiftedHalfTile
        if (needsRounding(dy)) {
            shifted--
        }

        var position: Int = x
        val direction = if (dx < 0) -1 else 1
        val slope = shift(dy) / abs
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
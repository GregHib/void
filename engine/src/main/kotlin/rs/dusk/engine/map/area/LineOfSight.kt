package rs.dusk.engine.map.area

import org.koin.dsl.module
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.CollisionFlag
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.map.collision.check
import kotlin.math.abs

@Suppress("USELESS_CAST")
val lineOfSightModule = module {
    single { LineOfSight(get()) }
}

class LineOfSight(
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
    fun withinSight(tile: Tile, other: Tile): Boolean {
        return canSee(tile, other) && canSee(other, tile)
    }

    /**
     * A variation of Bresenham's line algorithm which marches from starting point [tile]
     * alternating axis until reaching a blockage or target [other]
     * @return whether there is nothing blocking between the two points
     */
    private fun canSee(tile: Tile, other: Tile): Boolean {
        if (tile.plane != other.plane) {
            return false
        }
        if (tile.x == other.x && tile.y == other.y) {
            return true
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
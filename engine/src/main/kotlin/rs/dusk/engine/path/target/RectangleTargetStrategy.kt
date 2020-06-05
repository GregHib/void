package rs.dusk.engine.path.target

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.model.world.map.collision.flag
import rs.dusk.engine.path.TargetStrategy

/**
 * Checks if within interact range of a rectangle
 * Used for NPCs of differing sizes
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
data class RectangleTargetStrategy(
    private val collisions: Collisions,
    override val tile: Tile,
    override val size: Size = Size.TILE,
    val blockFlag: Int = 0
) : TargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        val srcEndX = currentX + size.width
        val srcEndY = currentY + size.height
        val destEndX = tile.x + size.width
        val destEndY = tile.y + size.height
        if (currentX == destEndX && blockFlag and EAST == 0) {
            val minY = if (tile.y < currentY) currentY else tile.y
            val maxY = if (destEndY <= srcEndY) destEndY else srcEndY
            for (y in minY until maxY) {
                if (!collisions.check(destEndX - 1, y, plane, Direction.EAST.flag())) {
                    return true
                }
            }
        } else if (tile.x == srcEndX && blockFlag and WEST == 0) {
            val minY = if (currentY <= tile.y) tile.y else currentY
            val maxY = if (destEndY <= srcEndY) destEndY else srcEndY
            for (y in minY until maxY) {
                if (!collisions.check(tile.x, y, plane, Direction.WEST.flag())) {
                    return true
                }
            }
        } else if (currentY == destEndY && blockFlag and NORTH == 0) {
            val minX = if (currentX <= tile.x) tile.x else currentX
            val maxX = if (destEndX <= srcEndX) destEndX else srcEndX
            for (x in minX until maxX) {
                if (!collisions.check(x, destEndY - 1, plane, Direction.NORTH.flag())) {
                    return true
                }
            }
        } else if (tile.y == srcEndY && blockFlag and SOUTH == 0) {
            val minX = if (currentX > tile.x) currentX else tile.x
            val maxX = if (srcEndX >= destEndX) destEndX else srcEndX
            for (x in minX until maxX) {
                if (!collisions.check(x, tile.y, plane, Direction.SOUTH.flag())) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private const val NORTH = 0x1
        private const val EAST = 0x2
        private const val SOUTH = 0x4
        private const val WEST = 0x8
    }
}
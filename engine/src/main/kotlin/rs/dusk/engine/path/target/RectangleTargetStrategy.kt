package rs.dusk.engine.path.target

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.model.world.map.collision.flag
import rs.dusk.engine.path.Target
import rs.dusk.engine.path.TargetStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
class RectangleTargetStrategy(private val collision: Collisions) : TargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size, target: Target): Boolean {
        val srcEndX = currentX + size.width
        val srcEndY = currentY + size.height
        val destEndX = target.tile.x + target.size.width
        val destEndY = target.tile.y + target.size.height
        val accessBlockFlag = (target as? Location)?.def?.blockFlag ?: 0
        if (currentX == destEndX && accessBlockFlag and EAST == 0) {
            val minY = if (target.tile.y < currentY) currentY else target.tile.y
            val maxY = if (destEndY <= srcEndY) destEndY else srcEndY
            for (y in minY until maxY) {
                if (!collision.check(destEndX - 1, y, plane, Direction.EAST.flag())) {
                    return true
                }
            }
        } else if (target.tile.x == srcEndX && accessBlockFlag and WEST == 0) {
            val minY = if (currentY <= target.tile.y) target.tile.y else currentY
            val maxY = if (destEndY <= srcEndY) destEndY else srcEndY
            for (y in minY until maxY) {
                if (!collision.check(target.tile.x, y, plane, Direction.WEST.flag())) {
                    return true
                }
            }
        } else if (currentY == destEndY && accessBlockFlag and NORTH == 0) {
            val minX = if (currentX <= target.tile.x) target.tile.x else currentX
            val maxX = if (destEndX <= srcEndX) destEndX else srcEndX
            for (x in minX until maxX) {
                if (!collision.check(x, destEndY - 1, plane, Direction.NORTH.flag())) {
                    return true
                }
            }
        } else if (target.tile.y == srcEndY && accessBlockFlag and SOUTH == 0) {
            val minX = if (currentX > target.tile.x) currentX else target.tile.x
            val maxX = if (srcEndX >= destEndX) destEndX else srcEndX
            for (x in minX until maxX) {
                if (!collision.check(x, target.tile.y, plane, Direction.SOUTH.flag())) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private val NORTH = 0x1
        private val EAST = 0x2
        private val SOUTH = 0x4
        private val WEST = 0x8
    }
}
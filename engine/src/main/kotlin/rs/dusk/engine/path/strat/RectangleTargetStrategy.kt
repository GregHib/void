package rs.dusk.engine.path.strat

import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.Character
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.map.collision.check
import rs.dusk.engine.map.collision.flag
import rs.dusk.engine.path.TargetStrategy
import kotlin.math.max
import kotlin.math.min

/**
 * Checks if within interact range of a rectangle
 * Used for NPCs of differing sizes
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
data class RectangleTargetStrategy(
    private val collisions: Collisions,
    private val entity: Entity,
    val blockFlag: Int = 0
) : TargetStrategy {

    override val tile: Tile
        get() = entity.tile

    override val size: Size
        get() = when (entity) {
            is GameObject -> entity.size
            is Character -> entity.size
            else -> Size.TILE
        }

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        val srcEndX = currentX + size.width
        val srcEndY = currentY + size.height
        val destEndX = tile.x + this.size.width
        val destEndY = tile.y + this.size.height
        if (currentX == destEndX && blockFlag and EAST == 0) {
            for (y in max(currentY, tile.y) until min(destEndY, srcEndY)) {
                if (!collisions.check(destEndX - 1, y, plane, Direction.EAST.flag())) {
                    return true
                }
            }
        } else if (tile.x == srcEndX && blockFlag and WEST == 0) {
            for (y in max(currentY, tile.y) until min(destEndY, srcEndY)) {
                if (!collisions.check(tile.x, y, plane, Direction.WEST.flag())) {
                    return true
                }
            }
        } else if (currentY == destEndY && blockFlag and NORTH == 0) {
            for (x in max(currentX, tile.x) until min(destEndX, srcEndX)) {
                if (!collisions.check(x, destEndY - 1, plane, Direction.NORTH.flag())) {
                    return true
                }
            }
        } else if (tile.y == srcEndY && blockFlag and SOUTH == 0) {
            for (x in max(currentX, tile.x) until min(destEndX, srcEndX)) {
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
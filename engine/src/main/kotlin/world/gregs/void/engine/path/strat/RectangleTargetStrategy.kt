package world.gregs.void.engine.path.strat

import world.gregs.void.engine.entity.Direction
import world.gregs.void.engine.entity.Entity
import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.Character
import world.gregs.void.engine.entity.obj.GameObject
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.map.collision.Collisions
import world.gregs.void.engine.map.collision.check
import world.gregs.void.engine.map.collision.flag
import world.gregs.void.engine.path.TargetStrategy
import kotlin.math.max
import kotlin.math.min

/**
 * Checks if within interact range of a rectangle
 * Used for NPCs of differing sizes
 * @author GregHib <greg@gregs.world>
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
                if (free(destEndX - 1, y, plane, Direction.EAST)) {
                    return true
                }
            }
        } else if (tile.x == srcEndX && blockFlag and WEST == 0) {
            for (y in max(currentY, tile.y) until min(destEndY, srcEndY)) {
                if (free(tile.x, y, plane, Direction.WEST)) {
                    return true
                }
            }
        } else if (currentY == destEndY && blockFlag and NORTH == 0) {
            for (x in max(currentX, tile.x) until min(destEndX, srcEndX)) {
                if (free(x, destEndY - 1, plane, Direction.NORTH)) {
                    return true
                }
            }
        } else if (tile.y == srcEndY && blockFlag and SOUTH == 0) {
            for (x in max(currentX, tile.x) until min(destEndX, srcEndX)) {
                if (free(x, tile.y, plane, Direction.SOUTH)) {
                    return true
                }
            }
        }
        return false
    }

    private fun free(x: Int, y: Int, z: Int, direction: Direction): Boolean {
        return !collisions.check(x, y, z, direction.flag())
    }

    companion object {
        private const val NORTH = 0x1
        private const val EAST = 0x2
        private const val SOUTH = 0x4
        private const val WEST = 0x8
    }
}
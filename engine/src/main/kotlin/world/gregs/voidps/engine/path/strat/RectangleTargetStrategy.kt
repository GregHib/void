package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.flag
import kotlin.math.max
import kotlin.math.min

/**
 * Checks if within interact range of a rectangle
 * Used for NPCs of differing sizes
 */
class RectangleTargetStrategy(
    private val collisions: Collisions,
    private val entity: Entity,
    private val allowUnder: Boolean,
    val blockFlag: Int = 0
) : TileTargetStrategy {

    override val tile: Tile
        get() = entity.tile

    override val size: Size
        get() = when (entity) {
            is GameObject -> entity.size
            is Character -> entity.size
            else -> Size.ONE
        }

    override fun reached(current: Tile, size: Size): Boolean {
        if (!allowUnder && Overlap.isUnder(tile, this.size, current, size)) {
            return false
        }
        val srcEndX = current.x + size.width
        val srcEndY = current.y + size.height
        val destEndX = tile.x + this.size.width
        val destEndY = tile.y + this.size.height
        if (current.x == destEndX && blockFlag and EAST == 0) {
            for (y in max(current.y, tile.y) until min(destEndY, srcEndY)) {
                if (free(destEndX - 1, y, current.plane, Direction.EAST)) {
                    return true
                }
            }
        } else if (tile.x == srcEndX && blockFlag and WEST == 0) {
            for (y in max(current.y, tile.y) until min(destEndY, srcEndY)) {
                if (free(tile.x, y, current.plane, Direction.WEST)) {
                    return true
                }
            }
        } else if (current.y == destEndY && blockFlag and NORTH == 0) {
            for (x in max(current.x, tile.x) until min(destEndX, srcEndX)) {
                if (free(x, destEndY - 1, current.plane, Direction.NORTH)) {
                    return true
                }
            }
        } else if (tile.y == srcEndY && blockFlag and SOUTH == 0) {
            for (x in max(current.x, tile.x) until min(destEndX, srcEndX)) {
                if (free(x, tile.y, current.plane, Direction.SOUTH)) {
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
package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.map.collision.flag
import world.gregs.voidps.engine.path.TargetStrategy

/**
 * Checks if within interact range of a targeted decoration
 * @author GregHib <greg@gregs.world>
 * @since May 18, 2020
 */
data class DecorationTargetStrategy(
    private val collisions: Collisions,
    private val gameObject: GameObject
) : TargetStrategy {

    override val tile: Tile
        get() = gameObject.tile

    override val size: Size
        get() = gameObject.size

    val rotation: Int
        get() = gameObject.rotation

    val type: Int
        get() = gameObject.type

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        val sizeXY = size.width
        var rotation = rotation
        if (sizeXY == 1) {
            if (tile.x == currentX && currentY == tile.y) {
                return true
            }
        } else if (currentX <= tile.x && sizeXY + currentX - 1 >= tile.x && tile.y <= sizeXY + tile.y - 1) {
            return true
        }
        if (sizeXY == 1) {
            if (type == 6 || type == 7) {
                if (type == 7) {
                    rotation = rotation + 2 and 0x3
                }
                if (rotation == 0) {
                    if (currentX == tile.x + 1 && currentY == tile.y && !collisions.check(currentX, currentY, plane, Direction.WEST.flag())) {
                        return true
                    }
                    if (tile.x == currentX && currentY == tile.y - 1 && !collisions.check(currentX, currentY, plane, Direction.NORTH.flag())) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (currentX == tile.x - 1 && currentY == tile.y && !collisions.check(currentX, currentY, plane, Direction.EAST.flag())) {
                        return true
                    }
                    if (tile.x == currentX && currentY == tile.y - 1 && !collisions.check(currentX, currentY, plane, Direction.NORTH.flag())) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (currentX == tile.x - 1 && tile.y == currentY && !collisions.check(currentX, currentY, plane, Direction.EAST.flag())) {
                        return true
                    }
                    if (tile.x == currentX && currentY == tile.y + 1 && !collisions.check(currentX, currentY, plane, Direction.SOUTH.flag())) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (tile.x + 1 == currentX && currentY == tile.y && !collisions.check(currentX, currentY, plane, Direction.WEST.flag())) {
                        return true
                    }
                    if (tile.x == currentX && currentY == tile.y + 1 && !collisions.check(currentX, currentY, plane, Direction.SOUTH.flag())) {
                        return true
                    }
                }
            }
            if (type == 8) {
                if (tile.x == currentX && currentY == tile.y + 1 && !collisions.check(currentX, currentY, plane, Direction.SOUTH.flag())) {
                    return true
                }
                if (currentX == tile.x && tile.y - 1 == currentY && !collisions.check(currentX, currentY, plane, Direction.NORTH.flag())) {
                    return true
                }
                return if (currentX == tile.x - 1 && tile.y == currentY && !collisions.check(currentX, currentY, plane, Direction.EAST.flag())) {
                    true
                } else tile.x + 1 == currentX && tile.y == currentY && !collisions.check(currentX, currentY, plane, Direction.WEST.flag())
            }
        } else {
            val sizeX = sizeXY + currentX - 1
            val sizeY = currentY + sizeXY - 1
            if (type == 6 || type == 7) {
                if (type == 7) {
                    rotation = rotation + 2 and 0x3
                }
                if (rotation == 0) {
                    if (currentX == tile.x + 1 && currentY <= tile.y && tile.y <= sizeY && !collisions.check(currentX, tile.y, plane, Direction.WEST.flag())) {
                        return true
                    }
                    if (tile.x in currentX..sizeX && currentY == tile.y - sizeXY && !collisions.check(tile.x, sizeY, plane, Direction.NORTH.flag())) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (currentX == tile.x - sizeXY && tile.y >= currentY && tile.y <= sizeY && !collisions.check(sizeX, tile.y, plane, Direction.EAST.flag())) {
                        return true
                    }
                    if (tile.x in currentX..sizeX && currentY == tile.y - sizeXY && !collisions.check(tile.x, sizeY, plane, Direction.NORTH.flag())) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (tile.x - sizeXY == currentX && tile.y >= currentY && tile.y <= sizeY && !collisions.check(sizeX, tile.y, plane, Direction.EAST.flag())) {
                        return true
                    }
                    if (tile.x in currentX..sizeX && tile.y + 1 == currentY && !collisions.check(tile.x, currentY, plane, Direction.SOUTH.flag())) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (currentX == tile.x + 1 && currentY <= tile.y && tile.y <= sizeY && !collisions.check(currentX, tile.y, plane, Direction.WEST.flag())) {
                        return true
                    }
                    if (tile.x in currentX..sizeX && currentY == tile.y + 1 && !collisions.check(tile.x, currentY, plane, Direction.SOUTH.flag())) {
                        return true
                    }
                }
            }
            if (type == 8) {
                if (tile.x in currentX..sizeX && currentY == tile.y + 1 && !collisions.check(tile.x, currentY, plane, Direction.SOUTH.flag())) {
                    return true
                }
                if (tile.x in currentX..sizeX && currentY == tile.y - sizeXY && !collisions.check(tile.x, sizeY, plane, Direction.NORTH.flag())) {
                    return true
                }
                return if (currentX == tile.x - sizeXY && currentY <= tile.y && tile.y <= sizeY && !collisions.check(sizeX, tile.y, plane, Direction.EAST.flag())) {
                    true
                } else currentX == tile.x + 1 && currentY <= tile.y && tile.y <= sizeY && !collisions.check(currentX, tile.y, plane, Direction.WEST.flag())
            }
        }
        return false
    }
}
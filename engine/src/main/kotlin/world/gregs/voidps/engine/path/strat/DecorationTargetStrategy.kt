package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.flag

/**
 * Checks if within interact range of a targeted decoration
 */
class DecorationTargetStrategy(
    private val collisions: Collisions,
    private val gameObject: GameObject
) : TileTargetStrategy {

    override val tile: Tile
        get() = gameObject.tile

    override val size: Size
        get() = gameObject.size

    val rotation: Int
        get() = gameObject.rotation

    val type: Int
        get() = gameObject.type

    override fun reached(current: Tile, size: Size): Boolean {
        val sizeXY = size.width
        var rotation = rotation
        if (sizeXY == 1) {
            if (current.x == tile.x && current.y == tile.y) {
                return true
            }
        } else if (current.x <= tile.x && sizeXY + current.x - 1 >= tile.x && tile.y <= sizeXY + tile.y - 1) {
            return true
        }
        if (sizeXY == 1) {
            if (type == 6 || type == 7) {
                if (type == 7) {
                    rotation = rotation + 2 and 0x3
                }
                if (rotation == 0) {
                    if (current.x == tile.x + 1 && current.y == tile.y && !collisions.check(current, Direction.WEST.flag())) {
                        return true
                    }
                    if (current.x == tile.x && current.y == tile.y - 1 && !collisions.check(current, Direction.NORTH.flag())) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (current.x == tile.x - 1 && current.y == tile.y && !collisions.check(current, Direction.EAST.flag())) {
                        return true
                    }
                    if (current.x == tile.x && current.y == tile.y - 1 && !collisions.check(current, Direction.NORTH.flag())) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (current.x == tile.x - 1 && current.y == tile.y && !collisions.check(current, Direction.EAST.flag())) {
                        return true
                    }
                    if (current.x == tile.x && current.y == tile.y + 1 && !collisions.check(current, Direction.SOUTH.flag())) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (current.x == tile.x + 1 && current.y == tile.y && !collisions.check(current, Direction.WEST.flag())) {
                        return true
                    }
                    if (current.x == tile.x && current.y == tile.y + 1 && !collisions.check(current, Direction.SOUTH.flag())) {
                        return true
                    }
                }
            }
            if (type == 8) {
                if (current.x == tile.x && current.y == tile.y + 1 && !collisions.check(current, Direction.SOUTH.flag())) {
                    return true
                }
                if (current.x == tile.x && current.y == tile.y - 1 && !collisions.check(current, Direction.NORTH.flag())) {
                    return true
                }
                return if (current.x == tile.x - 1 && current.y == tile.y && !collisions.check(current, Direction.EAST.flag())) {
                    true
                } else current.x == tile.x + 1 && current.y == tile.y && !collisions.check(current, Direction.WEST.flag())
            }
        } else {
            val sizeX = sizeXY + current.x - 1
            val sizeY = current.y + sizeXY - 1
            if (type == 6 || type == 7) {
                if (type == 7) {
                    rotation = rotation + 2 and 0x3
                }
                if (rotation == 0) {
                    if (current.x == tile.x + 1 && current.y <= tile.y && tile.y <= sizeY && !collisions.check(current.x, tile.y, current.plane, Direction.WEST.flag())) {
                        return true
                    }
                    if (tile.x in current.x..sizeX && current.y == tile.y - sizeXY && !collisions.check(tile.x, sizeY, current.plane, Direction.NORTH.flag())) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (current.x == tile.x - sizeXY && current.y <= tile.y && tile.y <= sizeY && !collisions.check(sizeX, tile.y, current.plane, Direction.EAST.flag())) {
                        return true
                    }
                    if (tile.x in current.x..sizeX && current.y == tile.y - sizeXY && !collisions.check(tile.x, sizeY, current.plane, Direction.NORTH.flag())) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (current.x == tile.x - sizeXY && current.y <= tile.y && tile.y <= sizeY && !collisions.check(sizeX, tile.y, current.plane, Direction.EAST.flag())) {
                        return true
                    }
                    if (tile.x in current.x..sizeX && tile.y + 1 == current.y && !collisions.check(tile.x, current.y, current.plane, Direction.SOUTH.flag())) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (current.x == tile.x + 1 && current.y <= tile.y && sizeY >= tile.y && !collisions.check(current.x, tile.y, current.plane, Direction.WEST.flag())) {
                        return true
                    }
                    if (tile.x in current.x..sizeX && current.y == tile.y + 1 && !collisions.check(tile.x, current.y, current.plane, Direction.SOUTH.flag())) {
                        return true
                    }
                }
            }
            if (type == 8) {
                if (tile.x in current.x..sizeX && current.y == tile.y + 1 && !collisions.check(tile.x, current.y, current.plane, Direction.SOUTH.flag())) {
                    return true
                }
                if (tile.x in current.x..sizeX && current.y == tile.y - sizeXY && !collisions.check(tile.x, sizeY, current.plane, Direction.NORTH.flag())) {
                    return true
                }
                return if (current.x == tile.x - sizeXY && current.y <= tile.y && tile.y <= sizeY && !collisions.check(sizeX, tile.y, current.plane, Direction.EAST.flag())) {
                    true
                } else current.x == tile.x + 1 && current.y <= tile.y && tile.y <= sizeY && !collisions.check(current.x, tile.y, current.plane, Direction.WEST.flag())
            }
        }
        return false
    }
}
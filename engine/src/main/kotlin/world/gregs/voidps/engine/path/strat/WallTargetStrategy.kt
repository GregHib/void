package world.gregs.voidps.engine.path.strat

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.flag

/**
 * Checks if within interact range of a wall
 * e.g. On the correct side to view a painting on a wall
 */
class WallTargetStrategy(
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
        // Check if under
        if (sizeXY == 1 && current.x == tile.x && current.y == tile.y) {
            return true
        } else if (sizeXY != 1 && tile.x >= current.x && tile.x <= current.x + sizeXY - 1 && tile.y <= tile.y + sizeXY - 1) {
            return true
        }

        if (sizeXY == 1) {
            if (type == 0) {
                var direction = Direction.cardinal[rotation + 3 and 0x3]
                if (current.x == tile.x + direction.delta.x && current.y == tile.y + direction.delta.y) {
                    return true
                }
                direction = Direction.cardinal[rotation and 0x3]
                if (current.x == tile.x - direction.delta.x && current.y == tile.y - direction.delta.y && !collisions.check(current, direction.wall())) {
                    return true
                }
                val inverse = direction.inverse()
                if (current.x == tile.x - inverse.delta.x && current.y == tile.y - inverse.delta.y && !collisions.check(current, inverse.wall())) {
                    return true
                }
            }
            if (type == 2) {
                val direction = Direction.ordinal[rotation and 0x3]
                val horizontal = direction.horizontal()
                if (current.x == tile.x + horizontal.delta.x && current.y == tile.y) {
                    return true
                }
                val vertical = direction.vertical()
                if (current.x == tile.x && current.y == tile.y + vertical.delta.y) {
                    return true
                }
                if (current.x == tile.x - horizontal.delta.x && current.y == tile.y && !collisions.check(current, horizontal.wall())) {
                    return true
                }
                if (current.x == tile.x && current.y == tile.y - vertical.delta.y && !collisions.check(current, vertical.wall())) {
                    return true
                }
            }
            if (type == 9) {
                Direction.ordinal.forEach { direction ->
                    if (current.x == tile.x - direction.delta.x && current.y == tile.y - direction.delta.y && !collisions.check(current, direction.flag())) {
                        return true
                    }
                }
                return false
            }
        } else {
            val sizeX = sizeXY + current.x - 1
            val sizeY = sizeXY + current.y - 1
            if (type == 0) {
                if (rotation == 0) {
                    if (current.x == tile.x - sizeXY && tile.y >= current.y && tile.y <= sizeY) {
                        return true
                    }
                    if (checkVertical(current, sizeX, sizeY, sizeXY)) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (current.y == tile.y + 1 && tile.x >= current.x && tile.x <= sizeX) {
                        return true
                    }
                    if (checkHorizontal(current, sizeX, sizeY, sizeXY)) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (current.x == tile.x + 1 && tile.y >= current.y && tile.y <= sizeY) {
                        return true
                    }
                    if (checkVertical(current, sizeX, sizeY, sizeXY)) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (current.y == tile.y - sizeXY && current.x <= tile.x && sizeX >= tile.x) {
                        return true
                    }
                    if (checkHorizontal(current, sizeX, sizeY, sizeXY)) {
                        return true
                    }
                }
            }
            if (type == 2) {
                if (rotation == 0) {
                    if (current.x == tile.x - sizeXY && tile.y >= current.y && sizeY >= tile.y) {
                        return true
                    }
                    if (current.y == tile.y + 1 && tile.x in current.x..sizeX) {
                        return true
                    }
                    if (current.x == tile.x + 1 && current.y <= tile.y && sizeY >= tile.y && !collisions.check(current.x, tile.y, current.plane, Direction.WEST.wall())) {
                        return true
                    }
                    if (current.y == tile.y - sizeXY && tile.x in current.x..sizeX && !collisions.check(tile.x, sizeY, current.plane, Direction.NORTH.wall())) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (current.x == tile.x - sizeXY && current.y <= tile.y && sizeY >= tile.y && !collisions.check(sizeX, tile.y, current.plane, Direction.EAST.wall())) {
                        return true
                    }
                    if (current.y == tile.y + 1 && tile.x in current.x..sizeX) {
                        return true
                    }
                    if (current.x == tile.x + 1 && current.y <= tile.y && sizeY >= tile.y) {
                        return true
                    }
                    if (current.y == tile.y - sizeXY && tile.x in current.x..sizeX && !collisions.check(tile.x, sizeY, current.plane, Direction.NORTH.wall())) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (current.x == tile.x - sizeXY && current.y <= tile.y && tile.y <= sizeY && !collisions.check(sizeX, tile.y, current.plane, Direction.EAST.wall())) {
                        return true
                    }
                    if (current.y == tile.y + 1 && tile.x in current.x..sizeX && !collisions.check(tile.x, current.y, current.plane, Direction.SOUTH.wall())) {
                        return true
                    }
                    if (current.x == tile.x + 1 && current.y <= tile.y && sizeY >= tile.y) {
                        return true
                    }
                    if (current.y == tile.y - sizeXY && tile.x in current.x..sizeX) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (current.x == tile.x - sizeXY && current.y <= tile.y && sizeY >= tile.y) {
                        return true
                    }
                    if (current.y == tile.y + 1 && tile.x in current.x..sizeX && !collisions.check(tile.x, current.y, current.plane, Direction.SOUTH.wall())) {
                        return true
                    }
                    if (current.x == tile.x + 1 && current.y <= tile.y && tile.y <= sizeY && !collisions.check(current.x, tile.y, current.plane, Direction.WEST.wall())) {
                        return true
                    }
                    if (current.y == tile.y - sizeXY && tile.x in current.x..sizeX) {
                        return true
                    }
                }
            }
            if (type == 9) {
                if (tile.x in current.x..sizeX && current.y == tile.y + 1 && !collisions.check(tile.x, current.y, current.plane, Direction.SOUTH.wall())) {
                    return true
                }
                if (tile.x in current.x..sizeX && current.y == tile.y - sizeXY && !collisions.check(tile.x, sizeY, current.plane, Direction.NORTH.wall())) {
                    return true
                }
                return if (current.x == tile.x - sizeXY && current.y <= tile.y && sizeY >= tile.y && !collisions.check(sizeX, tile.y, current.plane, Direction.EAST.wall())) {
                    true
                } else current.x == tile.x + 1 && current.y <= tile.y && sizeY >= tile.y && !collisions.check(current.x, tile.y, current.plane, Direction.WEST.wall())
            }
        }
        return false
    }

    private fun checkVertical(current: Tile, sizeX: Int, sizeY: Int, sizeXY: Int): Boolean {
        if (current.y == tile.y + 1 && tile.x in current.x..sizeX && !collisions.check(tile.x, current.y, current.plane, Direction.SOUTH.wall())) {
            return true
        }
        if (current.y == tile.y - sizeXY && tile.x in current.x..sizeX && !collisions.check(tile.x, sizeY, current.plane, Direction.NORTH.wall())) {
            return true
        }
        return false
    }

    private fun checkHorizontal(current: Tile, sizeX: Int, sizeY: Int, sizeXY: Int): Boolean {
        if (current.x == tile.x - sizeXY && tile.y >= current.y && tile.y <= sizeY && !collisions.check(sizeX, tile.y, current.plane, Direction.EAST.wall())) {
            return true
        }
        if (current.x == tile.x + 1 && tile.y >= current.y && tile.y <= sizeY && !collisions.check(current.x, tile.y, current.plane, Direction.WEST.wall())) {
            return true
        }
        return false
    }

    companion object {
        private fun Direction.wall() =
            flag() or CollisionFlag.BLOCKED
    }
}
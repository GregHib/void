package rs.dusk.engine.path.target

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.CollisionFlag
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.model.world.map.collision.flag
import rs.dusk.engine.path.TargetStrategy

/**
 * Checks if within interact range of a wall
 * e.g On the correct side to view a painting on a wall
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
data class WallTargetStrategy(
    private val collisions: Collisions,
    override val tile: Tile,
    override val size: Size = Size.TILE,
    val rotation: Int,
    val type: Int
) : TargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size): Boolean {
        val sizeXY = size.width
        // Check if under
        if (sizeXY == 1 && currentX == tile.x && currentY == tile.y) {
            return true
        } else if (sizeXY != 1 && tile.x >= currentX && tile.x <= currentX + sizeXY - 1 && tile.y <= tile.y + sizeXY - 1) {
            return true
        }

        if (sizeXY == 1) {
            if (type == 0) {
                var direction = Direction.cardinal[rotation + 3 and 0x3]
                if (currentX == tile.x + direction.delta.x && currentY == tile.y + direction.delta.y) {
                    return true
                }
                direction = Direction.cardinal[rotation and 0x3]
                if (currentX == tile.x - direction.delta.x && currentY == tile.y - direction.delta.y && !collisions.check(
                        currentX,
                        currentY,
                        plane,
                        direction.wall()
                    )
                ) {
                    return true
                }
                val inverse = direction.inverse()
                if (currentX == tile.x - inverse.delta.x && currentY == tile.y - inverse.delta.y && !collisions.check(
                        currentX,
                        currentY,
                        plane,
                        inverse.wall()
                    )
                ) {
                    return true
                }
            }
            if (type == 2) {
                val direction = Direction.ordinal[rotation and 0x3]
                val horizontal = direction.horizontal()
                if (currentX == tile.x + horizontal.delta.x && currentY == tile.y) {
                    return true
                }
                val vertical = direction.vertical()
                if (currentX == tile.x && currentY == tile.y + vertical.delta.y) {
                    return true
                }
                if (currentX == tile.x - horizontal.delta.x && currentY == tile.y && !collisions.check(
                        currentX,
                        currentY,
                        plane,
                        horizontal.wall()
                    )
                ) {
                    return true
                }
                if (currentX == tile.x && currentY == tile.y - vertical.delta.y && !collisions.check(
                        currentX,
                        currentY,
                        plane,
                        vertical.wall()
                    )
                ) {
                    return true
                }
            }
            if (type == 9) {
                Direction.ordinal.forEach { direction ->
                    if (currentX == tile.x - direction.delta.x && currentY == tile.y - direction.delta.y && !collisions.check(
                            currentX,
                            currentY,
                            plane,
                            direction.flag()
                        )
                    ) {
                        return true
                    }
                }
                return false
            }
        } else {
            val sizeX = sizeXY + currentX - 1
            val sizeY = sizeXY + currentY - 1
            if (type == 0) {
                if (rotation == 0) {
                    if (currentX == tile.x - sizeXY && tile.y >= currentY && tile.y <= sizeY) {
                        return true
                    }
                    if (currentY == tile.y + 1 && tile.x in currentX..sizeX && !collisions.check(
                            tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == tile.y - sizeXY && tile.x in currentX..sizeX && !collisions.check(
                            tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.wall()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (currentY == tile.y + 1 && tile.x >= currentX && tile.x <= sizeX) {
                        return true
                    }
                    if (currentX == tile.x - sizeXY && tile.y >= currentY && tile.y <= sizeY && !collisions.check(
                            sizeX,
                            tile.y,
                            plane,
                            Direction.EAST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentX == tile.x + 1 && tile.y >= currentY && tile.y <= sizeY && !collisions.check(
                            currentX,
                            tile.y,
                            plane,
                            Direction.WEST.wall()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (currentX == tile.x + 1 && tile.y >= currentY && tile.y <= sizeY) {
                        return true
                    }
                    if (currentY == tile.y + 1 && tile.x in currentX..sizeX && !collisions.check(
                            tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == tile.y - sizeXY && tile.x in currentX..sizeX && !collisions.check(
                            tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.wall()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (currentY == tile.y - sizeXY && currentX <= tile.x && sizeX >= tile.x) {
                        return true
                    }
                    if (currentX == tile.x - sizeXY && tile.y >= currentY && sizeY >= tile.y && !collisions.check(
                            sizeX,
                            tile.y,
                            plane,
                            Direction.EAST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentX == tile.x + 1 && currentY <= tile.y && sizeY >= tile.y && !collisions.check(
                            currentX,
                            tile.y,
                            plane,
                            Direction.WEST.wall()
                        )
                    ) {
                        return true
                    }
                }
            }
            if (type == 2) {
                if (rotation == 0) {
                    if (currentX == tile.x - sizeXY && tile.y >= currentY && sizeY >= tile.y) {
                        return true
                    }
                    if (currentY == tile.y + 1 && tile.x in currentX..sizeX) {
                        return true
                    }
                    if (currentX == tile.x + 1 && currentY <= tile.y && sizeY >= tile.y && !collisions.check(
                            currentX,
                            tile.y,
                            plane,
                            Direction.WEST.wall()
                        )
                    ) {
                        return true
                    }
                    if (tile.y - sizeXY == currentY && tile.x in currentX..sizeX && !collisions.check(
                            tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.wall()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (currentX == tile.x - sizeXY && currentY <= tile.y && sizeY >= tile.y && !collisions.check(
                            sizeX,
                            tile.y,
                            plane,
                            Direction.EAST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == tile.y + 1 && tile.x in currentX..sizeX) {
                        return true
                    }
                    if (currentX == tile.x + 1 && currentY <= tile.y && sizeY >= tile.y) {
                        return true
                    }
                    if (currentY == tile.y - sizeXY && tile.x in currentX..sizeX && !collisions.check(
                            tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.wall()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (currentX == tile.x - sizeXY && tile.y >= currentY && tile.y <= sizeY && !collisions.check(
                            sizeX,
                            tile.y,
                            plane,
                            Direction.EAST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == tile.y + 1 && tile.x in currentX..sizeX && !collisions.check(
                            tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentX == tile.x + 1 && currentY <= tile.y && sizeY >= tile.y) {
                        return true
                    }
                    if (currentY == tile.y - sizeXY && tile.x in currentX..sizeX) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (currentX == tile.x - sizeXY && currentY <= tile.y && sizeY >= tile.y) {
                        return true
                    }
                    if (currentY == tile.y + 1 && tile.x in currentX..sizeX && !collisions.check(
                            tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentX == tile.x + 1 && currentY <= tile.y && tile.y <= sizeY && !collisions.check(
                            currentX,
                            tile.y,
                            plane,
                            Direction.WEST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == tile.y - sizeXY && tile.x in currentX..sizeX) {
                        return true
                    }
                }
            }
            if (type == 9) {
                if (tile.x in currentX..sizeX && currentY == tile.y + 1 && !collisions.check(
                        tile.x,
                        currentY,
                        plane,
                        Direction.SOUTH.wall()
                    )
                ) {
                    return true
                }
                if (tile.x in currentX..sizeX && currentY == tile.y - sizeXY && !collisions.check(
                        tile.x,
                        sizeY,
                        plane,
                        Direction.NORTH.wall()
                    )
                ) {
                    return true
                }
                return if (currentX == tile.x - sizeXY && currentY <= tile.y && sizeY >= tile.y && !collisions.check(
                        sizeX,
                        tile.y,
                        plane,
                        Direction.EAST.wall()
                    )
                ) {
                    true
                } else currentX == tile.x + 1 && currentY <= tile.y && sizeY >= tile.y && !collisions.check(
                    currentX,
                    tile.y,
                    plane,
                    Direction.WEST.wall()
                )
            }
        }
        return false
    }

    companion object {
        fun Direction.wall() =
            flag() or CollisionFlag.WALL or CollisionFlag.BLOCKED
    }
}
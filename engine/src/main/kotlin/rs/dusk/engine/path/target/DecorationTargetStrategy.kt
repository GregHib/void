package rs.dusk.engine.path.target

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.model.world.map.collision.flag
import rs.dusk.engine.path.TargetStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
data class DecorationTargetStrategy(
    private val collision: Collisions,
    override val tile: Tile,
    override val size: Size,
    val rotation: Int,
    val type: Int
) : TargetStrategy {

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
                    if (currentX == tile.x + 1 && currentY == tile.y && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.WEST.flag()
                        )
                    ) {
                        return true
                    }
                    if (tile.x == currentX && currentY == tile.y - 1 && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.NORTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (currentX == tile.x - 1 && currentY == tile.y && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.EAST.flag()
                        )
                    ) {
                        return true
                    }
                    if (tile.x == currentX && currentY == tile.y - 1 && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.NORTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (currentX == tile.x - 1 && tile.y == currentY && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.EAST.flag()
                        )
                    ) {
                        return true
                    }
                    if (tile.x == currentX && currentY == tile.y + 1 && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.SOUTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (tile.x + 1 == currentX && currentY == tile.y && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.WEST.flag()
                        )
                    ) {
                        return true
                    }
                    if (tile.x == currentX && currentY == tile.y + 1 && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.SOUTH.flag()
                        )
                    ) {
                        return true
                    }
                }
            }
            if (type == 8) {
                if (tile.x == currentX && currentY == tile.y + 1 && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        Direction.SOUTH.flag()
                    )
                ) {
                    return true
                }
                if (currentX == tile.x && tile.y - 1 == currentY && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        Direction.NORTH.flag()
                    )
                ) {
                    return true
                }
                return if (currentX == tile.x - 1 && tile.y == currentY && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        Direction.EAST.flag()
                    )
                ) {
                    true
                } else tile.x + 1 == currentX && tile.y == currentY && !collision.check(
                    currentX,
                    currentY,
                    plane,
                    Direction.WEST.flag()
                )
            }
        } else {
            val sizeX = sizeXY + currentX - 1
            val sizeY = currentY + sizeXY - 1
            if (type == 6 || type == 7) {
                if (type == 7) {
                    rotation = rotation + 2 and 0x3
                }
                if (rotation == 0) {
                    if (currentX == tile.x + 1 && currentY <= tile.y && tile.y <= sizeY && !collision.check(
                            currentX,
                            tile.y,
                            plane,
                            Direction.WEST.flag()
                        )
                    ) {
                        return true
                    }
                    if (tile.x in currentX..sizeX && currentY == tile.y - sizeXY && !collision.check(
                            tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (currentX == tile.x - sizeXY && tile.y >= currentY && tile.y <= sizeY && !collision.check(
                            sizeX,
                            tile.y,
                            plane,
                            Direction.EAST.flag()
                        )
                    ) {
                        return true
                    }
                    if (tile.x in currentX..sizeX && currentY == tile.y - sizeXY && !collision.check(
                            tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (tile.x - sizeXY == currentX && tile.y >= currentY && tile.y <= sizeY && !collision.check(
                            sizeX,
                            tile.y,
                            plane,
                            Direction.EAST.flag()
                        )
                    ) {
                        return true
                    }
                    if (tile.x in currentX..sizeX && tile.y + 1 == currentY && !collision.check(
                            tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (currentX == tile.x + 1 && currentY <= tile.y && tile.y <= sizeY && !collision.check(
                            currentX,
                            tile.y,
                            plane,
                            Direction.WEST.flag()
                        )
                    ) {
                        return true
                    }
                    if (tile.x in currentX..sizeX && currentY == tile.y + 1 && !collision.check(
                            tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.flag()
                        )
                    ) {
                        return true
                    }
                }
            }
            if (type == 8) {
                if (tile.x in currentX..sizeX && currentY == tile.y + 1 && !collision.check(
                        tile.x,
                        currentY,
                        plane,
                        Direction.SOUTH.flag()
                    )
                ) {
                    return true
                }
                if (tile.x in currentX..sizeX && currentY == tile.y - sizeXY && !collision.check(
                        tile.x,
                        sizeY,
                        plane,
                        Direction.NORTH.flag()
                    )
                ) {
                    return true
                }
                return if (currentX == tile.x - sizeXY && currentY <= tile.y && tile.y <= sizeY && !collision.check(
                        sizeX,
                        tile.y,
                        plane,
                        Direction.EAST.flag()
                    )
                ) {
                    true
                } else currentX == tile.x + 1 && currentY <= tile.y && tile.y <= sizeY && !collision.check(
                    currentX,
                    tile.y,
                    plane,
                    Direction.WEST.flag()
                )
            }
        }
        return false
    }
}
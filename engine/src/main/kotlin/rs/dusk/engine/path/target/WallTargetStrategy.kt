package rs.dusk.engine.path.target

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.obj.IObject
import rs.dusk.engine.model.world.map.collision.Collisions
import rs.dusk.engine.model.world.map.collision.check
import rs.dusk.engine.model.world.map.collision.flag
import rs.dusk.engine.model.world.map.collision.wall
import rs.dusk.engine.path.Target
import rs.dusk.engine.path.TargetStrategy

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
class WallTargetStrategy(private val collision: Collisions) : TargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size, target: Target): Boolean {
        if (target !is IObject) {
            return false
        }
        val sizeXY = size.width
        // Check if under
        if (sizeXY == 1 && target.tile.x == currentX && currentY == target.tile.y) {
            return true
        } else if (target.tile.x >= currentX && target.tile.x <= currentX + sizeXY - 1 && target.tile.y <= target.tile.y + sizeXY - 1) {
            return true
        }

        if (sizeXY == 1) {
            if (target.type == 0) {
                var direction = Direction.cardinal[target.rotation + 3 and 0x3]
                if (currentX == target.tile.x + direction.delta.x && currentY == target.tile.y + direction.delta.y) {
                    return true
                }
                direction = Direction.cardinal[target.rotation and 0x3]
                if (currentX == target.tile.x - direction.delta.x && currentY == target.tile.y - direction.delta.y && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        direction.wall()
                    )
                ) {
                    return true
                }
                val inverse = direction.inverse()
                if (currentX == target.tile.x - inverse.delta.x && currentY == target.tile.y - inverse.delta.y && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        inverse.wall()
                    )
                ) {
                    return true
                }
            }
            if (target.type == 2) {
                val direction = Direction.ordinal[target.rotation and 0x3]
                val horizontal = direction.horizontal()
                if (currentX == target.tile.x + horizontal.delta.x && currentY == target.tile.y) {
                    return true
                }
                val vertical = direction.vertical()
                if (currentX == target.tile.x && currentY == target.tile.y + vertical.delta.y) {
                    return true
                }
                if (currentX == target.tile.x - horizontal.delta.x && currentY == target.tile.y && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        horizontal.wall()
                    )
                ) {
                    return true
                }
                if (currentX == target.tile.x && currentY == target.tile.y - vertical.delta.y && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        vertical.wall()
                    )
                ) {
                    return true
                }
            }
            if (target.type == 9) {
                Direction.ordinal.forEach { direction ->
                    if (currentX == target.tile.x - direction.delta.x && currentY == target.tile.y - direction.delta.y && !collision.check(
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
            if (target.type == 0) {
                if (target.rotation == 0) {
                    if (currentX == target.tile.x - sizeXY && target.tile.y >= currentY && target.tile.y <= sizeY) {
                        return true
                    }
                    if (currentY == target.tile.y + 1 && target.tile.x in currentX..sizeX && !collision.check(
                            target.tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == target.tile.y - sizeXY && target.tile.x in currentX..sizeX && !collision.check(
                            target.tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.wall()
                        )
                    ) {
                        return true
                    }
                } else if (target.rotation == 1) {
                    if (currentY == target.tile.y + 1 && target.tile.x >= currentX && target.tile.x <= sizeX) {
                        return true
                    }
                    if (currentX == target.tile.x - sizeXY && target.tile.y >= currentY && target.tile.y <= sizeY && !collision.check(
                            sizeX,
                            target.tile.y,
                            plane,
                            Direction.EAST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentX == target.tile.x + 1 && target.tile.y >= currentY && target.tile.y <= sizeY && !collision.check(
                            currentX,
                            target.tile.y,
                            plane,
                            Direction.WEST.wall()
                        )
                    ) {
                        return true
                    }
                } else if (target.rotation == 2) {
                    if (currentX == target.tile.x + 1 && target.tile.y >= currentY && target.tile.y <= sizeY) {
                        return true
                    }
                    if (currentY == target.tile.y + 1 && target.tile.x in currentX..sizeX && !collision.check(
                            target.tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == target.tile.y - sizeXY && target.tile.x in currentX..sizeX && !collision.check(
                            target.tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.wall()
                        )
                    ) {
                        return true
                    }
                } else if (target.rotation == 3) {
                    if (currentY == target.tile.y - sizeXY && currentX <= target.tile.x && sizeX >= target.tile.x) {
                        return true
                    }
                    if (currentX == target.tile.x - sizeXY && target.tile.y >= currentY && sizeY >= target.tile.y && !collision.check(
                            sizeX,
                            target.tile.y,
                            plane,
                            Direction.EAST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentX == target.tile.x + 1 && currentY <= target.tile.y && sizeY >= target.tile.y && !collision.check(
                            currentX,
                            target.tile.y,
                            plane,
                            Direction.WEST.wall()
                        )
                    ) {
                        return true
                    }
                }
            }
            if (target.type == 2) {
                if (target.rotation == 0) {
                    if (currentX == target.tile.x - sizeXY && target.tile.y >= currentY && sizeY >= target.tile.y) {
                        return true
                    }
                    if (currentY == target.tile.y + 1 && target.tile.x in currentX..sizeX) {
                        return true
                    }
                    if (currentX == target.tile.x + 1 && currentY <= target.tile.y && sizeY >= target.tile.y && !collision.check(
                            currentX,
                            target.tile.y,
                            plane,
                            Direction.WEST.wall()
                        )
                    ) {
                        return true
                    }
                    if (target.tile.y - sizeXY == currentY && target.tile.x in currentX..sizeX && !collision.check(
                            target.tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.wall()
                        )
                    ) {
                        return true
                    }
                } else if (target.rotation == 1) {
                    if (currentX == target.tile.x - sizeXY && currentY <= target.tile.y && sizeY >= target.tile.y && !collision.check(
                            sizeX,
                            target.tile.y,
                            plane,
                            Direction.EAST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == target.tile.y + 1 && target.tile.x in currentX..sizeX) {
                        return true
                    }
                    if (currentX == target.tile.x + 1 && currentY <= target.tile.y && sizeY >= target.tile.y) {
                        return true
                    }
                    if (currentY == target.tile.y - sizeXY && target.tile.x in currentX..sizeX && !collision.check(
                            target.tile.x,
                            sizeY,
                            plane,
                            Direction.NORTH.wall()
                        )
                    ) {
                        return true
                    }
                } else if (target.rotation == 2) {
                    if (currentX == target.tile.x - sizeXY && target.tile.y >= currentY && target.tile.y <= sizeY && !collision.check(
                            sizeX,
                            target.tile.y,
                            plane,
                            Direction.EAST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == target.tile.y + 1 && target.tile.x in currentX..sizeX && !collision.check(
                            target.tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentX == target.tile.x + 1 && currentY <= target.tile.y && sizeY >= target.tile.y) {
                        return true
                    }
                    if (currentY == target.tile.y - sizeXY && target.tile.x in currentX..sizeX) {
                        return true
                    }
                } else if (target.rotation == 3) {
                    if (currentX == target.tile.x - sizeXY && currentY <= target.tile.y && sizeY >= target.tile.y) {
                        return true
                    }
                    if (currentY == target.tile.y + 1 && target.tile.x in currentX..sizeX && !collision.check(
                            target.tile.x,
                            currentY,
                            plane,
                            Direction.SOUTH.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentX == target.tile.x + 1 && currentY <= target.tile.y && target.tile.y <= sizeY && !collision.check(
                            currentX,
                            target.tile.y,
                            plane,
                            Direction.WEST.wall()
                        )
                    ) {
                        return true
                    }
                    if (currentY == target.tile.y - sizeXY && target.tile.x in currentX..sizeX) {
                        return true
                    }
                }
            }
            if (target.type == 9) {
                if (target.tile.x in currentX..sizeX && currentY == target.tile.y + 1 && !collision.check(
                        target.tile.x,
                        currentY,
                        plane,
                        Direction.SOUTH.wall()
                    )
                ) {
                    return true
                }
                if (target.tile.x in currentX..sizeX && currentY == target.tile.y - sizeXY && !collision.check(
                        target.tile.x,
                        sizeY,
                        plane,
                        Direction.NORTH.wall()
                    )
                ) {
                    return true
                }
                return if (currentX == target.tile.x - sizeXY && currentY <= target.tile.y && sizeY >= target.tile.y && !collision.check(
                        sizeX,
                        target.tile.y,
                        plane,
                        Direction.EAST.wall()
                    )
                ) {
                    true
                } else currentX == target.tile.x + 1 && currentY <= target.tile.y && sizeY >= target.tile.y && !collision.check(
                    currentX,
                    target.tile.y,
                    plane,
                    Direction.WEST.wall()
                )
            }
        }
        return false
    }
}
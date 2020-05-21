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
class DecorationTargetStrategy(private val collision: Collisions) : TargetStrategy {

    override fun reached(currentX: Int, currentY: Int, plane: Int, size: Size, target: Target): Boolean {
        if (target !is Location) {
            return false
        }
        val targetX = target.tile.x
        val targetY = target.tile.y
        val sizeXY = size.width
        var rotation = target.rotation
        if (sizeXY == 1) {
            if (targetX == currentX && currentY == targetY) {
                return true
            }
        } else if (currentX <= targetX && sizeXY + currentX - 1 >= targetX && targetY <= sizeXY + targetY - 1) {
            return true
        }
        if (sizeXY == 1) {
            if (target.type == 6 || target.type == 7) {
                if (target.type == 7) {
                    rotation = rotation + 2 and 0x3
                }
                if (rotation == 0) {
                    if (currentX == targetX + 1 && currentY == targetY && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.WEST.flag()
                        )
                    ) {
                        return true
                    }
                    if (targetX == currentX && currentY == targetY - 1 && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.NORTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (currentX == targetX - 1 && currentY == targetY && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.EAST.flag()
                        )
                    ) {
                        return true
                    }
                    if (targetX == currentX && currentY == targetY - 1 && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.NORTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (currentX == targetX - 1 && targetY == currentY && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.EAST.flag()
                        )
                    ) {
                        return true
                    }
                    if (targetX == currentX && currentY == targetY + 1 && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.SOUTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (targetX + 1 == currentX && currentY == targetY && !collision.check(
                            currentX,
                            currentY,
                            plane,
                            Direction.WEST.flag()
                        )
                    ) {
                        return true
                    }
                    if (targetX == currentX && currentY == targetY + 1 && !collision.check(
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
            if (target.type == 8) {
                if (targetX == currentX && currentY == targetY + 1 && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        Direction.SOUTH.flag()
                    )
                ) {
                    return true
                }
                if (currentX == targetX && targetY - 1 == currentY && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        Direction.NORTH.flag()
                    )
                ) {
                    return true
                }
                return if (currentX == targetX - 1 && targetY == currentY && !collision.check(
                        currentX,
                        currentY,
                        plane,
                        Direction.EAST.flag()
                    )
                ) {
                    true
                } else targetX + 1 == currentX && targetY == currentY && !collision.check(
                    currentX,
                    currentY,
                    plane,
                    Direction.WEST.flag()
                )
            }
        } else {
            val sizeX = sizeXY + currentX - 1
            val sizeY = currentY + sizeXY - 1
            if (target.type == 6 || target.type == 7) {
                if (target.type == 7) {
                    rotation = rotation + 2 and 0x3
                }
                if (rotation == 0) {
                    if (currentX == targetX + 1 && currentY <= targetY && targetY <= sizeY && !collision.check(
                            currentX,
                            targetY,
                            plane,
                            Direction.WEST.flag()
                        )
                    ) {
                        return true
                    }
                    if (targetX in currentX..sizeX && currentY == targetY - sizeXY && !collision.check(
                            targetX,
                            sizeY,
                            plane,
                            Direction.NORTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 1) {
                    if (currentX == targetX - sizeXY && targetY >= currentY && targetY <= sizeY && !collision.check(
                            sizeX,
                            targetY,
                            plane,
                            Direction.EAST.flag()
                        )
                    ) {
                        return true
                    }
                    if (targetX in currentX..sizeX && currentY == targetY - sizeXY && !collision.check(
                            targetX,
                            sizeY,
                            plane,
                            Direction.NORTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 2) {
                    if (targetX - sizeXY == currentX && targetY >= currentY && targetY <= sizeY && !collision.check(
                            sizeX,
                            targetY,
                            plane,
                            Direction.EAST.flag()
                        )
                    ) {
                        return true
                    }
                    if (targetX in currentX..sizeX && targetY + 1 == currentY && !collision.check(
                            targetX,
                            currentY,
                            plane,
                            Direction.SOUTH.flag()
                        )
                    ) {
                        return true
                    }
                } else if (rotation == 3) {
                    if (currentX == targetX + 1 && currentY <= targetY && targetY <= sizeY && !collision.check(
                            currentX,
                            targetY,
                            plane,
                            Direction.WEST.flag()
                        )
                    ) {
                        return true
                    }
                    if (targetX in currentX..sizeX && currentY == targetY + 1 && !collision.check(
                            targetX,
                            currentY,
                            plane,
                            Direction.SOUTH.flag()
                        )
                    ) {
                        return true
                    }
                }
            }
            if (target.type == 8) {
                if (targetX in currentX..sizeX && currentY == targetY + 1 && !collision.check(
                        targetX,
                        currentY,
                        plane,
                        Direction.SOUTH.flag()
                    )
                ) {
                    return true
                }
                if (targetX in currentX..sizeX && currentY == targetY - sizeXY && !collision.check(
                        targetX,
                        sizeY,
                        plane,
                        Direction.NORTH.flag()
                    )
                ) {
                    return true
                }
                return if (currentX == targetX - sizeXY && currentY <= targetY && targetY <= sizeY && !collision.check(
                        sizeX,
                        targetY,
                        plane,
                        Direction.EAST.flag()
                    )
                ) {
                    true
                } else currentX == targetX + 1 && currentY <= targetY && targetY <= sizeY && !collision.check(
                    currentX,
                    targetY,
                    plane,
                    Direction.WEST.flag()
                )
            }
        }
        return false
    }
}
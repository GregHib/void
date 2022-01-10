package world.gregs.voidps.engine.map

import world.gregs.voidps.engine.entity.Size

object Overlap {

    fun isUnder(tile: Tile, size: Size, target: Tile, targetSize: Size) = isUnder(tile.x, tile.y, size, target.x, target.y, targetSize)

    fun isUnder(x: Int, y: Int, size: Size, targetX: Int, targetY: Int, targetSize: Size): Boolean {
        if (targetX > x + size.width - 1) {
            return false
        }
        if (targetY > y + size.height - 1) {
            return false
        }
        if (x > targetX + targetSize.width - 1) {
            return false
        }
        if (y > targetY + targetSize.height - 1) {
            return false
        }
        return true
    }

    fun isDiagonal(x: Int, y: Int, size: Size, targetX: Int, targetY: Int, targetSize: Size): Boolean {
        if (x >= targetX + targetSize.width && y >= targetY + targetSize.height) {
            return true// ne
        }
        if (x + size.width <= targetX && y >= targetY + targetSize.height) {
            return true// nw
        }
        if (x >= targetX + targetSize.width && y + size.height <= targetY) {
            return true// se
        }
        if (x + size.width <= targetX && y + size.height <= targetY) {
            return true// sw
        }
        return false
    }
}
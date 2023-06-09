package world.gregs.voidps.engine.map

import world.gregs.voidps.engine.entity.Size

object Overlap {

    fun isUnder(tile: Tile, width: Int, height: Int, target: Tile, targetWidth: Int, targetHeight: Int) = isUnder(tile.x, tile.y, width, height, target.x, target.y, targetWidth, targetHeight)

    fun isUnder(x: Int, y: Int, width: Int, height: Int, targetX: Int, targetY: Int, targetWidth: Int, targetHeight: Int): Boolean {
        if (targetX > x + width - 1) {
            return false
        }
        if (targetY > y + height - 1) {
            return false
        }
        if (x > targetX + targetWidth - 1) {
            return false
        }
        return y <= targetY + targetHeight - 1
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
package world.gregs.voidps.engine.map

import world.gregs.voidps.type.Tile

object Overlap {

    fun isUnder(tile: Tile, size: Int, target: Tile, targetSize: Int) = isUnder(tile.x, tile.y, size, size, target.x, target.y, targetSize, targetSize)

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

    fun isDiagonal(x: Int, y: Int, width: Int, height: Int, targetX: Int, targetY: Int, targetWidth: Int, targetHeight: Int): Boolean {
        if (x >= targetX + targetWidth && y >= targetY + targetHeight) {
            return true // ne
        }
        if (x + width <= targetX && y >= targetY + targetHeight) {
            return true // nw
        }
        if (x >= targetX + targetWidth && y + height <= targetY) {
            return true // se
        }
        if (x + width <= targetX && y + height <= targetY) {
            return true // sw
        }
        return false
    }
}

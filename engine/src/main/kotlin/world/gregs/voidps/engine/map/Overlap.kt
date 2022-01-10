package world.gregs.voidps.engine.map

import world.gregs.voidps.engine.entity.Size

object Overlap {

    fun isUnder(tile: Tile, size: Size, target: Tile, tSize: Size) = isUnder(tile.x, tile.y, size, target.x, target.y, tSize)

    fun isUnder(x: Int, y: Int, size: Size, tX: Int, tY: Int, tSize: Size): Boolean {
        if (tX > x + size.width - 1) {
            return false
        }
        if (tY > y + size.height - 1) {
            return false
        }
        if (x > tX + tSize.width - 1) {
            return false
        }
        if (y > tY + tSize.height - 1) {
            return false
        }
        return true
    }

    fun isDiagonal(x: Int, y: Int, size: Size, tX: Int, tY: Int, tSize: Size): Boolean {
        if (x >= tX + tSize.width && y >= tY + tSize.height) {
            return true// ne
        }
        if (x + size.width <= tX && y >= tY + tSize.height) {
            return true// nw
        }
        if (x >= tX + tSize.width && y + size.height <= tY) {
            return true// se
        }
        if (x + size.width <= tX && y + size.height <= tY) {
            return true// sw
        }
        return false
    }
}
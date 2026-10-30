package content.area.misthalin

import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle

object Border {
    // Longest axis determines direction, current location above is underside else above
    fun getOppositeSide(border: Rectangle, tile: Tile) = if (border.height > border.width) {
        tile.copy(y = if (tile.y > border.minY) border.minY - 1 else border.maxY + 1)
    } else {
        tile.copy(x = if (tile.x > border.minX) border.minX - 1 else border.maxX + 1)
    }

    // Tile in line with [tile] just outside the border, on the half of the border [side] is closest to
    fun getSide(border: Rectangle, tile: Tile, side: Tile) = if (border.height > border.width) {
        tile.copy(y = if (side.y * 2 < border.minY + border.maxY) border.minY - 1 else border.maxY + 1)
    } else {
        tile.copy(x = if (side.x * 2 < border.minX + border.maxX) border.minX - 1 else border.maxX + 1)
    }

    // Exit side when moving from [from] to [to] inside the border, or the opposite side if not moving along it
    fun getCrossingSide(border: Rectangle, from: Tile, to: Tile): Tile {
        val delta = if (border.height > border.width) to.y - from.y else to.x - from.x
        return when {
            delta > 0 -> getSide(border, from, Tile(border.maxX + 1, border.maxY + 1))
            delta < 0 -> getSide(border, from, Tile(border.minX - 1, border.minY - 1))
            else -> getOppositeSide(border, from)
        }
    }
}

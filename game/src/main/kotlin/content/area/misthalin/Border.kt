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
}
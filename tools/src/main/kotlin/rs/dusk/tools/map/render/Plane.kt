package rs.dusk.tools.map.render

import rs.dusk.engine.map.region.tile.TileData

open class Plane(val width: Int, val height: Int, val tiles: Array<Array<TileData?>>) {

    fun averageHeight(worldY: Int, worldX: Int): Int {
        val x = worldX shr tileScale
        val y = worldY shr tileScale
        if (x < 0 || y < 0 || x > width + -1 || -1 + height < y) {
            return 0
        }
        val dx: Int = tileUnits - 1 and worldX
        val dy: Int = tileUnits - 1 and worldY
        val a = dx * (tiles[1 + x][y]?.height ?: 0) + (tileUnits - dx) * (tiles[x][y]?.height ?: 0) shr tileScale
        val b = (tiles[x][y + 1]?.height ?: 0) * (tileUnits + -dx) + dx * (tiles[x + 1][y + 1]?.height ?: 0) shr tileScale
        return (-dy + tileUnits) * a + dy * b shr tileScale
    }

    companion object {
        val tileUnits: Int
        val tileScale: Int

        init {
            var scale = 0
            var units = 512
            while (units > 1) {
                scale++
                units = units shr 1
            }
            tileUnits = 1 shl scale
            tileScale = scale
        }
    }
}
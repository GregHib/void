package rs.dusk.tools.map.render

import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.tile.TileData

open class Plane(val width: Int, val height: Int, val plane: Int, val tiles: Map<Int, Array<Array<Array<TileData?>>>>) {


    fun tile(localX: Int, localY: Int): TileData {
        val regionX = localX / 64
        val regionY = localY / 64
        val regionId = Region.getId(regionX, regionY)
        return tiles[regionId]?.get(plane)?.get(localX.rem(64))?.get(localY.rem(64)) ?: emptyTile
    }

    fun averageHeight(worldY: Int, worldX: Int): Int {
        val x = worldX shr tileScale
        val y = worldY shr tileScale
        if (x < 0 || y < 0 || x > width + -1 || -1 + height < y) {
            return 0
        }
        val dx: Int = tileUnits - 1 and worldX
        val dy: Int = tileUnits - 1 and worldY
        val a = dx * tile(1 + x, y).height + (tileUnits - dx) * tile(x, y).height shr tileScale
        val b = tile(x, y + 1).height * (tileUnits + -dx) + dx * tile(x + 1, y + 1).height shr tileScale
        return (-dy + tileUnits) * a + dy * b shr tileScale
    }

    companion object {
        val emptyTile = TileData()
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
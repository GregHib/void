package world.gregs.voidps.cache.definition.data

import world.gregs.voidps.cache.Definition

class MapDefinition(
    override var id: Int = -1,
    val tiles: LongArray = LongArray(64 * 64 * 4),
    val objects: MutableList<MapObject> = mutableListOf()
) : Definition {


    fun getTile(localX: Int, localY: Int, plane: Int) = MapTile(tiles[index(localX, localY, plane)])

    fun setTile(localX: Int, localY: Int, plane: Int, tile: MapTile) {
        tiles[index(localX, localY, plane)] = tile.packed
    }

    fun setCol(x: Int, y: Int, plane: Int) {
        var booleans = collisions[zoneIndex(x, y, plane)]
        if (booleans == null) {
            booleans = BooleanArray(8 * 8)
            collisions[zoneIndex(x, y, plane)] = booleans
        }
        booleans[tileIndex(x, y)] = true
    }

    fun getCol(x: Int, y: Int, plane: Int): Boolean {
        return collisions[zoneIndex(x, y, plane)]?.get(tileIndex(x, y)) ?: false
    }

    fun getArray(x: Int, y: Int, plane: Int): BooleanArray? {
        return collisions[zoneIndex(x, y, plane)]
    }

    companion object {
        val collisions: Array<BooleanArray?> = arrayOfNulls(2048 * 2048 * 4)

        private fun tileIndex(x: Int, z: Int): Int = (x and 0x7) or ((z and 0x7) shl 3)

        private fun zoneIndex(x: Int, z: Int, level: Int): Int = ((x shr 3) and 0x7FF) or
                (((z shr 3) and 0x7FF) shl 11) or ((level and 0x3) shl 22)

        fun index(localX: Int, localY: Int, plane: Int): Int {
            return plane * 64 * 64 + localY * 64 + localX
        }
        fun getLocalX(tile: Int) = tile shr 6 and 0x3f
        fun getLocalY(tile: Int) = tile and 0x3f
        fun getPlane(tile: Int) = tile shr 12 and 0x3
    }
}
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

    companion object {
        fun index(localX: Int, localY: Int, plane: Int): Int {
            return plane * 64 * 64 + localY * 64 + localX
        }
        fun getLocalX(tile: Int) = tile shr 6 and 0x3f
        fun getLocalY(tile: Int) = tile and 0x3f
        fun getPlane(tile: Int) = tile shr 12 and 0x3

        fun id(packed: Long): Int = (packed shr 37 and 0x1ffff).toInt()
        fun x(packed: Long): Int = (packed shr 14 and 0x3fff).toInt()
        fun y(packed: Long): Int = (packed and 0x3fff).toInt()
        fun plane(packed: Long): Int = (packed shr 28 and 0x3).toInt()
        fun shape(packed: Long): Int = (packed shr 32 and 0x1f).toInt()
        fun rotation(packed: Long): Int = (packed shr 30 and 0x3).toInt()
    }
}
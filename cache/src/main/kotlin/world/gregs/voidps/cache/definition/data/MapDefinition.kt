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
        internal fun index(localX: Int, localY: Int, plane: Int): Int {
            return plane * 64 * 64 + localX * 64 + localY
        }
        internal fun localX(tile: Int) = tile shr 6 and 0x3f
        internal fun localY(tile: Int) = tile and 0x3f
        internal fun plane(tile: Int) = tile shr 12 and 0x3
    }
}
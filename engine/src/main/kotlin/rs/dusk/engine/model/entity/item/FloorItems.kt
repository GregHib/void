package rs.dusk.engine.model.entity.item

import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class FloorItems {
    val chunks: HashMap<Int, MutableList<FloorItem>> = hashMapOf()

    fun add(item: FloorItem) = chunks.getOrPut(item.tile.chunkPlane.id) { mutableListOf() }.add(item)

    fun remove(item: FloorItem): Boolean {
        val tile = chunks[item.tile.chunkPlane.id] ?: return false
        return tile.remove(item)
    }

    operator fun get(tile: Tile): List<FloorItem>? = chunks[tile.chunkPlane.id]

}

fun Tile.offset() = (x.rem(8) shl 4) or y.rem(8)
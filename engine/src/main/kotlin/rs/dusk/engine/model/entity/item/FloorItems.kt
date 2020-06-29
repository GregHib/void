package rs.dusk.engine.model.entity.item

import rs.dusk.engine.model.world.ChunkPlane
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

    operator fun get(tile: Tile): List<FloorItem>? = get(tile.chunkPlane)

    operator fun get(chunkPlane: ChunkPlane): List<FloorItem>? = chunks[chunkPlane.id]

}

fun Tile.offset(bit: Int = 4) = (x.rem(8) shl bit) or y.rem(8)
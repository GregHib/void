package rs.dusk.engine.model.entity.item

import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class FloorItems {
    val chunks: HashMap<Int, MutableList<FloorItem>> = hashMapOf()

    val updates: HashMap<ChunkPlane, MutableList<Message>> = hashMapOf()

    fun add(item: FloorItem) = chunks.getOrPut(item.tile.chunkPlane.id) { mutableListOf() }.add(item)

    fun remove(item: FloorItem): Boolean {
        val tile = chunks[item.tile.chunkPlane.id] ?: return false
        return tile.remove(item)
    }

    operator fun get(tile: Tile): List<FloorItem>? = chunks[tile.chunkPlane.id]

    fun update(tile: Tile, message: Message): Boolean {
        val list = updates.getOrPut(tile.chunkPlane) { mutableListOf() }
        return list.add(message)
    }

}

fun Tile.offset() = (x.rem(8) shl 4) or y.rem(8)
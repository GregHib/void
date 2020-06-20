package rs.dusk.engine.model.entity.item

import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
data class FloorItems(val delegate: HashMap<Int, MutableList<FloorItem>> = hashMapOf()) {

    val updates: HashMap<ChunkPlane, MutableList<Message>> = hashMapOf()

    fun add(item: FloorItem) = delegate.getOrPut(item.tile.chunkPlane.id) { mutableListOf() }.add(item)

    fun remove(item: FloorItem): Boolean {
        val tile = delegate[item.tile.chunkPlane.id] ?: return false
        return tile.remove(item)
    }

    operator fun get(tile: Tile): List<FloorItem>? = delegate[tile.chunkPlane.id]

    fun update(item: FloorItem, message: Message): Boolean {
        val list = updates.getOrPut(item.tile.chunkPlane) { mutableListOf() }
        return list.add(message)
    }

}

fun Tile.offset() = (x.rem(8) shl 4) or y.rem(8)
package world.gregs.voidps.engine.entity.item.floor

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.io.pool.DefaultPool
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.encode.chunk.FloorItemAddition
import world.gregs.voidps.network.encode.chunk.FloorItemRemoval
import world.gregs.voidps.network.encode.chunk.FloorItemUpdate

class FloorItemStorage(
    private val batches: ChunkBatchUpdates
) {

    internal val data = Int2ObjectOpenHashMap<MutableList<FloorItem>>()
    private val pool = object : DefaultPool<MutableList<FloorItem>>(INITIAL_POOL_CAPACITY) {
        override fun produceInstance() = ObjectArrayList<FloorItem>()
        override fun clearInstance(instance: MutableList<FloorItem>) = instance.apply { clear() }
    }

    fun add(tile: Tile, item: FloorItem) {
        val list = data.getOrPut(tile.id) { pool.borrow() }
        if (combined(list, tile, item)) {
            return
        }
        if (full(list, tile, item)) {
            return
        }
        if (list.add(item)) {
            batches.add(tile.chunk, FloorItemAddition(item.def.id, item.amount, tile.offset(), item.owner))
        }
    }

    /**
     * If [MAX_TILE_ITEMS] is reached replace the least or an equally valuable item,
     * otherwise prevent the item from being added.
     */
    private fun full(list: List<FloorItem>, tile: Tile, item: FloorItem): Boolean {
        if (list.size >= MAX_TILE_ITEMS) {
            val min = list.firstOrNull { it.value < item.value }
                ?: list.firstOrNull { it.value == item.value }
                ?: return true
            remove(tile, min)
        }
        return false
    }

    /**
     * Combine the amount's of two [FloorItem]
     */
    private fun combined(list: List<FloorItem>, tile: Tile, item: FloorItem): Boolean {
        if (item.owner == null) {
            return false
        }
        val existing = list.firstOrNull { it.owner == item.owner && it.id == item.id } ?: return false
        val original = existing.amount
        if (existing.combine(item)) {
            batches.add(tile.chunk, FloorItemUpdate(existing.def.id, tile.offset(), original, existing.amount, existing.owner))
            return true
        }
        return false
    }

    fun get(empty: Tile): List<FloorItem> {
        return data.getOrDefault(empty.id, emptyList())
    }

    fun remove(tile: Tile, item: FloorItem): Boolean {
        val list = data.get(tile.id) ?: return false
        if (list.remove(item)) {
            batches.add(item.tile.chunk, FloorItemRemoval(item.def.id, tile.offset(), item.owner))
            if (list.isEmpty() && data.remove<Int, Any>(tile.id, list)) {
                pool.recycle(list)
            }
            return true
        }
        return false
    }

    companion object {
        private const val MAX_TILE_ITEMS = 128
        private const val INITIAL_POOL_CAPACITY = 10
    }
}
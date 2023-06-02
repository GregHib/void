package world.gregs.voidps.engine.entity.item.floor

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.io.pool.DefaultPool
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.network.encode.chunk.FloorItemAddition
import world.gregs.voidps.network.encode.chunk.FloorItemRemoval
import world.gregs.voidps.network.encode.chunk.FloorItemUpdate
import world.gregs.voidps.network.encode.send

class FloorItems(
    private val batches: ChunkBatchUpdates,
    private val definitions: ItemDefinitions,
    private val store: EventHandlerStore
) : ChunkBatchUpdates.Sender {

    internal val data = Int2ObjectOpenHashMap<MutableList<FloorItem>>()
    private val pool = object : DefaultPool<MutableList<FloorItem>>(INITIAL_POOL_CAPACITY) {
        override fun produceInstance() = ObjectArrayList<FloorItem>()
        override fun clearInstance(instance: MutableList<FloorItem>) = instance.apply { clear() }
    }

    fun add(tile: Tile, id: String, amount: Int = 1, revealTicks: Int = -1, disappearTicks: Int = -1, owner: Player? = null): FloorItem {
        val definition = definitions.get(id)
        if (definition.id == -1) {
            logger.warn { "Null floor item $id $tile" }
        }
        val item = FloorItem(tile, id, amount, revealTicks, disappearTicks, if (revealTicks == 0) null else owner?.name)
        item.def = definition
        store.populate(item)
        add(item)
        item.events.emit(Registered)
        return item
    }

    fun add(item: FloorItem) {
        val list = data.getOrPut(item.tile.id) { pool.borrow() }
        if (combined(list, item)) {
            return
        }
        if (full(list, item)) {
            return
        }
        if (list.add(item)) {
            batches.add(item.tile.chunk, FloorItemAddition(item.tile.id, item.def.id, item.amount, item.owner))
        }
    }

    /**
     * If [MAX_TILE_ITEMS] is reached replace the least or an equally valuable item,
     * otherwise prevent the item from being added.
     */
    private fun full(list: List<FloorItem>, item: FloorItem): Boolean {
        if (list.size >= MAX_TILE_ITEMS) {
            val min = list.firstOrNull { it.value < item.value }
                ?: list.firstOrNull { it.value == item.value }
                ?: return true
            remove(min)
        }
        return false
    }

    /**
     * Combine the amount's of two [FloorItem]
     */
    private fun combined(list: List<FloorItem>, item: FloorItem): Boolean {
        if (item.owner == null) {
            return false
        }
        val existing = list.firstOrNull { it.owner == item.owner && it.id == item.id } ?: return false
        val original = existing.amount
        if (existing.combine(item)) {
            batches.add(item.tile.chunk, FloorItemUpdate(item.tile.id, existing.def.id, original, existing.amount, existing.owner))
            return true
        }
        return false
    }

    operator fun get(tile: Tile): List<FloorItem> {
        return data.getOrDefault(tile.id, emptyList())
    }

    fun remove(item: FloorItem): Boolean {
        val list = data.get(item.tile.id) ?: return false
        if (list.remove(item)) {
            batches.add(item.tile.chunk, FloorItemRemoval(item.tile.id, item.def.id, item.owner))
            if (list.isEmpty() && data.remove<Int, Any>(item.tile.id, list)) {
                pool.recycle(list)
            }
            return true
        }
        return false
    }

    fun clear() {
        for ((_, list) in data) {
            for (item in list) {
                batches.add(item.tile.chunk, FloorItemRemoval(item.tile.id, item.def.id, item.owner))
                item.events.emit(Unregistered)
            }
            pool.recycle(list)
        }
        data.clear()
    }

    override fun send(player: Player, chunk: Chunk) {
        for (tile in chunk.tile.toCuboid(8, 8)) {
            for (item in data.get(tile.id) ?: continue) {
                if (item.owner != null && item.owner != player.name) {
                    continue
                }
                player.client?.send(FloorItemAddition(tile.id, item.def.id, item.amount, item.owner))
            }
        }
    }

    companion object {
        private val logger = InlineLogger()
        private const val MAX_TILE_ITEMS = 128
        private const val INITIAL_POOL_CAPACITY = 10
    }
}


package world.gregs.voidps.engine.entity.item.floor

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.io.pool.DefaultPool
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.floor.FloorItems.Companion.MAX_TILE_ITEMS
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.emit
import world.gregs.voidps.network.encode.send
import world.gregs.voidps.network.encode.zone.FloorItemAddition
import world.gregs.voidps.network.encode.zone.FloorItemRemoval
import world.gregs.voidps.network.encode.zone.FloorItemUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Stores up to [MAX_TILE_ITEMS] [FloorItem]s per tile
 */
class FloorItems(
    private val batches: ZoneBatchUpdates,
    private val definitions: ItemDefinitions,
    private val store: EventHandlerStore
) : ZoneBatchUpdates.Sender {

    internal val data = Int2ObjectOpenHashMap<MutableMap<Int, MutableList<FloorItem>>>()
    private val tilePool = object : DefaultPool<MutableList<FloorItem>>(INITIAL_POOL_CAPACITY) {
        override fun produceInstance() = ObjectArrayList<FloorItem>()
        override fun clearInstance(instance: MutableList<FloorItem>) = instance.apply { clear() }
    }
    private val zonePool = object : DefaultPool<MutableMap<Int, MutableList<FloorItem>>>(INITIAL_POOL_CAPACITY) {
        override fun produceInstance() = Int2ObjectOpenHashMap<MutableList<FloorItem>>()
        override fun clearInstance(instance: MutableMap<Int, MutableList<FloorItem>>) = instance.apply { clear() }
    }

    fun add(tile: Tile, id: String, amount: Int = 1, revealTicks: Int = NEVER, disappearTicks: Int = NEVER, owner: Player?) = add(tile, id, amount, revealTicks, disappearTicks, owner?.name)

    fun add(tile: Tile, id: String, amount: Int = 1, revealTicks: Int = NEVER, disappearTicks: Int = NEVER, owner: String? = null): FloorItem {
        val definition = definitions.get(id)
        if (definition.id == -1) {
            logger.warn { "Null floor item $id $tile" }
        }
        val item = FloorItem(tile, id, amount, revealTicks, disappearTicks, if (revealTicks == 0) null else owner)
        item.def = definition
        store.populate(item)
        add(item)
        return item
    }

    fun add(item: FloorItem) {
        val list = data.getOrPut(item.tile.zone.id) { zonePool.borrow() }.getOrPut(item.tile.id) { tilePool.borrow() }
        if (combined(list, item)) {
            return
        }
        if (full(list, item)) {
            return
        }
        if (list.add(item)) {
            batches.add(item.tile.zone, FloorItemAddition(item.tile.id, item.def.id, item.amount, item.owner))
            item.emit(Registered)
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
        if (existing.merge(item)) {
            batches.add(item.tile.zone, FloorItemUpdate(item.tile.id, existing.def.id, original, existing.amount, existing.owner))
            return true
        }
        return false
    }

    operator fun get(tile: Tile): List<FloorItem> {
        return data.get(tile.zone.id)?.get(tile.id) ?: emptyList()
    }

    operator fun get(zone: Zone): Collection<List<FloorItem>> {
        return data.get(zone.id)?.values ?: emptyList()
    }

    fun remove(item: FloorItem): Boolean {
        val zone = data.get(item.tile.zone.id) ?: return false
        val list = zone[item.tile.id] ?: return false
        if (list.remove(item)) {
            batches.add(item.tile.zone, FloorItemRemoval(item.tile.id, item.def.id, item.owner))
            if (list.isEmpty() && zone.remove(item.tile.id, list)) {
                tilePool.recycle(list)
                if (zone.isEmpty() && data.remove(item.tile.zone.id) != null) {
                    zonePool.recycle(zone)
                }
            }
            item.emit(Unregistered)
            return true
        }
        return false
    }

    fun clear() {
        for ((_, zone) in data) {
            for ((_, items) in zone) {
                for (item in items) {
                    batches.add(item.tile.zone, FloorItemRemoval(item.tile.id, item.def.id, item.owner))
                    item.emit(Unregistered)
                }
                tilePool.recycle(items)
            }
            zonePool.recycle(zone)
        }
        data.clear()
    }

    override fun send(player: Player, zone: Zone) {
        for ((_, items) in data.get(zone.id) ?: return) {
            for (item in items) {
                if (item.owner != null && item.owner != player.name) {
                    continue
                }
                player.client?.send(FloorItemAddition(item.tile.id, item.def.id, item.amount, item.owner))
            }
        }
    }

    companion object {
        private val logger = InlineLogger()
        const val IMMEDIATE = 0
        const val NEVER = -1
        private const val MAX_TILE_ITEMS = 128
        private const val INITIAL_POOL_CAPACITY = 10
    }
}
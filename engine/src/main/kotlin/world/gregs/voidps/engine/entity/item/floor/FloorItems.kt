package world.gregs.voidps.engine.entity.item.floor

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.io.pool.DefaultPool
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.floor.FloorItems.Companion.MAX_TILE_ITEMS
import world.gregs.voidps.network.login.protocol.encode.send
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemAddition
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemRemoval
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Stores up to [MAX_TILE_ITEMS] [FloorItem]s per tile
 */
class FloorItems(
    private val batches: ZoneBatchUpdates,
    private val definitions: ItemDefinitions,
) : ZoneBatchUpdates.Sender,
    Runnable {

    internal val data = Int2ObjectOpenHashMap<MutableMap<Int, MutableList<FloorItem>>>()
    private val tilePool = object : DefaultPool<MutableList<FloorItem>>(INITIAL_POOL_CAPACITY) {
        override fun produceInstance() = ObjectArrayList<FloorItem>()
        override fun clearInstance(instance: MutableList<FloorItem>) = instance.apply { clear() }
    }
    private val zonePool = object : DefaultPool<MutableMap<Int, MutableList<FloorItem>>>(INITIAL_POOL_CAPACITY) {
        override fun produceInstance() = Int2ObjectOpenHashMap<MutableList<FloorItem>>()
        override fun clearInstance(instance: MutableMap<Int, MutableList<FloorItem>>) = instance.apply { clear() }
    }

    private val addQueue = mutableListOf<FloorItem>()
    private val removeQueue = mutableListOf<FloorItem>()

    override fun run() {
        for (floorItem in removeQueue) {
            floorItem.emit(Despawn)
        }
        removeQueue.clear()
        for (floorItem in addQueue) {
            add(floorItem)
            Spawn.spawn(floorItem)
        }
        addQueue.clear()
    }

    fun add(tile: Tile, id: String, amount: Int = 1, revealTicks: Int = NEVER, disappearTicks: Int = NEVER, charges: Int = 0, owner: Player?) = add(tile, id, amount, revealTicks, disappearTicks, charges, owner?.name)

    fun add(tile: Tile, id: String, amount: Int = 1, revealTicks: Int = NEVER, disappearTicks: Int = NEVER, charges: Int = 0, owner: String? = null): FloorItem {
        if (!definitions.contains(id)) {
            logger.warn { "Invalid floor item id: '$id' at $tile" }
        }
        val item = FloorItem(tile, id, amount, revealTicks, disappearTicks, charges, if (revealTicks == 0) null else owner)
        display(item)
        return item
    }

    private fun display(floorItem: FloorItem) {
        val list = data.getOrPut(floorItem.tile.zone.id) { zonePool.borrow() }.getOrPut(floorItem.tile.id) { tilePool.borrow() }
        if (combined(list, floorItem)) {
            return
        }
        if (full(list, floorItem)) {
            return
        }
        addQueue.add(floorItem)
        batches.add(floorItem.tile.zone, FloorItemAddition(floorItem.tile.id, floorItem.def.id, floorItem.amount, floorItem.owner))
    }

    private fun add(floorItem: FloorItem) {
        data.getOrPut(floorItem.tile.zone.id) { zonePool.borrow() }.getOrPut(floorItem.tile.id) { tilePool.borrow() }.add(floorItem)
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
    private fun combined(list: List<FloorItem>, floorItem: FloorItem): Boolean {
        if (floorItem.owner == null) {
            return false
        }
        val existing = list.firstOrNull { it.owner == floorItem.owner && it.id == floorItem.id } ?: return false
        val original = existing.amount
        if (existing.merge(floorItem)) {
            batches.add(floorItem.tile.zone, FloorItemUpdate(floorItem.tile.id, existing.def.id, original, existing.amount, existing.owner))
            return true
        }
        return false
    }

    operator fun get(tile: Tile): List<FloorItem> = data.get(tile.zone.id)?.get(tile.id) ?: emptyList()

    operator fun get(zone: Zone): Collection<List<FloorItem>> = data.get(zone.id)?.values ?: emptyList()

    fun remove(floorItem: FloorItem): Boolean {
        val zone = data.get(floorItem.tile.zone.id) ?: return false
        val list = zone[floorItem.tile.id] ?: return false
        if (list.remove(floorItem)) {
            removeQueue.add(floorItem)
            batches.add(floorItem.tile.zone, FloorItemRemoval(floorItem.tile.id, floorItem.def.id, floorItem.owner))
            if (list.isEmpty() && zone.remove(floorItem.tile.id, list)) {
                tilePool.recycle(list)
                if (zone.isEmpty() && data.remove(floorItem.tile.zone.id) != null) {
                    zonePool.recycle(zone)
                }
            }
            return true
        }
        return false
    }

    fun clear() {
        for ((_, zone) in data) {
            for ((_, items) in zone) {
                for (floorItem in items) {
                    batches.add(floorItem.tile.zone, FloorItemRemoval(floorItem.tile.id, floorItem.def.id, floorItem.owner))
                    removeQueue.add(floorItem)
                }
                tilePool.recycle(items)
            }
            zonePool.recycle(zone)
        }
        data.clear()
    }

    override fun send(player: Player, zone: Zone) {
        for ((_, items) in data.get(zone.id) ?: return) {
            for (floorItem in items) {
                if (floorItem.owner != null && floorItem.owner != player.name) {
                    continue
                }
                player.client?.send(FloorItemAddition(floorItem.tile.id, floorItem.def.id, floorItem.amount, floorItem.owner))
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

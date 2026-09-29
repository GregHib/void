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
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.network.login.protocol.encode.send
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemAddition
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemRemoval
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemReveal
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Stores up to [MAX_TILE_ITEMS] [FloorItem]s per tile
 */
object FloorItems : ZoneBatchUpdates.Sender, Runnable {

    private val data = Int2ObjectOpenHashMap<MutableMap<Int, MutableList<FloorItem>>>()
    private val tilePool = object : DefaultPool<MutableList<FloorItem>>(INITIAL_POOL_CAPACITY) {
        override fun produceInstance() = ObjectArrayList<FloorItem>()
        override fun clearInstance(instance: MutableList<FloorItem>) = instance.apply { clear() }
    }
    private val zonePool = object : DefaultPool<MutableMap<Int, MutableList<FloorItem>>>(INITIAL_POOL_CAPACITY) {
        override fun produceInstance() = Int2ObjectOpenHashMap<MutableList<FloorItem>>()
        override fun clearInstance(instance: MutableMap<Int, MutableList<FloorItem>>) = instance.apply { clear() }
    }

    private val removals = ObjectArrayList<FloorItem>()

    /**
     * Advance every items [FloorItem.lifecycle], revealing and removing items when their timers are complete
     */
    override fun run() {
        for ((_, zone) in data) {
            for ((_, list) in zone) {
                for (floorItem in list) {
                    when (floorItem.tick()) {
                        FloorItem.REVEAL -> reveal(floorItem)
                        FloorItem.REMOVE -> removals.add(floorItem)
                    }
                }
            }
        }
        for (floorItem in removals) {
            remove(floorItem)
        }
        removals.clear()
    }

    private fun reveal(floorItem: FloorItem) {
        val owner = floorItem.owner ?: return
        val player = Players.find(owner)
        ZoneBatchUpdates.add(floorItem.tile.zone, FloorItemReveal(floorItem.tile.id, floorItem.def.id, floorItem.amount, player?.index ?: -1))
        floorItem.owner = null
    }

    fun add(tile: Tile, id: String, amount: Int = 1, revealTicks: Int = NEVER, disappearTicks: Int = NEVER, charges: Int = 0, owner: Player?) = add(tile, id, amount, revealTicks, disappearTicks, charges, owner?.name)

    fun add(tile: Tile, id: String, amount: Int = 1, revealTicks: Int = NEVER, disappearTicks: Int = NEVER, charges: Int = 0, owner: String? = null): FloorItem {
        if (!ItemDefinitions.contains(id)) {
            logger.warn { "Invalid floor item id: '$id' at $tile" }
        }
        val item = FloorItem(tile, id, amount, revealTicks, disappearTicks, charges, if (revealTicks == IMMEDIATE) null else owner)
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
        list.add(floorItem)
        ZoneBatchUpdates.add(floorItem.tile.zone, FloorItemAddition(floorItem.tile.id, floorItem.def.id, floorItem.amount, floorItem.owner))
        Spawn.floorItem(floorItem)
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
            ZoneBatchUpdates.add(floorItem.tile.zone, FloorItemUpdate(floorItem.tile.id, existing.def.id, original, existing.amount, existing.owner))
            return true
        }
        return false
    }

    fun first(tile: Tile, id: String) = first(tile) { it.id == id }

    fun first(tile: Tile, filter: (FloorItem) -> Boolean) = firstOrNull(tile, filter) ?: error("Floor Item not found at $tile")

    fun firstOrNull(tile: Tile, id: String) = firstOrNull(tile) { it.id == id }

    fun firstOrNull(tile: Tile, filter: (FloorItem) -> Boolean) = at(tile).firstOrNull(filter)

    fun at(tile: Tile): List<FloorItem> = data.get(tile.zone.id)?.get(tile.id) ?: emptyList()

    fun at(zone: Zone): Collection<List<FloorItem>> = data.get(zone.id)?.values ?: emptyList()

    fun remove(floorItem: FloorItem): Boolean {
        val zone = data.get(floorItem.tile.zone.id) ?: return false
        val list = zone[floorItem.tile.id] ?: return false
        if (list.remove(floorItem)) {
            ZoneBatchUpdates.add(floorItem.tile.zone, FloorItemRemoval(floorItem.tile.id, floorItem.def.id, floorItem.owner))
            if (list.isEmpty() && zone.remove(floorItem.tile.id, list)) {
                tilePool.recycle(list)
                if (zone.isEmpty() && data.remove(floorItem.tile.zone.id) != null) {
                    zonePool.recycle(zone)
                }
            }
            Despawn.floorItem(floorItem)
            return true
        }
        return false
    }

    /**
     * Removes all items without triggering despawn events
     */
    fun clear() {
        for ((_, zone) in data) {
            for ((_, items) in zone) {
                for (floorItem in items) {
                    ZoneBatchUpdates.add(floorItem.tile.zone, FloorItemRemoval(floorItem.tile.id, floorItem.def.id, floorItem.owner))
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

    private val logger = InlineLogger()
    const val IMMEDIATE = 0
    const val NEVER = -1
    private const val MAX_TILE_ITEMS = 128
    private const val INITIAL_POOL_CAPACITY = 10
}

package world.gregs.voidps.engine.entity.item.floor

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.client.update.batch.*
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.strat.EntityTileTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.tick.Scheduler
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.network.chunk.ChunkUpdate
import world.gregs.voidps.network.chunk.update.FloorItemAddition

class FloorItems(
    private val definitions: ItemDefinitions,
    private val scheduler: Scheduler,
    private val store: EventHandlerStore,
    private val batches: ChunkBatches,
    private val collisions: Collisions,
    override val chunks: MutableMap<Int, MutableList<FloorItem>> = Int2ObjectOpenHashMap()
) : BatchList<FloorItem> {

    private val logger = InlineLogger()

    fun add(
        id: String,
        amount: Int,
        area: Area,
        revealTicks: Int = -1,
        disappearTicks: Int = -1,
        owner: Player? = null
    ): FloorItem? {
        val tile = area.random(collisions)
        if (tile == null) {
            logger.warn { "No free tile in item spawn area $area" }
            return null
        }
        return addItem(id, amount, tile, revealTicks, disappearTicks, owner, area)
    }

    /**
     * Spawns a floor item
     * Note: Not concerned with where the item is coming from
     * @param id The id of the item to spawn
     * @param amount The stack size of the item to spawn
     * @param tile The tile on which to spawn the item
     * @param revealTicks Number of ticks before the item is revealed to all
     * @param disappearTicks Number of ticks before the item is removed
     * @param owner The index of the owner of the item
     */
    fun add(
        id: String,
        amount: Int,
        tile: Tile,
        revealTicks: Int = -1,
        disappearTicks: Int = -1,
        owner: Player? = null
    ): FloorItem = addItem(id, amount, tile, revealTicks, disappearTicks, owner)

    private fun addItem(
        id: String,
        amount: Int,
        tile: Tile,
        revealTicks: Int = -1,
        disappearTicks: Int = -1,
        owner: Player? = null,
        area: Area? = null
    ): FloorItem {
        val definition = definitions.get(id)
        if (definitions.getOrNull(id) == null) {
            logger.warn { "Null floor item $id $tile" }
        }
        if (definition.stackable == 1) {
            val existing = getExistingStack(tile, id)
            if (existing != null && combinedStacks(existing, amount, disappearTicks)) {
                return existing
            }
        }
        val item = FloorItem(tile, id, amount, owner = if (revealTicks == 0) null else owner?.name)
        item.def = definition
        item.interactTarget = EntityTileTargetStrategy(item)
        item.tableTarget = RectangleTargetStrategy(collisions, item, true)
        store.populate(item)
        super.add(item)
        val update = addFloorItem(item)
        item["update"] = update
        batches.addInitial(tile.chunk, update)
        batches.update(tile.chunk, update)
        reveal(item, revealTicks, owner?.index ?: -1)
        disappear(item, disappearTicks)
        if (area != null) {
            item["area"] = area
        }
        item.events.emit(Registered)
        return item
    }

    private fun getExistingStack(tile: Tile, id: String): FloorItem? {
        return get(tile).firstOrNull { it.state == FloorItemState.Private && it.id == id }
    }

    /**
     * Combines both item stacks and resets disappear count down
     * Note: If total of combined stacks exceeds [Int.MAX_VALUE] then returns false
     */
    private fun combinedStacks(existing: FloorItem, amount: Int, disappearTicks: Int): Boolean {
        val stack = existing.amount
        val combined = stack + amount
        // Overflow should add as separate item
        if (stack xor combined and (amount xor combined) < 0) {
            return false
        }
        // Floor item is mutable because we need to keep the reveal timer from before
        existing.amount = combined
        val initial: FloorItemAddition = existing["update"]
        batches.removeInitial(existing.tile.chunk, initial)
        val update = addFloorItem(existing)
        existing["update"] = update
        batches.addInitial(existing.tile.chunk, update)
        batches.update(existing.tile.chunk, updateFloorItem(existing, stack, combined))
        existing.disappear?.cancel()
        disappear(existing, disappearTicks)
        return true
    }

    /**
     * Schedules disappearance after [ticks]
     */
    private fun disappear(item: FloorItem, ticks: Int) {
        if (ticks >= 0) {
            item.disappear = scheduler.add(ticks) {
                remove(item)
            }
        }
    }

    override fun remove(entity: FloorItem): Boolean {
        if (entity.state != FloorItemState.Removed) {
            entity.state = FloorItemState.Removed
            batches.update(entity.tile.chunk, removeFloorItem(entity))
            entity.remove<ChunkUpdate>("update")?.let {
                batches.removeInitial(entity.tile.chunk, it)
            }
            entity.disappear?.cancel()
            if (super.remove(entity)) {
                entity.events.emit(Unregistered)
                return true
            }
        }
        return false
    }

    /**
     * Schedules public reveal of [owner]'s item after [ticks]
     */
    private fun reveal(item: FloorItem, ticks: Int, owner: Int) {
        if (ticks <= 0 || owner == -1) {
            return
        }
        item.delay(ticks) {
            if (item.state != FloorItemState.Removed) {
                item.state = FloorItemState.Public
                batches.update(item.tile.chunk, revealFloorItem(item, owner))
            }
        }
    }

    fun clear() {
        val events = mutableListOf<FloorItem>()
        chunks.forEach { (_, set) ->
            set.forEach { item ->
                if (item.state != FloorItemState.Removed) {
                    item.state = FloorItemState.Removed
                    batches.update(item.tile.chunk, removeFloorItem(item))
                    item.remove<ChunkUpdate>("update")?.let {
                        batches.removeInitial(item.tile.chunk, it)
                    }
                }
            }
        }
        chunks.clear()
        for (item in events) {
            item.events.emit(Unregistered)
        }
    }
}

fun Tile.offset(bit: Int = 4) = (x.rem(8) shl bit) or y.rem(8)
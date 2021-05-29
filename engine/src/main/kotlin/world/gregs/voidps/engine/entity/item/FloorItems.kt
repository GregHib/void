package world.gregs.voidps.engine.entity.item

import kotlinx.coroutines.cancel
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.list.BatchList
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.engine.path.strat.PointTargetStrategy
import world.gregs.voidps.network.encode.addFloorItem
import world.gregs.voidps.network.encode.removeFloorItem
import world.gregs.voidps.network.encode.revealFloorItem
import world.gregs.voidps.network.encode.updateFloorItem

class FloorItems(
    private val decoder: ItemDefinitions,
    private val scheduler: Scheduler,
    private val store: EventHandlerStore,
    private val batches: ChunkBatches
) : BatchList<FloorItem> {

    override val chunks: MutableMap<Chunk, MutableSet<FloorItem>> = mutableMapOf()

    /**
     * Spawns a floor item
     * Note: Not concerned with where the item is coming from
     * @param name The id of the item to spawn
     * @param amount The stack size of the item to spawn
     * @param tile The tile on which to spawn the item
     * @param revealTicks Number of ticks before the item is revealed to all
     * @param disappearTicks Number of ticks before the item is removed
     * @param owner The index of the owner of the item
     */
    fun add(
        name: String,
        amount: Int,
        tile: Tile,
        revealTicks: Int = -1,
        disappearTicks: Int = -1,
        owner: Player? = null
    ): FloorItem? {
        val definition = decoder.get(name)
        if (definition.stackable == 1) {
            val existing = getExistingStack(tile, name)
            if (existing != null && combinedStacks(existing, amount, disappearTicks)) {
                return null
            }
        }
        val id = definition.id
        val item = FloorItem(tile, id, name, amount, owner = owner?.name)
        item.interactTarget = PointTargetStrategy(item)
        store.populate(item)
        super.add(item)
        val update = addFloorItem(item)
        item["update"] = update
        batches.addInitial(tile.chunk, update)
        batches.update(tile.chunk, update)
        reveal(item, revealTicks, owner?.index ?: -1)
        disappear(item, disappearTicks)
        item.events.emit(Registered)
        return item
    }

    fun getExistingStack(tile: Tile, name: String): FloorItem? {
        return get(tile).firstOrNull { it.tile == tile && it.state == FloorItemState.Private && it.name == name }
    }

    /**
     * Combines stacks of two items and resets the disappear count down
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
        batches.update(existing.tile.chunk, updateFloorItem(existing, stack, combined))
        existing.disappear?.cancel("Floor item disappear time extended.")
        disappear(existing, disappearTicks)
        return true
    }

    /**
     * Schedules disappearance after [ticks]
     */
    private fun disappear(item: FloorItem, ticks: Int) {
        if (ticks >= 0) {
            item.disappear = scheduler.launch {
                delay(ticks)
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
            if (super.remove(entity)) {
                entity.events.emit(Unregistered)
            }
        }
        return false
    }

    /**
     * Schedules public reveal of [owner]'s item after [ticks]
     */
    private fun reveal(item: FloorItem, ticks: Int, owner: Int) {
        if (ticks >= 0 && owner != -1) {
            scheduler.launch {
                delay(ticks)
                if (item.state != FloorItemState.Removed) {
                    item.state = FloorItemState.Public
                    batches.update(item.tile.chunk, revealFloorItem(item ,owner))
                }
            }
        }
    }
}

fun Tile.offset(bit: Int = 4) = (x.rem(8) shl bit) or y.rem(8)
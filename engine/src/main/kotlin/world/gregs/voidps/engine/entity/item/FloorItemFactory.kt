package world.gregs.voidps.engine.entity.item

import kotlinx.coroutines.cancel
import org.koin.dsl.module
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.engine.path.strat.PointTargetStrategy
import world.gregs.voidps.network.encode.addFloorItem
import world.gregs.voidps.network.encode.removeFloorItem
import world.gregs.voidps.network.encode.revealFloorItem
import world.gregs.voidps.network.encode.updateFloorItem

val floorItemModule = module {
    single { FloorItemFactory(get(), get(), get(), get(), get()) }
}

class FloorItemFactory(
    private val decoder: ItemDefinitions,
    private val items: FloorItems,
    private val scheduler: Scheduler,
    private val store: EventHandlerStore,
    private val batcher: ChunkBatcher
) {
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
    fun add(id: Int,
            amount: Int,
            tile: Tile,
            revealTicks: Int = -1,
            disappearTicks: Int = -1,
            owner: Player? = null
    ): FloorItem? {
        val definition = decoder.get(id)
        if (definition.stackable == 1) {
            val existing = items.getExistingStack(tile, id)
            if (existing != null && combinedStacks(existing, amount, disappearTicks)) {
                return null
            }
        }
        val item = FloorItem(tile, id, amount, owner = owner?.name)
        item.interactTarget = PointTargetStrategy(item)
        store.populate(item)
        items.add(item)
        batcher.update(tile.chunk) { player -> player.client?.addFloorItem(tile.offset(), id, amount) }
        reveal(item, revealTicks, owner?.index ?: -1)
        disappear(item, disappearTicks)
        item.events.emit(Registered)
        return item
    }

    fun add(
        name: String,
        amount: Int,
        tile: Tile,
        revealTicks: Int = -1,
        disappearTicks: Int = -1,
        owner: Player? = null
    ) = add(decoder.getId(name), amount, tile, revealTicks, disappearTicks, owner)


    fun FloorItems.getExistingStack(tile: Tile, id: Int): FloorItem? {
        return get(tile).firstOrNull { it.tile == tile && it.state == FloorItemState.Private && it.id == id }
    }

    /**
     * Combines stacks of two items and resets the disappear count down
     * Note: If total of combined stacks exceeds [Int.MAX_VALUE] then returns false
     */
    fun combinedStacks(existing: FloorItem, amount: Int, disappearTicks: Int): Boolean {
        val stack = existing.amount
        val combined = stack + amount
        // Overflow should add as separate item
        if (stack xor combined and (amount xor combined) < 0) {
            return false
        }
        // Floor item is mutable because we need to keep the reveal timer from before
        existing.amount = combined
        batcher.update(existing.tile.chunk) { player -> player.client?.updateFloorItem(existing.tile.offset(), existing.id, stack, combined) }
        existing.disappear?.cancel("Floor item disappear time extended.")
        disappear(existing, disappearTicks)
        return true
    }

    /**
     * Schedules disappearance after [ticks]
     */
    fun disappear(item: FloorItem, ticks: Int) {
        if (ticks >= 0) {
            item.disappear = scheduler.launch {
                delay(ticks)
                if (item.state != FloorItemState.Removed) {
                    item.state = FloorItemState.Removed
                    batcher.update(item.tile.chunk) { player -> player.client?.removeFloorItem(item.tile.offset(), item.id) }
                    items.remove(item)
                }
            }
        }
    }

    /**
     * Schedules public reveal of [owner]'s item after [ticks]
     */
    fun reveal(item: FloorItem, ticks: Int, owner: Int) {
        if (ticks >= 0 && owner != -1) {
            scheduler.launch {
                delay(ticks)
                if (item.state != FloorItemState.Removed) {
                    item.state = FloorItemState.Public
                    batcher.update(item.tile.chunk) { player -> player.client?.revealFloorItem(item.tile.offset(), item.id, item.amount, owner) }
                }
            }
        }
    }

    init {
        batcher.addInitial { player, chunk, messages ->
            items[chunk].forEach {
                if (it.visible(player)) {
                    messages += { player -> player.client?.addFloorItem(it.tile.offset(), it.id, it.amount) }
                }
            }
        }
    }
}
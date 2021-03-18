import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.cancel
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.FloorItemState
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.engine.path.strat.PointTargetStrategy
import world.gregs.voidps.network.encode.addFloorItem
import world.gregs.voidps.network.encode.removeFloorItem
import world.gregs.voidps.network.encode.revealFloorItem
import world.gregs.voidps.network.encode.updateFloorItem
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.item.spawn.Drop
import world.gregs.voidps.world.interact.entity.player.equip.ContainerAction

val decoder: ItemDefinitions by inject()
val items: FloorItems by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()
val logger = InlineLogger()

ContainerAction where { container == "inventory" && option == "Drop" } then {
    val id = player.inventory.getItem(slot)
    val amount = player.inventory.getAmount(slot)
    if (player.inventory.clear(slot) && id != -1 && amount > 0) {
        bus.emit(Drop(id, amount, player.tile, 60, 60, player))
    } else {
        logger.info { "Error dropping item $id $amount for $player" }
    }
}

Drop then {
    val definition = decoder.get(id)
    if (definition.stackable == 1) {
        val existing = items.getExistingStack(tile, id)
        if (existing != null && combinedStacks(existing, amount, disappearTicks)) {
            return@then
        }
    }
    val item = FloorItem(tile, id, amount, owner = owner?.name)
    item.interactTarget = PointTargetStrategy(item)
    items.add(item)
    batcher.update(tile.chunk) { player -> player.client?.addFloorItem(tile.offset(), id, amount) }
    reveal(item, revealTicks, owner?.index ?: -1)
    disappear(item, disappearTicks)
    bus.emit(Registered(item))
    result = item
}

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

batcher.addInitial { player, chunk, messages ->
    items[chunk].forEach {
        if (it.visible(player)) {
            messages += { player -> player.client?.addFloorItem(it.tile.offset(), it.id, it.amount) }
        }
    }
}
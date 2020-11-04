import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.cancel
import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.update.visual.player.name
import rs.dusk.engine.entity.item.FloorItem
import rs.dusk.engine.entity.item.FloorItemState
import rs.dusk.engine.entity.item.FloorItems
import rs.dusk.engine.entity.item.detail.ItemDefinitions
import rs.dusk.engine.entity.item.offset
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.chunk.ChunkBatcher
import rs.dusk.engine.path.strat.PointTargetStrategy
import rs.dusk.network.rs.codec.game.encode.message.FloorItemAddMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemRemoveMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemRevealMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemUpdateMessage
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.item.spawn.Drop
import rs.dusk.world.interact.entity.player.equip.ContainerAction

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
    batcher.update(tile.chunk, FloorItemAddMessage(tile.offset(), id, amount))
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
    batcher.update(
        existing.tile.chunk,
        FloorItemUpdateMessage(existing.tile.offset(), existing.id, stack, combined)
    )
    existing.disappear?.cancel("Floor item disappear time extended.")
    disappear(existing, disappearTicks)
    return true
}

/**
 * Schedules disappearance after [ticks]
 */
fun disappear(item: FloorItem, ticks: Int) {
    if (ticks >= 0) {
        item.disappear = scheduler.add {
            delay(ticks)
            if (item.state != FloorItemState.Removed) {
                item.state = FloorItemState.Removed
                batcher.update(item.tile.chunk, FloorItemRemoveMessage(item.tile.offset(), item.id))
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
        scheduler.add {
            delay(ticks)
            if (item.state != FloorItemState.Removed) {
                item.state = FloorItemState.Public
                batcher.update(
                    item.tile.chunk,
                    FloorItemRevealMessage(item.tile.offset(), item.id, item.amount, owner)
                )
            }
        }
    }
}

batcher.addInitial { player, chunk, messages ->
    items[chunk].forEach {
        if (it.visible(player)) {
            messages += FloorItemAddMessage(it.tile.offset(), it.id, it.amount)
        }
    }
}
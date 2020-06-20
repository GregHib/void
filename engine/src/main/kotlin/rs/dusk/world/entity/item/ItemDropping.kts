import kotlinx.coroutines.CancellationException
import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.item.FloorItem
import rs.dusk.engine.model.entity.item.FloorItemState
import rs.dusk.engine.model.entity.item.FloorItems
import rs.dusk.engine.model.entity.item.offset
import rs.dusk.network.rs.codec.game.encode.message.FloorItemAddMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemRemoveMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemRevealMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemUpdateMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.item.Drop
import kotlin.coroutines.resumeWithException

val items: FloorItems by inject()
val scheduler: Scheduler by inject()

Drop then {
    // TODO switch [Drop] to raw data and create FloorItem here so we can emit Registered(floorItem) and delete FloorItemFactory?
    if(item.def.stackable == 1) {
        val existing = items.getExistingStack(item)
        if(existing != null && combinedStacks(existing, item, disappearTicks)) {
            return@then
        }
    }
    items.add(item)
    items.update(item, FloorItemAddMessage(item.tile.offset(), item.id, item.amount))
    reveal(item, revealTicks, owner)
    disappear(item, disappearTicks)
}


fun FloorItems.getExistingStack(item: FloorItem): FloorItem? {
    return get(item.tile)?.firstOrNull { it.state == FloorItemState.Private && it.id == item.id }
}

/**
 * Combines stacks of two items and resets the disappear count down
 * Note: If total of combined stacks exceeds [Int.MAX_VALUE] then returns false
 */
fun combinedStacks(existing: FloorItem, item: FloorItem, disappearTicks: Int): Boolean {
    val stack = existing.amount
    val amount = item.amount
    val combined = stack + amount
    // Overflow should add as separate item
    if (stack xor combined and (amount xor combined) < 0) {
        return false
    }
    existing.amount = combined
    items.update(item, FloorItemUpdateMessage(existing.tile.offset(), existing.id, stack, combined))
    existing.disappear?.resumeWithException(CancellationException("Floor item disappear time extended."))
    disappear(item, disappearTicks)
    return true
}

/**
 * Schedules disappearance after [ticks]
 */
fun disappear(item: FloorItem, ticks: Int) {
    if(ticks >= 0) {
        item.disappear = scheduler.add {
            delay(ticks)
            item.state = FloorItemState.Removed
            items.update(item, FloorItemRemoveMessage(item.tile.offset(), item.id))
            items.remove(item)
        }
    }
}

/**
 * Schedules public reveal of [owner]'s item after [ticks]
 */
fun reveal(item: FloorItem, ticks: Int, owner: Int) {
    if(ticks >= 0) {
        scheduler.add {
            delay(ticks)
            item.state = FloorItemState.Public
            items.update(item, FloorItemRevealMessage(item.tile.offset(), item.id, item.amount, owner))
        }
    }
}
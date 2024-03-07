package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

data class InventoryUpdate(
    val inventory: String,
    val updates: List<ItemChanged>
) : Event {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "inventory_update"
        else -> null
    }
}
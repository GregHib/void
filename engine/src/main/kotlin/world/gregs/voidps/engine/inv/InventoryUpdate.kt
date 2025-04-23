package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class InventoryUpdate(
    val inventory: String,
    val updates: List<InventorySlotChanged>
) : Event {
    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when(index) {
        0 -> "inventory_update"
        1 -> inventory
        else -> null
    }
}

fun inventoryUpdate(inventory: String = "*", handler: suspend InventoryUpdate.(Player) -> Unit) {
    Events.handle("inventory_update", inventory, handler = handler)
}
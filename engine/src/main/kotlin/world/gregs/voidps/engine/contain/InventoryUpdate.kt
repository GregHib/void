package world.gregs.voidps.engine.contain

import world.gregs.voidps.engine.event.Event

data class InventoryUpdate(
    val inventory: String,
    val updates: List<ItemChanged>
) : Event
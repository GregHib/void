package world.gregs.voidps.engine.contain

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

data class ItemChanged(
    val container: String,
    val index: Int,
    val oldItem: Item,
    val item: Item,
    val from: String,
    val to: String
) : Event {

    val added = oldItem.id.isBlank() && item.id.isNotBlank()

    val removed = oldItem.id.isNotBlank() && item.id.isBlank()

}

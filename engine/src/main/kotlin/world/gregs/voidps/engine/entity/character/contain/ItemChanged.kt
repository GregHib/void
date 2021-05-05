package world.gregs.voidps.engine.entity.character.contain

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * @param moved Whether the item was moved internally or to another [Container]
 */
data class ItemChanged(
    val container: String,
    val index: Int,
    val oldItem: Item,
    val item: Item,
    val moved: Boolean
) : Event {

    val added = oldItem.name.isBlank() && item.name.isNotBlank()

    val removed = oldItem.name.isNotBlank() && item.name.isBlank()

}

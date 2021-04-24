package world.gregs.voidps.engine.entity.character.contain

import world.gregs.voidps.engine.event.Event

/**
 * @param moved Whether the item was moved internally or to another [Container]
 */
data class ItemChanged(
    val container: String,
    val index: Int,
    val oldItem: String,
    val oldAmount: Int,
    val item: String,
    val amount: Int,
    val moved: Boolean
) : Event {

    val added = oldItem.isBlank() && item.isNotBlank()

    val removed = oldItem.isNotBlank() && item.isBlank()

}

package content.entity.player.inv.item.take

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * @param floorItem The item which was taken off of the floor
 * @param item The item which was given to the player
 */
data class Taken(val floorItem: FloorItem, val item: String) : CancellableEvent() {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "taken"
        1 -> floorItem.id
        else -> null
    }
}

fun taken(vararg items: String = arrayOf("*"), handler: Taken.(Player) -> Unit) {
    for (item in items) {
        Events.handle("taken", item, handler = handler)
    }
}

package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class InterfaceOnItem(
    val id: String,
    val component: String,
    val index: Int,
    val item: Item,
    val itemSlot: Int,
    val inventory: String,
) : Event {

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "interface_on_item"
        1 -> item.id
        2 -> id
        3 -> component
        else -> null
    }
}

fun interfaceOnItem(id: String = "*", component: String = "*", item: String = "*", handler: suspend InterfaceOnItem.(Player) -> Unit) {
    Events.handle("interface_on_item", item, id, component, handler = handler)
}

fun interfaceOnItems(id: String = "*", component: String = "*", items: Array<String> = arrayOf("*"), handler: suspend InterfaceOnItem.(Player) -> Unit) {
    for (item in items) {
        Events.handle("interface_on_item", item, id, component, handler = handler)
    }
}

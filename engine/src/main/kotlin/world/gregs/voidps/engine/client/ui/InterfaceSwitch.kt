package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class InterfaceSwitch(
    val id: String,
    val component: String,
    val fromItem: Item,
    val fromSlot: Int,
    val fromInventory: String,
    val toId: String,
    val toComponent: String,
    val toItem: Item,
    val toSlot: Int,
    val toInventory: String,
) : Event {

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "interface_switch"
        1 -> id
        2 -> component
        3 -> toId
        4 -> toComponent
        else -> null
    }
}

fun interfaceSwap(fromId: String = "*", fromComponent: String = "*", toId: String = fromId, toComponent: String = fromComponent, handler: suspend InterfaceSwitch.(Player) -> Unit) {
    Events.handle("interface_switch", fromId, fromComponent, toId, toComponent, handler = handler)
}

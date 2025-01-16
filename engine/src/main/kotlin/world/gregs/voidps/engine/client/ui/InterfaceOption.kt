package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class InterfaceOption(
    override val character: Player,
    val id: String,
    val component: String,
    val optionIndex: Int,
    val option: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction<Player>() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "interface_option"
        1 -> id
        2 -> component
        3 -> option
        4 -> itemSlot.toString()
        else -> null
    }
}

fun interfaceOption(option: String = "*", component: String = "*", id: String, handler: suspend InterfaceOption.() -> Unit) {
    Events.handle<InterfaceOption>("interface_option", id, component, option, "*") {
        handler.invoke(this)
    }
}

fun interfaceSlot(component: String = "*", id: String, itemSlot: Int = -1, handler: suspend InterfaceOption.() -> Unit) {
    Events.handle<InterfaceOption>("interface_option", id, component, "*", if (itemSlot == -1) "*" else itemSlot.toString()) {
        handler.invoke(this)
    }
}
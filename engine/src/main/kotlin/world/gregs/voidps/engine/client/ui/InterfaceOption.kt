package world.gregs.voidps.engine.client.ui

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
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

    companion object {
        val handlers: MutableMap<String, suspend InterfaceOption.() -> Unit> = Object2ObjectOpenHashMap()
    }
}

fun interfaceOption(option: String = "*", component: String = "*", id: String, handler: suspend InterfaceOption.() -> Unit) {
    when {
        !option.contains('*') && !component.contains('*') && !id.contains('*') -> InterfaceOption.handlers["${id}:${component}:${option}"] = handler
        option == "*" && !component.contains('*') && !id.contains('*') -> InterfaceOption.handlers["${id}:${component}"] = handler
        option == "*" && component == "*" && !id.contains('*') -> InterfaceOption.handlers[id] = handler
        else -> Events.handle<InterfaceOption>("interface_option", id, component, option, "*") {
            handler.invoke(this)
        }
    }
}
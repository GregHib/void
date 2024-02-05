package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

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
    val toInventory: String
) : Event

fun interfaceSwitch(filter: InterfaceSwitch.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend InterfaceSwitch.(Player) -> Unit) {
    on<InterfaceSwitch>(filter, priority, block)
}

fun interfaceSwitch(id: String, component: String, option: String, block: suspend InterfaceOption.() -> Unit) {
    on<InterfaceOption>({ wildcardEquals(this.id, id) && wildcardEquals(this.component, component) && wildcardEquals(this.option, option) }) { _: Player ->
        block.invoke(this)
    }
}
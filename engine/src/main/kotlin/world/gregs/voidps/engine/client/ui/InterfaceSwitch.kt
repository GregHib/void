package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

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
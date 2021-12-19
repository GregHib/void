package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

data class InterfaceOption(
    val id: String,
    val component: String,
    val optionIndex: Int,
    val option: String,
    val item: Item,
    val itemIndex: Int,
    val container: String
) : Event

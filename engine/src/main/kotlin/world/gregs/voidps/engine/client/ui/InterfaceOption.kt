package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.event.Event

data class InterfaceOption(
    val id: Int,
    val name: String,
    val componentId: Int,
    val component: String,
    val optionId: Int,
    val option: String,
    val item: String,
    val itemId: Int,
    val itemIndex: Int
) : Event

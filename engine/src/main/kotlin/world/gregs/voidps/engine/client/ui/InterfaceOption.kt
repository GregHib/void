package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.item.Item

data class InterfaceOption(
    val item: Item,
    val itemSlot: Int,
    val option: String,
    val optionIndex: Int,
    val interfaceComponent: String,
) {
    val id: String
        get() = interfaceComponent.substringBefore(":")
    val component: String
        get() = interfaceComponent.substringAfter(":")
}

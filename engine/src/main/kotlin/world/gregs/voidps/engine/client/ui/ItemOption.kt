package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.entity.item.Item

data class ItemOption(
    val item: Item,
    val slot: Int,
    val inventory: String,
    val option: String,
) {
    override fun toString(): String {
        return "$option:${item.id}:${inventory} slot=$slot"
    }
}
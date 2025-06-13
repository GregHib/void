package world.gregs.voidps.engine.data.config

import world.gregs.voidps.engine.entity.item.Item

/**
 * Codes to redeem holiday items from django
 * @param variable the variable to unlock this item
 * @param add the items added with this code
 */
data class DiangoCodeDefinition(
    val variable: String = "",
    val add: List<Item> = listOf(),
) {

    companion object {
        val EMPTY = DiangoCodeDefinition()
    }
}

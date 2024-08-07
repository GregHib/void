package world.gregs.voidps.engine.data.config

import world.gregs.voidps.engine.entity.item.Item

/**
 * Codes to redeem holiday items from django
 * @param variable the variable to unlock this item
 * @param add the items added with this code
 */
data class DiangoCodeDefinition(
    val variable: String = "",
    val add: List<Item> = emptyList(),
) {

    companion object {

        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>) = DiangoCodeDefinition(
            variable = map["variable"] as? String ?: EMPTY.variable,
            add = map["add"] as? List<Item> ?: EMPTY.add,
        )

        val EMPTY = DiangoCodeDefinition()
    }
}
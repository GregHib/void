package world.gregs.voidps.engine.data.definition.data

import world.gregs.voidps.engine.entity.item.Item

/**
 * Represents the smelting mechanics, including requirements, rewards, and success chances.
 *
 * @param level The required level to perform smelting.
 * @param xp The experience rewarded upon a successful smelting action.
 * @param chance The success chance for smelting, where 255 indicates 100% success by default.
 * @param items The list of items resulting from the smelting process.
 * @param message The associated message or feedback displayed during smelting.
 */
data class Smelting(
    val level: Int = 0,
    val xp: Double = 0.0,
    val chance: Int = 255,
    val items: List<Item> = emptyList(),
    val message: String = ""
) {
    /**
     * Companion object for the Smelting class.
     * Provides utility methods and constants for creating and managing Smelting instances.
     */
    companion object {
        /**
         * Creates an instance of the `Smelting` class using a map of values.
         * Fallbacks to default `EMPTY` values for missing or invalid entries in the map.
         *
         * @param map A map containing keys and values to initialize the Smelting instance:
         * - `level`: Int representing the required level for smelting, default is `EMPTY.level`.
         * - `xp`: Double representing the experience points gained from smelting, default is `EMPTY.xp`.
         * - `chance`: IntRange or its highest value representing the probability of successful smelting, default is `EMPTY.chance`.
         * - `items`: List<Map<String, Any>> containing item maps with fields `item` (String) and `amount` (Int, default 1), default is `EMPTY.items`.
         * - `message`: String representing the message associated with smelting, default is `EMPTY.message`.
         */
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>) = Smelting(
            level = map["level"] as Int,
            xp = map["xp"] as Double,
            chance = (map["chance"] as? IntRange)?.last ?: EMPTY.chance,
            items = (map["items"] as List<Map<String, Any>>).map { Item(it["item"] as String, it["amount"] as? Int ?: 1) },
            message = map["message"] as String
        )

        /**
         * A predefined constant representing an instance of the Smelting class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Smelting()
    }
}
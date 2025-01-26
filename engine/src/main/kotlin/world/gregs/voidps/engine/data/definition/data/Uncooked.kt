package world.gregs.voidps.engine.data.definition.data

/**
 * Represents an uncooked item with properties related to its cooking process, such as experience gained, chances of success, and related messaging.
 *
 * @param level The required level to cook the item.
 * @param xp The experience rewarded for successfully cooking the item.
 * @param burntXp The experience rewarded if the item is burnt during cooking.
 * @param chance The range of chance influencing the cooking outcome when using fire.
 * @param rangeChance The range of chance influencing the cooking outcome when using a range.
 * @param cooksRangeChance The range of chance when using a range with cook-specific bonuses.
 * @param gauntletChance The range of chance when using cooking gauntlets.
 * @param cooked The identifier for the successfully cooked item.
 * @param cookedMessage The message displayed upon successfully cooking the item.
 * @param burnt The identifier for the burnt variant of the item.
 * @param burntMessage The message displayed upon burning the item.
 * @param leftover The identifier for any leftover item produced during cooking.
 * @param start The starting level of cooking ticks.
 * @param ticks The total number of ticks required for cooking the item.
 * @param type Specifies the type of cooking process (e.g., cook, bake, etc.).
 * @param rangeOnly A flag indicating if the item can only be cooked on a range.
 */
data class Uncooked(
    val level: Int = 1,
    val xp: Double = 0.0,
    val burntXp: Double = 0.0,
    val chance: IntRange = 255..255,
    val rangeChance: IntRange = chance,
    val cooksRangeChance: IntRange = rangeChance,
    val gauntletChance: IntRange = rangeChance,
    val cooked: String = "",
    val cookedMessage: String = "",
    val burnt: String = "",
    val burntMessage: String = "",
    val leftover: String = "",
    val start: Int = 1,
    val ticks: Int = 4,
    val type: String = "cook",
    val rangeOnly: Boolean = false
) {

    /**
     * Companion object for the Uncooked class.
     * Provides utility methods and constants for creating and managing Uncooked instances.
     */
    companion object {

        /**
         * Creates an instance of the Uncooked class using values provided in the map.
         * If certain keys are missing or their values are invalid, default values from `EMPTY` are used.
         *
         * @param map A map containing keys and corresponding values to initialize an instance of Uncooked:
         * - `chances`: Map<String, IntRange> (optional) representing various chances such as fire, range, cooks_range, or gauntlet.
         * - `level`: Int (optional) indicating the level, default is `EMPTY.level`.
         * - `xp`: Double (optional) indicating the experience points, default is `EMPTY.xp`.
         * - `cooked`: String (optional) indicating the cooked item, default is `EMPTY.cooked`.
         * - `cooked_message`: String (optional) representing a message related to the cooked item, default is `EMPTY.cookedMessage`.
         * - `burnt`: String (optional) representing the burnt item, default is `EMPTY.burnt`.
         * - `burnt_message`: String (optional) representing a message related to the burnt item, default is `EMPTY.burntMessage`.
         * - `leftover`: String (optional) representing the leftover item, default is `EMPTY.leftover`.
         * - `type`: String (optional) indicating the type, default is `EMPTY.type`.
         *
         * @return An instance of the Uncooked class initialized with the values provided in the map.
         */
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>): Uncooked {
            val chances = map["chances"] as? Map<String, IntRange> ?: emptyMap()
            val fireChance = chances["fire"] ?: EMPTY.chance
            val rangeChance = chances["range"] ?: fireChance
            val cooksRangeChance = chances["cooks_range"] ?: rangeChance
            val gauntletChance = chances["gauntlet"] ?: rangeChance
            return Uncooked(
                level = map["level"] as? Int ?: EMPTY.level,
                xp = map["xp"] as? Double ?: EMPTY.xp,
                chance = fireChance,
                rangeChance = rangeChance,
                cooksRangeChance = cooksRangeChance,
                gauntletChance = gauntletChance,
                cooked = map["cooked"] as? String ?: EMPTY.cooked,
                cookedMessage = map["cooked_message"] as? String ?: EMPTY.cookedMessage,
                burnt = map["burnt"] as? String ?: EMPTY.burnt,
                burntMessage = map["burnt_message"] as? String ?: EMPTY.burntMessage,
                leftover = map["leftover"] as? String ?: EMPTY.leftover,
                type = map["type"] as? String ?: EMPTY.type,
                rangeOnly = chances.containsKey("range") && !chances.containsKey("fire")
            )
        }

        /**
         * A predefined constant representing an instance of the `Uncooked` class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Uncooked()
    }
}
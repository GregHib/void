package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the mechanics of weaving, including the amount of materials, the final product,
 * the level requirement, and the experience gained.
 *
 * @param amount The quantity of materials needed for the weaving process.
 * @param to The resulting product of the weaving process.
 * @param level The required skill level to perform the weaving action.
 * @param xp The experience points rewarded for successfully completing the weaving.
 */
data class Weaving(
    val amount: Int = 1,
    val to: String = "",
    val level: Int = 1,
    val xp: Double = 0.0
) {

    /**
     * Companion object for the Weaving class.
     * Provides utility methods and constants for creating and managing Weaving instances.
     */
    companion object {

        /**
         * Creates an instance of the Weaving class using values from the provided map.
         * Defaults to `EMPTY` values for missing or invalid entries in the map.
         *
         * @param map A map containing keys and values for initializing the Weaving instance:
         * - `amount`: Int representing the quantity, default is `EMPTY.amount`.
         * - `to`: String representing the destination or target, default is an empty string.
         * - `level`: Int representing the level requirement, default is `EMPTY.level`.
         * - `xp`: Double representing experience points, default is `EMPTY.xp`.
         */
        operator fun invoke(map: Map<String, Any>) = Weaving(
            amount = map["amount"] as? Int ?: EMPTY.amount,
            to = map["to"] as? String ?: "",
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
        )

        /**
         * A predefined constant representing an instance of the Weaving class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Weaving()
    }
}
package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the skills and experience associated with jewellery crafting or usage.
 *
 * @param level The level required for crafting or interacting with the jewellery.
 * @param xp The experience gained from crafting or interacting with the jewellery.
 */
data class Jewellery(
    val level: Int = 1,
    val xp: Double = 0.0
) {

    /**
     * Companion object for the Jewellery class.
     * Provides utility methods and constants for creating and managing Jewellery instances.
     */
    companion object {
        /**
         * Creates an instance of the Jewellery class based on the provided map.
         * If a property is missing or is of a different type, default values are used.
         *
         * @param map A map containing key-value pairs where:
         * - `level`: Int, the level of the jewellery (default is `Spinning.EMPTY.level`).
         * - `xp`: Double, the experience value of the jewellery (default is `Spinning.EMPTY.xp`).
         */
        operator fun invoke(map: Map<String, Any>) = Jewellery(
            level = map["level"] as? Int ?: Spinning.EMPTY.level,
            xp = map["xp"] as? Double ?: Spinning.EMPTY.xp,
        )

        /**
         * A predefined constant representing an instance of the Jewellery class with default values.
         * This instance serves as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Jewellery()
    }
}
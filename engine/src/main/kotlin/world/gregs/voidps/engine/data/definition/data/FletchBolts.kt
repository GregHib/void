package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the properties associated with fletching bolts in a game or simulation.
 *
 * @param level The player's level required to fletch the bolts.
 * @param xp The amount of experience awarded for successfully fletching the bolts.
 */
data class FletchBolts(
    val level: Int = 1,
    val xp: Double = 0.0
) {
    /**
     * Companion object for the FletchBolts class.
     * Provides utility methods and constants for creating and managing FletchBolts instances.
     */
    companion object {

        /**
         * Creates a new FletchBolts instance based on the provided map of values.
         * Missing or invalid values in the map default to the `EMPTY` instance properties.
         *
         * @param map A map containing keys and corresponding values for `level` and `xp`.
         * - `level`: Int (the required level, default is `EMPTY.level`)
         * - `xp`: Double (experience value, default is `EMPTY.xp`)
         */
        operator fun invoke(map: Map<String, Any>) = FletchBolts(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
        )

        /**
         * Represents a predefined constant of a `FletchBolts` instance with default values.
         * Used as a placeholder or default value to ensure safe initialization and prevent null references.
         */
        val EMPTY = FletchBolts()
    }
}
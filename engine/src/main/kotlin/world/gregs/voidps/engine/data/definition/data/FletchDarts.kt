package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the properties for fletching darts in the game.
 *
 * @param level The required level to fletch darts.
 * @param xp The experience awarded for successfully fletching darts.
 */
data class FletchDarts(
    val level: Int = 1,
    val xp: Double = 0.0
) {
    /**
     * Companion object for the FletchDarts class.
     * Provides utility methods and constants for creating and managing FletchDarts instances.
     */
    companion object {

        /**
         * Creates a FletchDarts instance based on the given map.
         * If the map contains missing or invalid values, the default values from `EMPTY` are used.
         *
         * @param map A map containing the following optional keys:
         * - `level`: Int (required level, defaults to `EMPTY.level`)
         * - `xp`: Double (experience value, defaults to `EMPTY.xp`)
         */
        operator fun invoke(map: Map<String, Any>) = FletchDarts(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
        )

        /**
         * A predefined constant representing an instance of the FletchDarts class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = FletchDarts()
    }
}
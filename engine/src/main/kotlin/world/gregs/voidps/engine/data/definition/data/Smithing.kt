package world.gregs.voidps.engine.data.definition.data

/**
 * Represents a Smithing activity with properties defining the level required and experience points.
 *
 * @param level The level of Smithing. Default is 0.
 * @param xp The experience points accumulated in Smithing. Default is 0.0.
 */
data class Smithing(
    val level: Int = 0,
    val xp: Double = 0.0
) {
    /**
     * Companion object for the Smithing class.
     * Provides utility methods and constants for creating and managing Smithing instances.
     */
    companion object {
        /**
         * Creates an instance of the Smithing class using values from the provided map.
         *
         * @param map A map containing keys and values to initialize the Smithing instance:
         * - `level`: Int representing the level, required and must be a valid integer.
         * - `xp`: Double representing experience points, required and must be a valid double.
         */
        operator fun invoke(map: Map<String, Any>) = Smithing(
            level = map["level"] as Int,
            xp = map["xp"] as Double
        )
        /**
         * A predefined constant representing an instance of the Smithing class with default values.
         * Used as a placeholder or default value for initialization or to avoid null references.
         */
        val EMPTY = Smithing()
    }
}
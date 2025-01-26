package world.gregs.voidps.engine.data.definition.data

/**
 * Represents a collection of pottery data, composed of various ceramics and their associated properties.
 *
 * @param map A mapping of ceramic names to their respective `Ceramic` instances.
 * Defaults to an empty map if no data is provided.
 */
data class Pottery(
    val map: Map<String, Ceramic> = emptyMap()
) {

    /**
     * Represents a Ceramic object with level and experience points (xp).
     *
     * @param level The current level of the ceramic object. Defaults to 1.
     * @param xp The experience points associated with the ceramic object. Defaults to 0.0.
     */
    data class Ceramic(
        val level: Int = 1,
        val xp: Double = 0.0
    ) {
        /**
         * Companion object for the Ceramic class.
         * Provides utility methods and constants for creating and managing Ceramic instances.
         */
        companion object {
            /**
             * Creates an instance of the Ceramic class using the provided map.
             * Missing or invalid values will default to the equivalent values from `EMPTY`.
             *
             * @param map A map containing key-value pairs to initialize the Ceramic instance.
             * - `level`: Int representing the level required, defaults to `EMPTY.level`.
             * - `xp`: Double representing the experience points, defaults to `EMPTY.xp`.
             */
            operator fun invoke(map: Map<String, Any>) = Ceramic(
                level = map["level"] as? Int ?: EMPTY.level,
                xp = map["xp"] as? Double ?: EMPTY.xp,
            )

            /**
             * A predefined constant representing an instance of the `Ceramic` class with default values.
             * Used as a placeholder or default value to avoid null references or for initialization purposes.
             */
            val EMPTY = Ceramic()
        }
    }

    /**
     * Companion object for the Pottery class.
     * Provides utility constants for managing Pottery instances.
     */
    companion object {
        /**
         * A predefined constant representing an instance of the Pottery class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Pottery()
    }
}
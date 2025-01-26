package world.gregs.voidps.engine.data.definition.data

/**
 * Represents a cleaning activity with associated level and experience.
 * The `Cleaning` class provides functionality for instantiating objects
 * with default values or mapping values from a given data structure.
 *
 * @param level The level required or associated with cleaning.
 * @param xp The experience gained or associated with cleaning.
 */
data class Cleaning(
        val level: Int = 1,
        val xp: Double = 0.0
) {
    /**
     * Companion object for the Cleaning data class. Provides utility functions and constants
     * for working with Cleaning instances.
     */
    companion object {

        /**
         * Creates a new instance of the `Cleaning` class from the given map, using default values for missing or invalid keys.
         *
         * @param map A map containing key-value pairs where:
         * - "level" is the cleaning level as an `Int`.
         * - "clean_xp" is the cleaning experience points as a `Double`.
         * If a key is missing or its value cannot be cast to the required type, a default value will be used.
         */
        operator fun invoke(map: Map<String, Any>) = Cleaning(
                level = map["level"] as? Int ?: EMPTY.level,
                xp = map["clean_xp"] as? Double ?: EMPTY.xp,
        )

        /**
         * Represents an empty or default instance of the `Cleaning` data class.
         * Used as a baseline for creating or referencing uninitialized `Cleaning` objects.
         */
        val EMPTY = Cleaning()
    }
}
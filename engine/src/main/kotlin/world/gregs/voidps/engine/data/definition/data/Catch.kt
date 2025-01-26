package world.gregs.voidps.engine.data.definition.data

/**
 * Data class representing a Catch entity that contains attributes related to its level, experience points (xp),
 * and a chance range.
 *
 * @property level The level associated with the Catch instance. Defaults to 1.
 * @property xp The experience points associated with the Catch instance. Defaults to 0.0.
 * @property chance The range of chance associated with the Catch instance. Defaults to 1..1.
 */
data class Catch(
    val level: Int = 1,
    val xp: Double = 0.0,
    val chance: IntRange = 1..1
) {
    /**
     * Companion object for the `Catch` class.
     * Provides utility methods and properties for handling instances of the `Catch` class.
     */
    companion object {

        /**
         * Converts a map of values to a Catch object by extracting the relevant properties.
         *
         * @param map A map containing key-value pairs. Expected keys are:
         * - "level" (Int?): The level value to be extracted. Defaults to `EMPTY.level` if not present or invalid.
         * - "xp" (Double?): The experience points value to be extracted. Defaults to `EMPTY.xp` if not present or invalid.
         * - "chance" (IntRange?): The range defining the chance value. Defaults to `EMPTY.chance` if not present or invalid.
         */
        operator fun invoke(map: Map<String, Any>) = Catch(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            chance = map["chance"] as? IntRange ?: EMPTY.chance
        )

        /**
         * Represents an empty state or default instance of the `Catch` class.
         * It is typically used as a placeholder or to signify the absence of a specific value.
         */
        val EMPTY = Catch()
    }
}
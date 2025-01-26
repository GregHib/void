package world.gregs.voidps.engine.data.definition.data

/**
 * Represents an ore with properties influencing its mechanics, such as the experience rewarded and the chance of occurrence.
 *
 * @param xp The experience rewarded for successfully interacting with the ore.
 * @param chance The range of chance influencing the occurrence or success of mining the ore.
 */
data class Ore(
    val xp: Double = 0.0,
    val chance: IntRange = 0..0
) {
    /**
     * Companion object for the Ore class.
     * Provides utility methods and constants for managing Ore instances.
     */
    companion object {

        /**
         * Creates an instance of the Ore class using values from the provided map.
         * Fallbacks to default `EMPTY` values for missing or invalid entries in the map.
         *
         * @param map A map containing keys and values to initialize the Ore instance.
         * - `xp`: Double representing experience points, default is `EMPTY.xp`.
         * - `chance`: IntRange representing the range of chance, default is `EMPTY.chance`.
         */
        operator fun invoke(map: Map<String, Any>) = Ore(
            xp = map["xp"] as? Double ?: EMPTY.xp,
            chance = map["chance"] as? IntRange ?: EMPTY.chance
        )

        /**
         * A predefined constant representing an instance of the Ore class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Ore()
    }
}
package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the properties of fire, typically associated with its usage mechanics such as color, duration, and chance of occurrence.
 *
 * @param level The required level to interact with the fire feature.
 * @param xp The experience rewarded for interacting with the fire.
 * @param chance The range of chance influencing interactions or outcomes involving the fire.
 * @param life The duration in ticks that the fire will remain active.
 * @param colour The visual representation of the fire, given as a color.
 */
data class Fire(
    val level: Int = 1,
    val xp: Double = 0.0,
    val chance: IntRange = 65..513,
    val life: Int = 0,
    val colour: String = "orange"
) {
    /**
     * Companion object for the Fire class.
     * Provides utility methods and constants for creating and managing Fire instances.
     */
    companion object {

        /**
         * Creates a Fire instance based on the provided map of values.
         * Fallbacks to default `EMPTY` values for missing or invalid entries in the map.
         *
         * @param map A map containing keys and corresponding values for `level`, `xp`, `chance`, `life`, and `colour`.
         * - `level`: Int (required level, default is `EMPTY.level`)
         * - `xp`: Double (experience value, default is `EMPTY.xp`)
         * - `chance`: IntRange (chance range, default is `EMPTY.chance`)
         * - `life`: Int (duration in ticks, default is `EMPTY.life`)
         * - `colour`: String (colour representation, default is `EMPTY.colour`)
         */
        operator fun invoke(map: Map<String, Any>) = Fire(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            chance = map["chance"] as? IntRange ?: EMPTY.chance,
            life = map["life"] as? Int ?: EMPTY.life,
            colour = map["colour"] as? String ?: EMPTY.colour
        )

        /**
         * A predefined constant representing an instance of the Fire class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Fire()
    }
}
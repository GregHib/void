package world.gregs.voidps.engine.data.definition.data

/**
 * Represents a rock that can contain ores and potentially gems.
 *
 * @param level The hardness or toughness level of the rock. Higher levels may require more effort to mine.
 * @param ores A list of ore types available in the rock.
 * @param life The remaining durability or life of the rock. A value of -1 indicates infinite durability.
 * @param gems Indicates if the rock contains gems.
 */
data class Rock(
    val level: Int = 1,
    val ores: List<String> = emptyList(),
    val life: Int = -1,
    val gems: Boolean = false
) {
    /**
     * Companion object for the Rock class.
     * Provides utility methods and constants for creating and managing Rock instances.
     */
    companion object {

        /**
         * Creates an instance of the Rock class using values from the provided map.
         * Fallbacks to default `EMPTY` values for missing or invalid entries in the map.
         *
         * @param map A map containing keys and values for initializing the Rock instance:
         * - `level`: Int representing the level, default is `EMPTY.level`.
         * - `ores`: List<String> representing ore types, default is `EMPTY.ores`.
         * - `life`: Int representing life or durability, default is `EMPTY.life`.
         * - `gems`: Boolean indicating if gems are present, default is `EMPTY.gems`.
         */
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>) = Rock(
            level = map["level"] as? Int ?: EMPTY.level,
            ores = map["ores"] as? List<String> ?: EMPTY.ores,
            life = map["life"] as? Int ?: EMPTY.life,
            gems = map["gems"] as? Boolean ?: EMPTY.gems,
        )

        /**
         * A predefined constant representing an instance of the Rock class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Rock()
    }
}
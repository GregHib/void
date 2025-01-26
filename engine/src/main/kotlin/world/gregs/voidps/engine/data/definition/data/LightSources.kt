package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the properties of light sources, encompassing details about their states
 * when lit or extinguished, as well as an associated lighting level.
 *
 * @param onceLit A string representing the state or identifier of the light source when lit.
 * @param onceExtinguish A string representing the state or identifier of the light source when extinguished.
 * @param level An integer representing the level or intensity of the light source.
 */
data class LightSources(
    val onceLit: String = "",
    val onceExtinguish: String = "",
    val level: Int = -1
) {
    /**
     * Companion object for the LightSources class.
     * Provides utility methods and constants for creating and managing LightSources instances.
     */
    companion object {

        /**
         * Creates a `LightSources` instance based on the provided map of values.
         * Defaults to `EMPTY` values for any missing or invalid map entries.
         *
         * @param map A map containing keys and corresponding values for `onceLit`, `onceExtinguish`, and `level`:
         * - `onceLit`: String (value for once lit, defaults to `EMPTY.onceLit`)
         * - `onceExtinguish`: String (value for once extinguished, defaults to `EMPTY.onceExtinguish`)
         * - `level`: Int (value for level, defaults to `EMPTY.level`)
         */
        operator fun invoke(map: Map<String, Any>) = LightSources(
            onceLit = map["once_lit"] as? String ?: EMPTY.onceLit,
            onceExtinguish = map["once_extinguish"] as? String ?: EMPTY.onceExtinguish,
            level = map["level"] as? Int ?: EMPTY.level,
        )

        /**
         * A predefined constant representing an instance of the LightSources class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = LightSources()
    }
}
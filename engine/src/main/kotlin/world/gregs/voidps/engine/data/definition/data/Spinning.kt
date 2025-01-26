package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the spinning mechanic in the context of processing items or resources.
 *
 * @param to The resulting item or state after spinning.
 * @param level The minimum level required to perform the spinning action.
 * @param xp The experience awarded for successfully completing the spinning process.
 */
data class Spinning(
    val to: String = "",
    val level: Int = 1,
    val xp: Double = 0.0
) {

    /**
     * Companion object for the Spinning class.
     * Provides utility methods and constants for creating and managing Spinning instances.
     */
    companion object {

        /**
         * Creates a Spinning instance using values from the given map, with fallback to default values
         * if map entries are missing or invalid.
         *
         * @param map A map containing keys and corresponding values for initializing the Spinning instance:
         * - `to`: String, default is an empty string.
         * - `level`: Int, default is `EMPTY.level`.
         * - `xp`: Double, default is `EMPTY.xp`.
         */
        operator fun invoke(map: Map<String, Any>) = Spinning(
            to = map["to"] as? String ?: "",
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
        )

        /**
         * A predefined constant representing an instance of the `Spinning` class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Spinning()
    }
}
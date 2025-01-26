package world.gregs.voidps.engine.data.definition.data

/**
 * Represents a fishing spot with the properties defining available tackle and bait options.
 *
 * @param tackle A list of strings representing the tackle items available for use in the fishing spot.
 * @param bait A map of bait types (string keys) paired with lists of corresponding bait options.
 */
data class Spot(
    val tackle: List<String> = emptyList(),
    val bait: Map<String, List<String>> = emptyMap()
) {

    /**
     * Companion object for the Spot class.
     * Provides utility methods and constants for creating and managing Spot instances.
     */
    companion object {
        /**
         * Creates an instance of the Spot class using values from the provided map.
         *
         * @param map A map containing keys and corresponding values for initializing the Spot instance:
         * - `items`: List<String> representing the tackle items.
         * - `bait`: Map<String, List<String>> representing the bait configuration.
         * @return A Spot instance initialized with the specified map values.
         */
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>): Spot {
            return Spot(
                tackle = map["items"] as List<String>,
                bait = map["bait"] as Map<String, List<String>>
            )
        }

        /**
         * A predefined constant representing an instance of the Spot class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Spot()
    }
}
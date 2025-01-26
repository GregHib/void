package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the pricing structure for a tanning process, consisting of a nested list
 * of prices and their associated values or conditions.
 *
 * @param prices A nested list where each sublist represents specific pricing information.
 * Each sublist can contain varying types of data, such as price values and any related attributes.
 */
data class Tanning(
    val prices: List<List<Any>> = emptyList()
) {
    /**
     * Companion object for the Tanning class.
     * Provides utility constants for creating and managing Tanning instances.
     */
    companion object {
        /**
         * A predefined constant representing an instance of the Tanning class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Tanning()
    }
}
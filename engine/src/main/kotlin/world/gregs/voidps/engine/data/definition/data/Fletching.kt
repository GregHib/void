package world.gregs.voidps.engine.data.definition.data

/**
 * Represents the properties and actions related to fletching, including level requirements, experience gained,
 * animations used, production amount, and associated ticks for the activity.
 *
 * @param level The level required to perform the fletching action.
 * @param xp The experience points gained upon a successful fletching action.
 * @param animation The animation played during the fletching process.
 * @param makeAmount The number of items produced per fletching action.
 * @param tick The number of ticks required to complete the action; default is -1.
 */
data class Fletching(
    val level: Int = 1,
    val xp: Double = 0.0,
    val animation: String = "",
    val makeAmount: Int = 1,
    val tick: Int = -1
) {
    /**
     * Companion object for the Fletching class.
     * Provides utility methods and constants for creating and managing Fletching instances.
     */
    companion object {

        /**
         * Creates an instance of Fletching based on the provided map of values.
         * Each key in the map corresponds to a property of the Fletching object.
         * If a key is absent or its value cannot be cast to the expected type, a default value is used.
         *
         * @param map A map containing the property keys and values:
         * - `level`: Int (the required level, default is `EMPTY.level`)
         * - `xp`: Double (experience points, default is `EMPTY.xp`)
         * - `animation`: String (animation name, default is `EMPTY.animation`)
         * - `make_amount`: Int (quantity to make, default is `EMPTY.makeAmount`)
         * - `tick`: Int (time in ticks, default is `EMPTY.tick`)
         */
        operator fun invoke(map: Map<String, Any>) = Fletching(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            animation = map["animation"] as? String ?: EMPTY.animation,
            makeAmount = map["make_amount"] as? Int ?: EMPTY.makeAmount,
            tick = map["tick"] as? Int ?: EMPTY.tick,
        )

        /**
         * A predefined constant representing an instance of the Fletching class with default values.
         * Used as a placeholder or default value to avoid null references or for initialization purposes.
         */
        val EMPTY = Fletching()
    }
}
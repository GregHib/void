package world.gregs.voidps.engine.data.definition.data

/**
 * Represents an item that can be picked up, with customizable properties for respawn delay and messaging.
 *
 * @param item The identifier or name of the item that can be picked up.
 * @param respawnDelay The time in ticks before the item reappears after being picked up.
 * @param message A custom message displayed when the item is interacted with.
 */
data class Pickable(
    val item: String = "",
    val respawnDelay: Int = -1,
    val message: String = ""
) {
    /**
     * Companion object for the Pickable class.
     * Provides utility methods and constants for creating and managing Pickable instances.
     */
    companion object {

        /**
         * Creates an instance of the Pickable class using the values from the provided map.
         * Missing or invalid values default to the `EMPTY` instance's properties.
         *
         * @param map A map containing key-value pairs to initialize the Pickable instance:
         * - `item`: A String representing the item name. Defaults to `EMPTY.item` if not provided or invalid.
         * - `delay`: An Int representing the respawn delay in ticks. Defaults to `EMPTY.respawnDelay` if not provided or invalid.
         * - `message`: A String representing a message associated with the item. Defaults to `EMPTY.message` if not provided or invalid.
         */
        operator fun invoke(map: Map<String, Any>) = Pickable(
            item = (map["item"] as? String) ?: EMPTY.item,
            respawnDelay = map["delay"] as? Int ?: EMPTY.respawnDelay,
            message = map["message"] as? String ?: EMPTY.message,
        )

        /**
         * A predefined constant representing an instance of the Pickable class with default values.
         * Serves as a default or placeholder value to avoid null references or for initialization purposes.
         */
        val EMPTY = Pickable()
    }
}
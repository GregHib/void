package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.area.Rectangle

/**
 * Represents a named area of tiles with additional metadata.
 *
 * @property name The name of the area.
 * @property area The shape and size of the area, represented as an implementation of [Area].
 * @property tags A set of associated tags for categorizing or describing the area.
 * @property stringId A unique identifier for the area, defaulting to the area's name.
 * @property extras A map of additional metadata or properties associated with the area.
 */
data class AreaDefinition(
    val name: String,
    val area: Area,
    val tags: Set<String>,
    override var stringId: String = name,
    override var extras: Map<String, Any>? = null
) : Extra {
    /**
     * Companion object for `AreaDefinition` that provides predefined constants and utility methods
     * to work with `AreaDefinition` instances.
     */
    companion object {
        /**
         * An empty instance of `AreaDefinition`.
         *
         * Represents a definition with no name, an empty rectangular area, and no tags.
         * Used as a default or placeholder value when no specific area definition is provided.
         */
        val EMPTY = AreaDefinition("", Rectangle(0, 0, 0, 0), emptySet())

        /**
         * Constructs an `AreaDefinition` from a provided name and a mutable map of attributes.
         *
         * The `map` parameter must include the following keys:
         * - `"area"`: An instance of `Area`, which represents the spatial area of the definition.
         * - `"tags"` (optional): A set of `String` tags associated with the area. If not provided, defaults to an empty set.
         *
         * Any remaining entries in the `map` will be stored as extras in the `AreaDefinition`.
         *
         * @param name The name of the area definition.
         * @param map A mutable map containing the attributes required to create the `AreaDefinition`.
         *            The map must include an `"area"` key with an `Area` instance and optionally a `"tags"` key with a `Set<String>`.
         *            Any remaining entries will be considered extras.
         * @return An instance of `AreaDefinition` created using the provided name and attributes from the map.
         */
        @Suppress("UNCHECKED_CAST")
        fun fromMap(name: String, map: MutableMap<String, Any>): AreaDefinition {
            return AreaDefinition(
                name = name,
                area = map.remove("area") as Area,
                tags = (map.remove("tags") as? Set<String>) ?: emptySet(),
                extras = map
            )
        }
    }
}
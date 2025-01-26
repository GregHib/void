package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.GraphicDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * A class responsible for managing and decoding graphic definitions. GraphicDefinitions provides functionalities
 * to store, decode, and retrieve graphic-related data, including managing a collection of `GraphicDefinition`
 * objects and their associated identifiers.
 *
 * This class extends the `DefinitionsDecoder` interface, allowing it to decode definitions from external sources
 * and provides utility methods for working with graphic definitions.
 *
 * @property definitions An array of `GraphicDefinition` objects representing the graphic definitions managed by
 * this class.
 */
class GraphicDefinitions(
    override var definitions: Array<GraphicDefinition>
) : DefinitionsDecoder<GraphicDefinition> {

    /**
     * A mapping of string identifiers to their corresponding integer IDs for graphic definitions.
     *
     * This map serves as a lookup table where each string key represents a unique graphic identifier
     * and the integer value corresponds to its associated graphical ID.
     * It is typically used to decode and manage graphic definitions efficiently by linking string identifiers
     * to their respective numerical representations.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Provides an empty default value for the `GraphicDefinition` type.
     *
     * This method is typically used to return a placeholder or default
     * instance of `GraphicDefinition` when no valid data is available.
     *
     * @return The predefined empty instance of `GraphicDefinition`.
     */
    override fun empty() = GraphicDefinition.EMPTY

    /**
     * Loads and decodes the graphic definitions from the specified YAML file.
     *
     * @param yaml The YAML parser instance used to load the data. Defaults to the global YAML instance.
     * @param path The file path to the YAML file to be decoded. Defaults to the path specified in the settings under "definitions.graphics".
     * @return The instance of [GraphicDefinitions] with the loaded and decoded graphic definitions.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.graphics"]): GraphicDefinitions {
        timedLoad("graphic extra") {
            decode(yaml, path)
        }
        return this
    }

}
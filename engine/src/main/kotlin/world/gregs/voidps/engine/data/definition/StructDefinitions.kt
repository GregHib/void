package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Represents a collection of `StructDefinition` objects, allowing for decoding,
 * management, and manipulation of structured data definitions. This class extends
 * the functionality of `DefinitionsDecoder` and provides additional methods to
 * facilitate the loading and handling of structured definitions.
 *
 * @property definitions The array of `StructDefinition` instances managed by this class.
 */
class StructDefinitions(
    override var definitions: Array<StructDefinition>
) : DefinitionsDecoder<StructDefinition> {

    /**
     * A map associating string identifiers with their corresponding integer IDs.
     *
     * Typically used to map unique names or keys to numerical IDs for efficient lookup or storage.
     * The keys in the map represent the unique string identifiers, while the values represent their associated integer IDs.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Returns an empty `StructDefinition` instance.
     *
     * This method provides a default or placeholder value representing
     * an absence of meaningful data within the context of `StructDefinitions`.
     *
     * @return The static `EMPTY` instance of `StructDefinition`.
     */
    override fun empty() = StructDefinition.EMPTY

    /**
     * Loads structural definitions from the specified YAML configuration and path.
     *
     * @param yaml The YAML configuration to use. Defaults to the result of `get()`.
     * @param path The path within the settings where the structural definitions are located. Defaults to the value of `Settings["definitions.structs"]`.
     * @return The loaded structural definitions as an instance of `StructDefinitions`.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.structs"]): StructDefinitions {
        timedLoad("struct extra") {
            decode(yaml, path)
        }
        return this
    }

}
package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Represents a collection of font definitions. This class extends the functionality for decoding
 * and managing `FontDefinition` objects, allowing them to be loaded and associated with unique identifiers.
 *
 * @property definitions An array of `FontDefinition` objects that are managed by this class.
 */
class FontDefinitions(
    override var definitions: Array<FontDefinition>
) : DefinitionsDecoder<FontDefinition> {

    /**
     * A map linking string identifiers to integer IDs.
     *
     * Used to store and manage mappings between unique string keys and their corresponding integer values
     * for font definitions. Typically utilized to resolve or retrieve font details by their string identifiers.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Provides an empty or default instance of `FontDefinition`.
     *
     * This method is typically used to return a placeholder or default value
     * when no specific font definition is available or required.
     *
     * @return The empty `FontDefinition` instance.
     */
    override fun empty() = FontDefinition.EMPTY

    /**
     * Loads font definitions from a YAML file and processes it into the internal structure.
     *
     * @param yaml An instance of the `Yaml` parser used to parse the input file. Defaults to a globally retrieved instance.
     * @param path The file path to the YAML file containing font definitions. Defaults to the value of `Settings["definitions.fonts"]`.
     * @return The updated `FontDefinitions` instance after loading and decoding the YAML data.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.fonts"]): FontDefinitions {
        timedLoad("font extra") {
            decode(yaml, path)
        }
        return this
    }

}
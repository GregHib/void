package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.JingleDefinition
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * JingleDefinitions is responsible for decoding and managing a collection of JingleDefinition objects
 * from a specified configuration source. It provides functionality for loading definitions from a YAML file
 * and managing their identifiers and mappings.
 */
class JingleDefinitions : DefinitionsDecoder<JingleDefinition> {

    /**
     * An array of `JingleDefinition` objects that holds specific definitions
     * used within the context of a jingle or messaging component.
     * This property must be initialized before use.
     */
    override lateinit var definitions: Array<JingleDefinition>
    /**
     * A map that associates string keys to integer values.
     * This overridden lateinit property allows lazy initialization
     * and is intended to store a collection of IDs, with each ID represented
     * as a key-value pair.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Loads jingle definitions from the specified YAML configuration.
     *
     * @param yaml The YAML configuration to use for loading. Defaults to the output of the `get()` function.
     * @param path The path in the YAML configuration where the jingle definitions are stored. Defaults to the value of `Settings["definitions.jingles"]`.
     * @return The loaded `JingleDefinitions` instance.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.jingles"]): JingleDefinitions {
        timedLoad("jingle definition") {
            decode(yaml, path) { id, key, _ ->
                JingleDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    /**
     * Returns an empty representation of a JingleDefinition.
     *
     * This method provides a standard way to retrieve an empty or placeholder
     * JingleDefinition object, which can be used when no meaningful data is available
     * or no specific configuration is required.
     *
     * @return JingleDefinition.EMPTY, representing an empty JingleDefinition.
     */
    override fun empty() = JingleDefinition.EMPTY

}
package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.yaml.decode
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * The `ClientScriptDefinitions` class is responsible for decoding and managing client script definitions.
 * It extends `DefinitionsDecoder` to handle the specific type `ClientScriptDefinition`.
 */
class ClientScriptDefinitions : DefinitionsDecoder<ClientScriptDefinition> {

    /**
     * An overridden, late-initialized array holding the definitions of
     * client script objects. These objects represent client-side scripting
     * configurations or instructions.
     */
    override lateinit var definitions: Array<ClientScriptDefinition>
    /**
     * A mapping of unique string identifiers to their corresponding numeric IDs.
     *
     * This map is utilized to maintain the relationship between human-readable
     * string IDs and their internal numerical counterparts, which may be used
     * for more efficient lookups or storage.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Loads and decodes client script definitions from the specified YAML configuration file and path.
     *
     * @param yaml the Yaml instance used for decoding; defaults to the result of `get()`.
     * @param path the path to the client scripts definition file; defaults to the value of `Settings["definitions.clientScripts"]`.
     * @return the loaded ClientScriptDefinitions instance.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.clientScripts"]): ClientScriptDefinitions {
        timedLoad("client script definition") {
            decode(yaml, path) { id, key, _ ->
                ClientScriptDefinition(id = id, stringId = key)
            }
        }
        return this
    }

    /**
     * Provides an empty default instance of `ClientScriptDefinition`.
     *
     * This method is used as a fallback or placeholder, returning a static
     * `EMPTY` instance from `ClientScriptDefinition` when no specific
     * instance is available or applicable.
     *
     * @return A predefined `ClientScriptDefinition` instance representing an empty or default state.
     */
    override fun empty() = ClientScriptDefinition.EMPTY

}
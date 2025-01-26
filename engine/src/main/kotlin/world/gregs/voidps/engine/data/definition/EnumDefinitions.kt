package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * A class responsible for decoding and managing EnumDefinitions. It provides functionality
 * to work with EnumDefinitions tied to specific StructDefinitions.
 *
 * @property definitions Array of EnumDefinitions managed by this class.
 * @property structs An instance of StructDefinitions used for structural data retrieval.
 */
class EnumDefinitions(
    override var definitions: Array<EnumDefinition>,
    private val structs: StructDefinitions
) : DefinitionsDecoder<EnumDefinition> {

    /**
     * A map where the keys are string identifiers and the values are integer IDs.
     *
     * This property is used to store a mapping of unique string keys, such as names or identifiers,
     * to their corresponding integers. It functions as a lookup table, providing efficient access
     * to integer IDs based on their string keys.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Retrieves a structured value of type [T] based on the provided parameters.
     *
     * @param id The identifier used to locate the associated enum definition.
     * @param index The index used to retrieve a specific integer from the enum definition.
     * @param param The key used to extract a specific value from the struct definition.
     * @return The value of type [T] retrieved from the struct.
     *
     * @throws NoSuchElementException if the `id`, `index`, or `param` does not match an existing definition.
     * @throws ClassCastException if the value associated with `param` cannot be cast to the type [T].
     */
    fun <T : Any> getStruct(id: String, index: Int, param: String): T {
        val enum = get(id)
        val struct = enum.getInt(index)
        return structs.get(struct)[param]
    }

    /**
     * Retrieves a struct of type T from the provided identifier, index, and parameter,
     * or null if the struct or parameter is not found.
     *
     * @param id The unique identifier used to locate the struct.
     * @param index The index within the struct to retrieve.
     * @param param The specific parameter within the struct to retrieve the value for.
     * @return The value of type T associated with the specified id, index, and param,
     *         or null if the value does not exist.
     */
    fun <T : Any?> getStructOrNull(id: String, index: Int, param: String): T? {
        val enum = get(id)
        val struct = enum.getInt(index)
        return structs.getOrNull(struct)?.getOrNull(param)
    }

    /**
     * Retrieves a structured value of a specified type associated with the given parameters.
     *
     * @param id The unique identifier for retrieving the enum definition.
     * @param index The index to retrieve the specific integer value from the enum definition.
     * @param param The key used to fetch the associated structured value from the struct definition.
     * @param default The default value to return if the key does not exist or the value cannot be cast.
     * @return The structured value of type T associated with the given parameters, or the default value if not found.
     */
    fun <T : Any> getStruct(id: String, index: Int, param: String, default: T): T {
        val enum = get(id)
        val struct = enum.getInt(index)
        return structs.get(struct)[param, default]
    }

    /**
     * Loads the EnumDefinitions from the specified YAML configuration.
     *
     * @param yaml The YAML instance to be used for loading. Defaults to the result of get().
     * @param path The path in the settings where the enum definitions are located. Defaults to Settings["definitions.enums"].
     * @return The loaded EnumDefinitions.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.enums"]): EnumDefinitions {
        timedLoad("enum extra") {
            decode(yaml, path)
        }
        return this
    }

    /**
     * Returns an empty instance of [EnumDefinition].
     *
     * This method is used to provide a default or placeholder value
     * when no specific enumeration definition is available.
     *
     * @return The constant `EnumDefinition.EMPTY`.
     */
    override fun empty() = EnumDefinition.EMPTY

}
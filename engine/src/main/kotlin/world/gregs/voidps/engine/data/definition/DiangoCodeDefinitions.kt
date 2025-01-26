package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.DiangoCodeDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * Represents a collection of Diango code definitions which are used
 * to manage configurations for retrieving data related to Diango codes.
 */
class DiangoCodeDefinitions {

    /**
     * A map containing all the DiangoCodeDefinitions keyed by their corresponding code strings.
     *
     * This variable is used within the DiangoCodeDefinitions class to store and manage
     * the mapping of Django codes to their respective definitions. It is initialized
     * later through the `load` method.
     */
    private lateinit var definitions: Map<String, DiangoCodeDefinition>

    /**
     * Retrieves a definition based on the provided code. If no valid definition is found,
     * a default empty definition is returned.
     *
     * @param code the code used to look up the corresponding definition
     * @return the definition associated with the given code, or an empty definition if none is found
     */
    fun get(code: String) = getOrNull(code) ?: DiangoCodeDefinition.EMPTY

    /**
     * Retrieves the value associated with the provided code from the definitions map.
     * If the code is not present in the map, returns null.
     *
     * @param code The key for which the corresponding value is to be retrieved from the definitions map.
     * @return The value associated with the specified key or null if the key is not found.
     */
    fun getOrNull(code: String) = definitions[code]

    /**
     * Loads DiangoCodeDefinitions from the specified YAML configuration file.
     *
     * @param yaml An instance of the Yaml parser to read the configuration. If not provided, a default instance will be used.
     * @param path A string representing the path to the YAML file. Defaults to the path defined in the Settings for Diango codes.
     * @param itemDefinitions Optional parameter containing definitions of items. Used to validate item identifiers during loading.
     * @return The loaded DiangoCodeDefinitions containing parsed configuration details.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.diangoCodes"], itemDefinitions: ItemDefinitions? = null): DiangoCodeDefinitions {
        timedLoad("diango code definition") {
            val config = object : YamlReaderConfiguration(2, 2) {
                /**
                 * Adds an item to the specified mutable list, converting the value to an Item object if necessary.
                 *
                 * @param list The mutable list to which the item will be added.
                 * @param value The value to be added to the list. If it is a map, it is transformed into an Item.
                 * @param parentMap Optional parent map identifier used for additional processing.
                 */
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    super.add(list, if (value is Map<*, *>) {
                        val id = value["item"] as String
                        if (itemDefinitions != null && !itemDefinitions.contains(id)) {
                            logger.warn { "Invalid diango item id: $id" }
                        }
                        Item(id, value["amount"] as? Int ?: 1)
                    } else {
                        Item(value as String, amount = 1)
                    }, parentMap)
                }
                /**
                 * Sets a value in the provided map according to the given key and handles specific conditions
                 * based on the key value and indentation level.
                 *
                 * If the key is "<<", the method merges the given value (expected to be a map) into the provided map.
                 * If the indentation level (`indent`) is 0, the value is wrapped as a `DiangoCodeDefinition` before being set.
                 * Otherwise, the value is set without modification.
                 *
                 * @param map The mutable map to be updated.
                 * @param key The key under which the value should be stored or merged.
                 * @param value The value to be set or merged into the map.
                 * @param indent The indentation level determining how the value should be processed.
                 * @param parentMap An optional parameter representing the parent map structure, may be null.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        super.set(map, key, DiangoCodeDefinition(value as Map<String, Any>), indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            val definitions = yaml.load<Any>(path, config) as Map<String, DiangoCodeDefinition>
            this.definitions = definitions
            definitions.size
        }
        return this
    }

    /**
     * Companion object for the `DiangoCodeDefinitions` class.
     *
     * Provides utility functions or shared behavior related to the scope of `DiangoCodeDefinitions`.
     */
    companion object {
        /**
         * Logger instance used for logging within the `DiangoCodeDefinitions` class.
         * Provides a way to output debug or informational messages during the execution
         * of the class's functions, such as loading or retrieving definitions.
         */
        private val logger = InlineLogger()
    }

}
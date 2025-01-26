package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.SpellDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * The `SpellDefinitions` class is responsible for managing a collection of spell definitions.
 * It provides methods to retrieve and load spell definitions from a configuration source.
 */
class SpellDefinitions {

    /**
     * A map that holds spell definitions, where the key is a unique identifier (String)
     * and the value is an instance of [SpellDefinition].
     *
     * This property is initialized through a loading mechanism and contains all available spell definitions.
     * It is expected to be used for retrieving specific spell configurations within the system.
     */
    private lateinit var definitions: Map<String, SpellDefinition>

    /**
     * Retrieves a `SpellDefinition` from the `definitions` map using the provided key.
     * If the key does not exist in the map, a default `SpellDefinition` is returned.
     *
     * @param key The key used to locate the desired `SpellDefinition` in the map.
     * @return The `SpellDefinition` corresponding to the key, or a default instance if the key is not found.
     */
    fun get(key: String) = definitions[key] ?: SpellDefinition()

    /**
     * Loads spell definitions from a YAML configuration file.
     *
     * @param yaml The YAML parser used for loading the file. Defaults to a provided instance.
     * @param path The path to the YAML file containing the spell definitions. Defaults to a specified configuration path.
     * @return An instance of [SpellDefinitions] containing the loaded spell definitions.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.spells"]): SpellDefinitions {
        timedLoad("spell definition") {
            val definitions = Object2ObjectOpenHashMap<String, SpellDefinition>()
            val config = object : YamlReaderConfiguration() {
                /**
                 * Overrides the behavior for setting values in a map, with custom logic for handling certain keys and conditions.
                 *
                 * @param map The map in which the key-value pair will be set.
                 * @param key The key to be set in the map.
                 * @param value The value to be associated with the key in the map.
                 * @param indent The level of indentation, which determines how the key and value should be processed.
                 * @param parentMap The containing map's key, if applicable, to provide contextual information during processing.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        definitions[key] = if (value is Map<*, *>) {
                            SpellDefinition(key, value as MutableMap<String, Any>)
                        } else {
                            SpellDefinition(stringId = key)
                        }
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

}
package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.PatrolDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * A class representing a collection of patrol definitions.
 * This class provides functionality for loading patrol definitions
 * from a YAML file and accessing specific definitions by their keys.
 */
class PatrolDefinitions {

    /**
     * A map containing patrol definitions, associating a unique string identifier to each [PatrolDefinition].
     *
     * Utilized for storing and managing the collection of patrol definitions loaded from external data sources.
     */
    private lateinit var definitions: Map<String, PatrolDefinition>

    /**
     * Retrieves the value associated with the given key from the definitions map.
     * If the key is not found, returns a new instance of `PatrolDefinition`.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the specified key or a new `PatrolDefinition` if the key is not found.
     */
    fun get(key: String) = definitions[key] ?: PatrolDefinition()

    /**
     * Loads patrol definitions from a YAML configuration file and populates the `definitions` map.
     *
     * @param yaml The YAML parsing instance to be used for loading, with a default value obtained from `get()`.
     * @param path The path to the YAML file that contains the patrol definitions, defaulting to the value in `Settings["definitions.patrols"]`.
     * @return The updated `PatrolDefinitions` instance containing the loaded patrol definitions.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.patrols"]): PatrolDefinitions {
        timedLoad("patrol definition") {
            val definitions = Object2ObjectOpenHashMap<String, PatrolDefinition>()
            val config = object : YamlReaderConfiguration() {
                /**
                 * Configures the specified map by either adding or updating a key-value pair or merging another map,
                 * depending on the provided key and indentation level.
                 *
                 * @param map The mutable map to modify.
                 * @param key The key to set in the map. If the key equals "<<", the value is expected to be a map,
                 *            which will be merged with the input map.
                 * @param value The value to associate with the specified key. If the `indent` is 0 and the value
                 *              is a map, it will be used to create a `PatrolDefinition`.
                 * @param indent The indentation level, which determines whether the operation is handled directly
                 *               or delegated to the parent class.
                 * @param parentMap The identifier of the parent map, which can be null. Used for maintaining
                 *                  structure in contexts involving nested maps.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        definitions[key] = if (value is Map<*, *>) {
                            PatrolDefinition(key, value as MutableMap<String, Any>)
                        } else {
                            PatrolDefinition(stringId = key)
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
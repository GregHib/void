package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.WeaponAnimationDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * This class is responsible for managing weapon animation definitions.
 * It provides functionality to load, retrieve, and query animation definitions
 * for weapons from a configuration source, typically in YAML format.
 */
class WeaponAnimationDefinitions {

    /**
     * A map containing weapon animation definitions, where the key is a unique string identifier,
     * and the value is the corresponding weapon animation definition.
     *
     * This map is populated during the loading process from an external configuration
     * (e.g., YAML file) and provides quick access to the weapon animation definitions
     * by their identifiers.
     */
    private lateinit var definitions: Map<String, WeaponAnimationDefinition>

    /**
     * Retrieves the value associated with the specified key or returns a default value if the key is not found.
     *
     * @param key the key whose associated value is to be returned.
     * @return the value corresponding to the given key, or `WeaponAnimationDefinition.EMPTY` if no value is found.
     */
    fun get(key: String) = getOrNull(key) ?: WeaponAnimationDefinition.EMPTY

    /**
     * Retrieves the value associated with the provided key from the definitions map.
     * If the key does not exist in the map, this function returns null.
     *
     * @param key The key whose associated value is to be retrieved.
     * @return The value associated with the key, or null if the key is not found.
     */
    fun getOrNull(key: String) = definitions[key]

    /**
     * Loads weapon animation definitions from the specified YAML configuration file.
     *
     * @param yaml The YAML reader instance to load data. Defaults to an instance returned by `get()`.
     * @param path The path to the configuration file containing weapon animation definitions. Defaults to the value of `Settings["definitions.weapons.animations"]`.
     * @return The loaded `WeaponAnimationDefinitions` containing the parsed weapon animation data.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.weapons.animations"]): WeaponAnimationDefinitions {
        timedLoad("weapon animation definition") {
            val definitions = Object2ObjectOpenHashMap<String, WeaponAnimationDefinition>()
            val config = object : YamlReaderConfiguration() {
                /**
                 * Sets a value in the provided map or processes it based on the given parameters.
                 *
                 * @param map The mutable map where the key-value pair may be set.
                 * @param key The key associated with the value to be set.
                 * @param value The value to be set in the map or processed.
                 * @param indent Indicates the level of indentation or processing required.
                 * @param parentMap An optional parameter representing the parent map, if applicable.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (indent == 0) {
                        definitions[key] = if (value is Map<*, *>) {
                            WeaponAnimationDefinition.fromMap(key, value as MutableMap<String, Any>)
                        } else {
                            WeaponAnimationDefinition(stringId = key)
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
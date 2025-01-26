package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.HuntModeDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * A class that defines and manages hunt mode configurations. This class supports loading
 * and retrieving different hunt modes by their names.
 */
class HuntModeDefinitions {

    /**
     * A map containing hunt mode definitions, keyed by their respective names.
     *
     * Each entry represents a specific hunting mode defined by an instance of [HuntModeDefinition].
     * These definitions control various behavior parameters and checks for hunting targets in the system.
     *
     * The map is populated by loading definitions from external sources during initialization or runtime.
     */
    private lateinit var modes: Map<String, HuntModeDefinition>

    /**
     * Retrieves the HuntModeDefinition associated with the specified name.
     *
     * @param name The name of the hunt mode to retrieve.
     * @return The HuntModeDefinition corresponding to the given name.
     */
    fun get(name: String): HuntModeDefinition {
        return modes.getValue(name)
    }

    /**
     * Loads hunt mode definitions from the specified YAML configuration.
     *
     * @param yaml An optional Yaml instance to parse the configuration. Defaults to the result of `get()`.
     * @param path The file path or configuration path to load the YAML definitions from. Defaults to `Settings["definitions.huntModes"]`.
     * @return Returns an instance of `HuntModeDefinitions` with loaded and parsed hunt modes.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.huntModes"]): HuntModeDefinitions {
        timedLoad("hunt mode") {
            val config = object : YamlReaderConfiguration(2, 2) {
                /**
                 * Sets a key-value pair into the provided mutable map with additional handling for nested merge operations and
                 * specialized value processing based on the indent level.
                 *
                 * @param map The mutable map where the key-value pair or merged map will be set.
                 * @param key The key for the value to set or merge into the map.
                 * @param value The value to set or merge; can be a map or a processed object depending on the key or indent.
                 * @param indent The indentation level to determine how the value should be processed. If 0, the value is processed differently.
                 * @param parentMap The parent map context, if applicable, which might be used by the caller to determine behavior.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 0) {
                        super.set(map, key, HuntModeDefinition.fromMap(value as Map<String, Any>), indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            this.modes = yaml.load(path, config)
            modes.size
        }
        return this
    }

}
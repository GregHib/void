package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.data.Pickable
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.data.definition.data.Tree
import world.gregs.voidps.engine.data.yaml.DefinitionConfig
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * A container and manager for handling an array of `ObjectDefinition` objects.
 * This class provides functionality to decode, retrieve, and load object definitions,
 * allowing for configuration and customization of the objects using external YAML files.
 *
 * @property definitions An array of `ObjectDefinition` objects representing the core data structure.
 */
class ObjectDefinitions(
    override var definitions: Array<ObjectDefinition>
) : DefinitionsDecoder<ObjectDefinition> {

    /**
     * A map linking unique string identifiers to integer IDs.
     *
     * This is used to store and retrieve mappings for object definitions, where each key is a string
     * representing the name or identifier, and each value is an integer ID associated with it.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Retrieves the `ObjectDefinition` corresponding to the specified identifier.
     *
     * @param id The unique identifier of the `ObjectDefinition` to retrieve.
     * @return The `ObjectDefinition` associated with the given identifier.
     */
    fun getValue(id: Int): ObjectDefinition {
        return definitions[id]
    }

    /**
     * Provides an empty instance of `ObjectDefinition`.
     *
     * This function returns a predefined constant `ObjectDefinition.EMPTY`, which represents
     * a placeholder or default object definition with no specific data. It is typically used
     * when no actual data is available or required for a given context.
     *
     * @return The empty instance of `ObjectDefinition`.
     */
    override fun empty() = ObjectDefinition.EMPTY

    /**
     * Loads object definitions from a YAML file and initializes object-specific configurations.
     *
     * @param yaml An instance of the Yaml class, used to parse the YAML file. Defaults to the result of the `get` function.
     * @param path The file path to the YAML definitions. Defaults to the value from `Settings["definitions.objects"]`.
     * @return The current instance of ObjectDefinitions, loaded with parsed data and mappings.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.objects"]): ObjectDefinitions {
        timedLoad("object extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = object : DefinitionConfig<ObjectDefinition>(ids, definitions) {
                /**
                 * Sets a key-value pair in the provided map based on the given parameters. Depending on the
                 * provided key, value, and indentation level, the method applies different transformations
                 * or types/mappings to the value before setting it into the map.
                 *
                 * @param map The mutable map where the key-value pair should be set.
                 * @param key The key to be used in the map.
                 * @param value The value to be associated with the key in the map.
                 * @param indent The indentation level that determines how the key-value pair is processed.
                 * @param parentMap An optional parent map reference for further processing, if required.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                    } else if (indent == 1) {
                        super.set(map, key,
                            when (key) {
                                "pickable" -> Pickable(value as Map<String, Any>)
                                "woodcutting" -> Tree(value as Map<String, Any>)
                                "mining" -> Rock(value as Map<String, Any>)
                                else -> value
                            }, indent, parentMap)
                    } else {
                        super.set(map, key, when (key) {
                            "chance", "hatchet_low_dif", "hatchet_high_dif", "respawn" -> (value as String).toIntRange()
                            else -> value
                        }, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            ids.size
        }
        return this
    }
}
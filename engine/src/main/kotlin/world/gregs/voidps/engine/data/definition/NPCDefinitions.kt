package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.data.Pocket
import world.gregs.voidps.engine.data.definition.data.Spot
import world.gregs.voidps.engine.data.yaml.DefinitionConfig
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReader

/**
 * Represents a decoder for NPC (Non-Player Character) definitions. This class is responsible for
 * reading and managing an array of `NPCDefinition` objects, and optionally loading them from
 * a YAML configuration file.
 *
 * @property definitions An array of `NPCDefinition` instances representing the NPCs.
 */
class NPCDefinitions(
    override var definitions: Array<NPCDefinition>
) : DefinitionsDecoder<NPCDefinition> {

    /**
     * A map containing identifiers for NPC definitions.
     *
     * The key represents a string identifier for the NPC, and the value is an integer
     * corresponding to the NPC's unique internal ID.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Provides an empty NPC definition as a placeholder or default instance.
     *
     * Returns a predefined constant representing an NPC definition with no data or properties.
     * Useful for scenarios where an NPC definition is required but no specific data is available.
     */
    override fun empty() = NPCDefinition.EMPTY

    /**
     * Loads NPC definitions from the specified YAML file at the given path.
     *
     * This method parses and configures NPC definitions using a custom DefinitionConfig class,
     * adapting specific configurations based on the provided YAML data.
     *
     * @param yaml The YAML parser to use for loading data. Defaults to the global YAML instance.
     * @param path The file path to the YAML data for the NPC definitions. Defaults to the path specified in application settings.
     * @return The updated instance of NPCDefinitions containing the loaded data.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.npcs"]): NPCDefinitions {
        timedLoad("npc extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = object : DefinitionConfig<NPCDefinition>(ids, definitions) {
                /**
                 * Sets a value in the provided map based on the key, indentation level, and other contextual parameters.
                 * Overrides the behavior for the key "chance" when the indentation level is 2, calling a specialized method to process the value.
                 *
                 * @param reader An instance of YamlReader used to parse and read the YAML content.
                 * @param map A mutable map where the key-value pair should be set or modified.
                 * @param key The key in the YAML map for which the value will be set.
                 * @param indent The current indentation level within the YAML structure, used to determine parsing logic.
                 * @param indentOffset The offset from the base indentation level, providing additional context for parsing.
                 * @param withinMap The identifier or name of the current nested map being processed, if applicable.
                 * @param parentMap The identifier or name of the immediate parent map within the YAML structure, if applicable.
                 */
                override fun setMapValue(reader: YamlReader, map: MutableMap<String, Any>, key: String, indent: Int, indentOffset: Int, withinMap: String?, parentMap: String?) {
                    if (indent == 2 && key == "chance") {
                        set(map, key, reader.readIntRange(), indent, parentMap)
                    } else {
                        super.setMapValue(reader, map, key, indent, indentOffset, withinMap, parentMap)
                    }
                }

                /**
                 * Sets a key-value pair in the provided map. Handles special cases for merging maps,
                 * creating custom objects, and delegating behavior based on the indent parameter.
                 *
                 * @param map The mutable map where the key-value pair will be set.
                 * @param key The key to set in the map.
                 * @param value The value associated with the key.
                 * @param indent The indentation level, used to determine specific behaviors.
                 * @param parentMap An optional string identifier for the parent map, if applicable.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "<<") {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    if (indent == 1) {
                        super.set(
                            map, key, when (key) {
                                "pickpocket" -> Pocket(value as Map<String, Any>)
                                "fishing" -> Object2ObjectOpenHashMap((value as Map<String, Map<String, Any>>).mapValues { Spot(it.value) })
                                else -> value
                            }, indent, parentMap
                        )
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            ids.size
        }
        return this
    }

}
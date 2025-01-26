package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.GearDefinition
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * The `GearDefinitions` class is responsible for managing and loading gear definitions
 * from a specified YAML configuration file. The gear definitions are stored as a mapping
 * of gear styles to their respective lists of gear definitions.
 */
class GearDefinitions {

    /**
     * A map that holds gear definitions categorized by a string key.
     *
     * Each key in the map represents a specific style or category, and the value
     * is a mutable list of `GearDefinition` objects associated with that category.
     * This structure allows the organization and retrieval of gear definitions
     * based on their associated style.
     */
    private lateinit var definitions: Map<String, MutableList<GearDefinition>>

    /**
     * Retrieves a list of GearDefinition objects associated with the specified style.
     *
     * @param style the style identifier used to fetch the corresponding gear definitions
     * @return a list of GearDefinition objects matching the provided style; an empty list if none are found
     */
    fun get(style: String): List<GearDefinition> = definitions[style] ?: emptyList()

    /**
     * Loads gear definitions from the specified YAML file path using the provided YAML configuration.
     *
     * This method uses a custom `YamlReaderConfiguration` to process the YAML data into
     * `GearDefinitions` by parsing equipment, inventory, and other definitions accordingly.
     *
     * @param yaml The YAML parser to use for loading the configuration. Defaults to the result of `get()`.
     * @param path The file path to the YAML configuration file. Defaults to `Settings["definitions.gearSets"]`.
     * @return The loaded `GearDefinitions` object containing the parsed gear data.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.gearSets"]): GearDefinitions {
        timedLoad("gear definition") {
            var count = 0
            val config = object : YamlReaderConfiguration() {
                /**
                 * Adds a value to the specified list based on the type of value and the parent map context.
                 *
                 * @param list The mutable list to which the value will be added.
                 * @param value The value to be added to the list. The type of this value can vary and will be processed accordingly.
                 * @param parentMap A string identifier representing the context or type of parent map, used to determine how to process the value.
                 */
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    if (parentMap == "inventory") {
                        value as Map<String, Any>
                        val id = value["id"]
                        if (id is List<*>) {
                            val amount = value["amount"] as? Int ?: 1
                            val subList = createList()
                            for (i in id as List<String>) {
                                subList.add(Item(i, amount))
                            }
                            super.add(list, subList, parentMap)
                        } else {
                            val subList = createList()
                            subList.add(Item(id as String, value["amount"] as? Int ?: 1))
                            super.add(list, subList, parentMap)
                        }
                    } else if (parentMap == "equipment") {
                        value as Map<String, List<Item>>
                        super.add(list, Object2ObjectOpenHashMap(value.mapKeys { EquipSlot.valueOf(it.key.toSentenceCase()) }), parentMap)
                    } else if (value is Map<*, *> && value.containsKey("id")) {
                        val id = value["id"] as String
                        val item = Item(id, value["amount"] as? Int ?: 1)
                        super.add(list, item, parentMap)
                    } else if (parentMap != "id" && value is Map<*, *>) {
                        count++
                        super.add(list, GearDefinition(parentMap!!, value as Map<String, Any>), parentMap)
                    } else {
                        super.add(list, value, parentMap)
                    }
                }

                /**
                 * Sets a key-value pair in the provided map with custom handling for specific keys.
                 *
                 * @param map The map where the key-value pair will be set.
                 * @param key The key to set in the map.
                 * @param value The value associated with the key.
                 * @param indent The current indentation level in the YAML structure.
                 * @param parentMap The name of the parent map, if applicable.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    super.set(map, key, when (key) {
                        "levels" -> (value as String).toIntRange()
                        else -> value
                    }, indent, parentMap)
                }
            }
            this.definitions = yaml.load(path, config)
            count
        }
        return this
    }
}
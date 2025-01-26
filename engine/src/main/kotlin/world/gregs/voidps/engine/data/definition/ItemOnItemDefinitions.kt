package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.yaml.DefinitionIdsConfig
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Represents a collection of item-on-item interaction definitions.
 * These definitions describe how two items interact with each other in a specific context.
 */
class ItemOnItemDefinitions {

    /**
     * Stores a mapping between a string key and a list of `ItemOnItemDefinition` objects.
     * This variable is initialized later using the `lateinit` modifier, meaning it must
     * be assigned a value before its first use. It is private and cannot be accessed outside
     * of its containing class.
     */
    private lateinit var definitions: Map<String, List<ItemOnItemDefinition>>

    /**
     * Retrieves a list of results based on the provided items. If no results are found,
     * it returns an empty list.
     *
     * @param one The first item to be used in the retrieval process.
     * @param two The second item to be used in the retrieval process.
     * @return A list of results corresponding to the provided items or an empty list if no results are found.
     */
    fun get(one: Item, two: Item) = getOrNull(one, two) ?: emptyList()

    /**
     * Retrieves a value from the `definitions` map based on the combined identifier
     * of the given `one` and `two` items. If no value is found for the first identifier,
     * it tries with the reversed identifier.
     *
     * @param one The first item used to construct the identifier.
     * @param two The second item used to construct the identifier.
     * @return The value associated with the identifier, or null if no value is found.
     */
    fun getOrNull(one: Item, two: Item) = definitions[id(one, two)] ?: definitions[id(two, one)]

    /**
     * Checks if the specified combination of two items exists in the definitions map.
     *
     * @param one the first item to check for containment.
     * @param two the second item to check for containment.
     * @return `true` if the combination of the two items is present in the definitions map, either as (one, two) or (two, one), `false` otherwise.
     */
    fun contains(one: Item, two: Item) = definitions.containsKey(id(one, two)) || definitions.containsKey(id(two, one))

    /**
     * Loads and processes item-on-item definitions from a YAML configuration file.
     *
     * @param yaml An instance of the Yaml parser to use for loading the configuration. Defaults to the result of `get()`.
     * @param path The file path of the YAML configuration file defining the item-on-item interactions. Defaults to the value specified in the `Settings["definitions.itemOnItem"]
     * `.
     * @param itemDefinitions The collection of item definitions to validate against when processing the YAML data. Defaults to the result of `get()`.
     * @return The `ItemOnItemDefinitions` instance populated with the loaded definitions.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.itemOnItem"], itemDefinitions: ItemDefinitions = get()): ItemOnItemDefinitions {
        timedLoad("item on item definition") {
            val definitions = Object2ObjectOpenHashMap<String, MutableList<ItemOnItemDefinition>>()
            var count = 0
            val config = object : DefinitionIdsConfig() {
                /**
                 * Adds an item to the provided list, converting input values into the appropriate representation.
                 *
                 * @param list The mutable list to which the item will be added.
                 * @param value The value to be added, which may be a Map or a String representing the item.
                 * @param parentMap An optional reference to a parent map, used for context during addition.
                 */
                override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                    super.add(list, if (value is Map<*, *>) {
                        val id = value["item"] as String
                        if (!itemDefinitions.contains(id)) {
                            logger.warn { "Invalid item-on-item id: $id" }
                        }
                        Item(id, value["amount"] as? Int ?: 1)
                    } else {
                        Item(value as String, amount = 1)
                    }, parentMap)
                }

                /**
                 * Overrides the parent set method to process and store item definitions or delegate to the super method depending on the indent level.
                 * At an indent level of 0, processes the map to create and add item-on-item definitions for various requirements.
                 * At different indent levels, forwards the data to the super set method with specific handling for certain keys.
                 *
                 * @param map The map being modified or updated.
                 * @param key The key in the map that corresponds to the value being set.
                 * @param value The new value being set in the map, which can vary in type depending on the key.
                 * @param indent The level of indentation for the current operation, determining the processing logic.
                 * @param parentMap An optional parent map name, typically used for contextual or hierarchical operations.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (indent == 0) {
                        val definition = ItemOnItemDefinition(value as Map<String, Any>)
                        val usable = definition.requires.toMutableList()
                        usable.addAll(definition.one)
                        usable.addAll(definition.remove)
                        for (a in usable.indices) {
                            for (b in usable.indices) {
                                if (a != b) {
                                    val one = usable[a]
                                    val two = usable[b]
                                    val list = definitions.getOrPut(id(one, two)) { ObjectArrayList(2) }
                                    if (!list.contains(definition)) {
                                        list.add(definition)
                                    }
                                }
                            }
                        }
                        count++
                    } else {
                        super.set(map, key, when (key) {
                            "skill" -> Skill.valueOf((value as String).toSentenceCase())
                            "chance" -> (value as String).toIntRange()
                            else -> value
                        }, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            this.definitions = definitions
            count
        }
        return this
    }

    /**
     * Companion object for the outer class, providing additional utilities and helper methods.
     */
    companion object {
        /**
         * A logger instance used for logging messages within the scope of this class or file.
         * This logger is an instance of `InlineLogger`, which allows for inline logging functionality.
         * It is declared as a private property, restricting its visibility to the containing scope.
         */
        private val logger = InlineLogger()
        /**
         * Combines the IDs of two Item objects into a single string, separated by an ampersand.
         *
         * @param one The first Item object.
         * @param two The second Item object.
         * @return A string combining the IDs of the two Item objects separated by "&".
         */
        private fun id(one: Item, two: Item): String = "${one.id}&${two.id}"
    }

}
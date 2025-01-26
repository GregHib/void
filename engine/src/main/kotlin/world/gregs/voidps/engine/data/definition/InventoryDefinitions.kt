package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.yaml.DefinitionConfig
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml

/**
 * Represents the definitions for inventories in the system. This class handles the loading
 * and decoding of inventory-related definitions from configuration files.
 *
 * @property definitions Array of `InventoryDefinition` objects that define the inventory details.
 */
class InventoryDefinitions(
    override var definitions: Array<InventoryDefinition>
) : DefinitionsDecoder<InventoryDefinition> {

    /**
     * A map storing unique identifiers as keys and their associated integer values.
     * The keys are of type `String`, and the corresponding values are of type `Int`.
     * This variable is marked as `lateinit`, which means it will be initialized later.
     * It is overridden from a base class or interface where it is declared.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Overrides the `empty` method to return a predefined constant representing
     * an empty inventory definition. This method provides a standard way to
     * access an empty or default state for the inventory definition.
     *
     * @return An instance of `InventoryDefinition.EMPTY` representing an empty inventory.
     */
    override fun empty() = InventoryDefinition.EMPTY

    /**
     * Loads inventory definitions from the provided YAML configuration file.
     *
     * @param yaml The YAML parser instance to use for loading the configurations. Defaults to the result of `get()` function.
     * @param path The path to the YAML file containing the inventory definitions. Defaults to the value of `Settings["definitions.inventories"]`.
     * @param itemDefs The item definitions used for resolving item IDs and data. Defaults to the result of `get()` function.
     * @return The loaded inventory definitions.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.inventories"], itemDefs: ItemDefinitions = get()): InventoryDefinitions {
        timedLoad("inventory extra") {
            val ids = Object2IntOpenHashMap<String>()
            val config = object : DefinitionConfig<InventoryDefinition>(ids, definitions) {
                /**
                 * Overrides the set method to modify the behavior when the key is "defaults" and
                 * the value is a list. Processes the list to update specific fields of a definition
                 * object if conditions are met, then delegates to the superclass method for other cases.
                 *
                 * @param map A mutable map containing string keys and arbitrary values. Represents a
                 *            structure to be manipulated.
                 * @param key A string key used to identify the data to be processed or modified
                 *            within the map.
                 * @param value The value associated with the key in the map, which may be manipulated
                 *              if specific conditions are met.
                 * @param indent The current indentation level, which may be relevant for structured
                 *               data processing.
                 * @param parentMap An optional string reference to the parent map, allowing access to
                 *                  higher-level context if needed.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "defaults" && value is List<*>) {
                        val id = map["id"] as Int
                        value as List<Map<String, Int>>
                        if (id !in definitions.indices) {
                            return
                        }
                        val def = definitions[id]
                        def.length = map["length"] as? Int ?: def.length
                        def.ids = IntArray(def.length) { itemDefs.get(value.getOrNull(it)?.keys?.first() ?: "").id }
                        def.amounts = IntArray(def.length) { value.getOrNull(it)?.values?.first() ?: 0 }
                    }
                    super.set(map, key, value, indent, parentMap)
                }
            }
            yaml.load<Any>(path, config)
            this.ids = ids
            ids.size
        }
        return this
    }
}

/**
 * Retrieves a list of string identifiers for items in the inventory.
 *
 * @return A list of string IDs representing the items in the inventory.
 *         If the inventory is null or empty, an empty list is returned.
 */
fun InventoryDefinition.items(): List<String> {
    val defs: ItemDefinitions = get()
    return ids?.map { defs.get(it).stringId } ?: emptyList()
}
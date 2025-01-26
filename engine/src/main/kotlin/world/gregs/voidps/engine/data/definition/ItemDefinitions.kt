package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.data.*
import world.gregs.voidps.engine.data.yaml.DefinitionConfig
import world.gregs.voidps.engine.entity.character.player.equip.EquipType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.ItemKept
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReader

/**
 * A class representing definitions for items. It provides decoding capabilities
 * for item definitions and operations to manage and load them.
 *
 * @property definitions An array of `ItemDefinition` objects representing item details.
 * @property size The total number of item definitions available.
 */
class ItemDefinitions(
    override var definitions: Array<ItemDefinition>
) : DefinitionsDecoder<ItemDefinition> {

    /**
     * Represents the size of a collection or dataset.
     * Stores the total number of elements in the collection.
     * The value is derived from the size of the `definitions` collection.
     */
    val size: Int = definitions.size

    /**
     * A map that associates string keys with integer values, representing unique identifiers.
     * The variable is declared using `lateinit` to allow for deferred initialization and
     * is marked as `override` to indicate it overrides a property in a supertype.
     */
    override lateinit var ids: Map<String, Int>

    /**
     * Indicates an overridden method that provides an empty state or representation
     * of an `ItemDefinition`.
     *
     * This method returns a constant value representing an "empty" `ItemDefinition`
     * as defined by `ItemDefinition.EMPTY`.
     *
     * @return An empty instance of `ItemDefinition`.
     */
    override fun empty() = ItemDefinition.EMPTY

    /**
     * Loads item definitions using the provided YAML configuration and path.
     *
     * @param yaml The YAML configuration to load. Defaults to the result of the `get()` function.
     * @param path The path to the definitions file within the YAML configuration. Defaults to the value of `Settings["definitions.items"]`.
     * @return The loaded item definitions as an `ItemDefinitions` object.
     */
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.items"]): ItemDefinitions {
        timedLoad("item extra") {
            val equipment = IntArray(definitions.size) { -1 }
            var index = 0
            for (def in definitions) {
                if (def.primaryMaleModel >= 0 || def.primaryFemaleModel >= 0) {
                    equipment[def.id] = index++
                }
            }
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = CustomConfig(equipment, ids, definitions)
            yaml.load<Any>(path, config)
            ids.size
        }
        return this
    }

    /**
     * CustomConfig is a specialized configuration class responsible for managing the
     * properties and attributes of game items. It extends the DefinitionConfig class and
     * provides implementations for handling custom logic specific to game mechanics.
     *
     * This class overrides methods for data parsing and transformation, offering support
     * for a variety of game-related attributes and types such as equipment, crafting,
     * and resource handling (e.g., pottery, smelting, fishing).
     *
     * The class also provides specific handling for nested map structures, range-based
     * values, and object transformations based on the indentation levels and keys
     * encountered during YAML processing.
     *
     * @property equipment Array of integers representing equipment configuration
     * @property ids Mutable map linking string keys to integer indices
     * @property definitions Array of ItemDefinition objects containing metadata
     * for corresponding game items
     */
    @Suppress("UNCHECKED_CAST")
    private class CustomConfig(
        private val equipment: IntArray,
        ids: MutableMap<String, Int>,
        definitions: Array<ItemDefinition>
    ) : DefinitionConfig<ItemDefinition>(ids, definitions) {
        /**
         * Sets a value in the specified map based on the parameters provided and specific conditions,
         * delegating to the superclass or handling custom logic for specific cases.
         *
         * @param reader The YamlReader instance used to parse and retrieve values.
         * @param map The mutable map where the key-value pair will be set or modified.
         * @param key The key in the map to associate the value with.
         * @param indent The current indentation level of the YAML being read.
         * @param indentOffset The offset to account for in the indentation level.
         * @param withinMap Optional string indicating the context within a nested map, if applicable.
         * @param parentMap Optional string indicating the parent map, if applicable.
         */
        override fun setMapValue(reader: YamlReader, map: MutableMap<String, Any>, key: String, indent: Int, indentOffset: Int, withinMap: String?, parentMap: String?) {
            if (indent > 1 && parentMap == "pottery") {
                val value = reader.value(indentOffset, withinMap)
                super.set(map, key, Pottery.Ceramic(value as Map<String, Any>), indent, parentMap)
            } else if (indent == 1 && key == "heals" || key == "chance" || parentMap == "chances") {
                set(map, key, reader.readIntRange(), indent, parentMap)
            } else {
                super.setMapValue(reader, map, key, indent, indentOffset, withinMap, parentMap)
            }
        }

        /**
         * Sets a value in the specified map with additional handling for specific keys and definitions.
         *
         * @param map The mutable map where the value will be set.
         * @param key The key under which the value will be set. Special handling applies to keys ending with "_lent".
         * @param id The definition ID used to determine the appropriate behavior and extras.
         * @param extras An optional map of additional properties to include when setting the value.
         */
        override fun set(map: MutableMap<String, Any>, key: String, id: Int, extras: Map<String, Any>?) {
            if (key.endsWith("_lent") && id in definitions.indices) {
                val def = definitions[id]
                val normal = definitions[def.lendId]
                if (normal.extras != null) {
                    val lentExtras = Object2ObjectOpenHashMap(normal.extras)
                    lentExtras.remove("aka")
                    if (extras != null) {
                        lentExtras.putAll(extras)
                    }
                    super.set(map, key, id, lentExtras)
                } else {
                    super.set(map, key, id, extras)
                }
            } else {
                super.set(map, key, id, extras)
            }
        }

        /**
         * Overrides the `set` method to provide custom behavior for handling specific key-value pairs
         * within a mutable map. Processes and maps the provided key and value based on defined rules
         * and further modifies the input map if needed.
         *
         * @param map The mutable map to be updated or modified.
         * @param key The key corresponding to the value being processed or inserted into the map.
         * @param value The value to be processed or inserted based on the key.
         * @param indent The current level of indentation or depth in the processing logic.
         * @param parentMap An optional string value indicating the parent map context or hierarchy.
         */
        override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
            if (indent == 1) {
                super.set(map, key, when (key) {
                    "<<" -> {
                        map.putAll(value as Map<String, Any>)
                        return
                    }
                    "id" -> {
                        value as Int
                        if (value in equipment.indices && equipment[value] != -1) {
                            super.set(map, "equip", equipment[value], indent, parentMap)
                        }
                        value
                    }
                    "slot" -> EquipSlot.valueOf(value as String)
                    "type" -> EquipType.valueOf(value as String)
                    "kept" -> ItemKept.valueOf(value as String)
                    "smelting" -> Smelting(value as Map<String, Any>)
                    "smithing" -> Smithing(value as Map<String, Any>)
                    "fishing" -> Catch(value as Map<String, Any>)
                    "firemaking" -> Fire(value as Map<String, Any>)
                    "mining" -> Ore(value as Map<String, Any>)
                    "cooking" -> Uncooked(value as Map<String, Any>)
                    "tanning" -> Tanning(value as List<List<Any>>)
                    "spinning" -> Spinning(value as Map<String, Any>)
                    "pottery" -> Pottery(value as Map<String, Pottery.Ceramic>)
                    "weaving" -> Weaving(value as Map<String, Any>)
                    "jewellery" -> Jewellery(value as Map<String, Any>)
                    "silver_jewellery" -> Silver(value as Map<String, Any>)
                    "runecrafting" -> Rune(value as Map<String, Any>)
                    "ammo" -> ObjectOpenHashSet(value as List<String>)
                    "cleaning" -> Cleaning(value as Map<String, Any>)
                    "fletch_dart" -> FletchDarts(value as Map<String, Any>)
                    "fletch_bolts" -> FletchBolts(value as Map<String, Any>)
                    "fletching_unf" -> Fletching(value as Map<String, Any>)
                    "light_source" -> LightSources(value as Map<String, Any>)
                    "skill_req" -> (value as MutableMap<String, Any>).mapKeys { Skill.valueOf(it.key.toSentenceCase()) }
                    else -> value
                }, indent, parentMap)
            } else {
                super.set(map, key, value, indent, parentMap)
            }
        }

        /**
         * Processes the given anchor object and removes the "aka" entry if the resulting value is a mutable map.
         *
         * @param anchor The input object to be processed and potentially modified.
         * @return The processed object, with "aka" removed if it is a mutable map.
         */
        override fun anchor(anchor: Any): Any {
            val value = super.anchor(anchor)
            if (value is MutableMap<*, *>) {
                value.remove("aka")
            }
            return value
        }
    }
}
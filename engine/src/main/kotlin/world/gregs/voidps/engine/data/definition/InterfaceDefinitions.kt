package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * Represents the default type used to categorize or identify a primary screen or interface within
 * the application. This value is utilized as the fallback or standard type in scenarios where
 * no specific type is explicitly provided.
 */
private const val DEFAULT_TYPE = "main_screen"
/**
 * Indicates the default parent interface name for fixed game layouts.
 *
 * This constant is used as a reference point for the default game frame in scenarios
 * where a fixed parent layout is required. It is tied to the game's specific user
 * interface structure and ensures consistent rendering and handling across related
 * UI components.
 */
private const val DEFAULT_FIXED_PARENT = Interfaces.GAME_FRAME_NAME
/**
 * A constant representing the default name for a game frame in resize mode.
 *
 * This value is indirectly linked to a predefined interface constant that specifies
 * the name used for resizing the game frame in the UI system. It serves as a
 * single source of truth for handling the default behavior related to resize operations.
 */
private const val DEFAULT_RESIZE_PARENT = Interfaces.GAME_FRAME_RESIZE_NAME
/**
 * Indicates whether an area or object is permanent by default.
 *
 * Used as a default value for settings or flags where permanence needs
 * to be established unless explicitly overridden.
 */
private const val DEFAULT_PERMANENT = true

/**
 * Handles the decoding and management of interface definitions.
 *
 * This class provides methods to load, retrieve, and manipulate interface definitions and their components.
 * It is used to decode interface data from a YAML configuration and manage associations between
 * interfaces, their components, types, and other attributes.
 *
 * @constructor Initializes the definitions with an array of `InterfaceDefinition`.
 */
@Suppress("UNCHECKED_CAST")
class InterfaceDefinitions(
    override var definitions: Array<InterfaceDefinition>
) : DefinitionsDecoder<InterfaceDefinition> {

    /**
     * A map linking string identifiers to integer values.
     *
     * Typically used to store and retrieve IDs related to interface elements or components.
     */
    override lateinit var ids: Map<String, Int>
    /**
     * Maintains a mapping between component names and their identifiers.
     *
     * This variable serves as a centralized data structure that links string-based component names or keys
     * to their corresponding integer IDs. It is useful for quick lookups and management of components,
     * allowing for efficient access to their identifiers in various operations or definitions.
     */
    lateinit var componentIds: Map<String, Int>

    /**
     * Retrieves the ID of a specific component based on the provided ID and component name.
     *
     * @param id The identifier representing a specific entity or group.
     * @param component The name of the component to retrieve the ID for.
     * @return The ID of the component as stored in the `componentIds` map, or `null` if the component ID does not exist.
     */
    fun getComponentId(id: String, component: String) = componentIds["${id}_$component"]

    /**
     * Retrieves a specific component from an interface definition based on the given identifiers.
     *
     * @param id The unique string identifier of the interface.
     * @param component The unique string identifier of the component within the interface.
     * @return An instance of [InterfaceComponentDefinition] if the component is found, or `null` if not.
     */
    fun getComponent(id: String, component: String): InterfaceComponentDefinition? {
        return get(id).components?.get(getComponentId(id, component) ?: return null)
    }

    /**
     * Retrieves a component of an object based on its identifier and component index.
     *
     * @param id The unique identifier of the object from which the component is retrieved.
     * @param component The index of the component to retrieve.
     * @return The component at the specified index, or null if the object or component is not found.
     */
    fun getComponent(id: String, component: Int) = get(id).components?.get(component)

    /**
     * Retrieves a specific component of an entity based on its identifier and component index.
     *
     * @param id The identifier of the entity to retrieve.
     * @param component The index of the component to retrieve from the entity.
     * @return The component at the specified index, or null if not found.
     */
    fun getComponent(id: Int, component: Int) = get(id).components?.get(component)

    /**
     * Retrieves an empty instance of `InterfaceDefinition`.
     *
     * This method provides a default or placeholder value representing an empty interface definition.
     * It is commonly used when there is no specific data to return or as a fallback.
     *
     * @return A constant representing an empty interface definition.
     */
    override fun empty() = InterfaceDefinition.EMPTY

    /**
     * Loads interface definitions from the specified YAML configuration.
     *
     * @param yaml the YAML utility instance used for parsing configuration. Defaults to the result of `get()`.
     * @param path the path to the YAML file containing the interface definitions. Defaults to `Settings["definitions.interfaces"]`.
     * @param typePath the path to the YAML file containing the type definitions. Defaults to `Settings["definitions.interfaces.types"]`.
     * @return an instance of `InterfaceDefinitions` containing the loaded interface definitions and associated data.
     */
    fun load(
        yaml: Yaml = get(),
        path: String = Settings["definitions.interfaces"],
        typePath: String = Settings["definitions.interfaces.types"]
    ): InterfaceDefinitions {
        timedLoad("interface extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val componentIds = Object2IntOpenHashMap<String>()
            this.componentIds = componentIds
            val config = object : YamlReaderConfiguration(2, 2) {
                /**
                 * Overrides the behavior for setting a key-value pair in a map during YAML parsing, with additional custom logic
                 * based on specific conditions such as `key`, `value` type, and `indent` level.
                 *
                 * @param map The map to which a key-value pair needs to be set.
                 * @param key The key to be associated with the value in the map.
                 * @param value The value to be set in the map.
                 * @param indent The current indentation level in the YAML data structure.
                 * @param parentMap The parent map key, if relevant to the context, used for hierarchical structures or null.
                 */
                @Suppress("UNCHECKED_CAST")
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "options" && value is Map<*, *> && indent == 3) {
                        value as Map<String, Int>
                        val options = Array(value.maxOf { it.value } + 1) { "" }
                        for ((option, index) in value) {
                            options[index] = option
                        }
                        super.set(map, key, options, indent, parentMap)
                    } else if (indent == 0 && value is Int) {
                        val extras = createMap()
                        set(extras, "id", value, 1, parentMap)
                        ids[key] = value
                        definitions[value].stringId = key
                        definitions[value].extras = extras
                        super.set(map, key, createMap().apply {
                            put("id", value)
                        }, indent, parentMap)
                    } else if (indent == 0) {
                        value as MutableMap<String, Any>
                        val id = value["id"] as Int
                        if (id < 0) {
                            return
                        }
                        ids[key] = id
                        definitions[id].stringId = key
                        definitions[id].extras = value
                        super.set(map, key, value, indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            val typeData: Map<String, Map<String, Any>> = yaml.load(typePath)
            val types = loadTypes(typeData)
            val data = yaml.load<Map<String, MutableMap<String, Any>>>(path, config)
            for ((stringId, map) in data) {
                val typeName = map["type"] as? String ?: DEFAULT_TYPE
                map.putAll(types[typeName]!!)
                val components = map.remove("components") as? Map<String, Any> ?: continue
                val intId = ids.getValue(stringId)
                for ((key, value) in components) {
                    when (value) {
                        is Int -> {
                            componentIds["${stringId}_$key"] = value
                            val componentDefinition = getOrPut(intId, value)
                            componentDefinition.stringId = key
                            componentDefinition.extras = Object2ObjectOpenHashMap<String, Any>(1).apply {
                                put("parent", intId)
                            }
                        }
                        is Map<*, *> -> {
                            value as MutableMap<String, Any>
                            val id = value["id"] as Int
                            componentIds["${stringId}_$key"] = id
                            val componentDefinition = getOrPut(intId, id)
                            componentDefinition.stringId = key
                            value["parent"] = intId
                            componentDefinition.extras = value
                        }
                        is String -> {
                            val range = value.toIntRange(inclusive = true)
                            val startDigit = key.takeLastWhile { it.isDigit() }.toInt()
                            val prefix = key.removeSuffix(startDigit.toString())
                            for ((index, id) in range.withIndex()) {
                                val name = "$prefix${startDigit + index}"
                                map[name] = id
                                componentIds["${stringId}_$name"] = id
                                val componentDefinition = getOrPut(intId, id)
                                componentDefinition.stringId = name
                                componentDefinition.extras = Object2ObjectOpenHashMap<String, Any>(1).apply {
                                    put("parent", intId)
                                }
                            }
                        }
                    }
                }
            }
            data.size
        }
        return this
    }

    /**
     * Retrieves an existing `InterfaceComponentDefinition` for the specified `id` and `index` or creates
     * and stores a new one if it does not already exist.
     *
     * @param id The unique identifier for the interface definition.
     * @param index The index of the component definition within the interface.
     * @return The `InterfaceComponentDefinition` associated with the given `id` and `index`.
     */
    private fun getOrPut(id: Int, index: Int): InterfaceComponentDefinition {
        val definition = definitions[id]
        var components = definition.components
        if (components == null) {
            components = Int2ObjectOpenHashMap(2)
            definition.components = components
        }
        return components.getOrPut(index) { InterfaceComponentDefinition(id = index + (id shl 16)) }
    }

    /**
     * Processes a nested map structure and returns a new map with transformed values.
     *
     * The method modifies the input map by generating new values based on specific keys (`index`, `parent`, `fixedParent`, `resizeParent`,
     * `fixedIndex`, `resizeIndex`, and optionally `permanent`). If certain keys are not found, it uses predefined fallback defaults.
     *
     * @param data A map where the keys represent string identifiers and the values are nested maps containing key-value pairs
     *             used for generating the transformed map.
     * @return A new map with the same structure as the input, but with transformed inner maps containing keys like `parent_fixed`,
     *         `parent_resize`, `index_fixed`, `index_resize`, and optionally `permanent`.
     */
    private fun loadTypes(data: Map<String, Map<String, Any>>): Map<String, Map<String, Any>> {
        return data.mapValues { (_, values) ->
            val index = values["index"] as? Int
            val parent = values["parent"] as? String
            val map = Object2ObjectOpenHashMap<String, Any>(5)
            map["parent_fixed"] = (parent ?: values["fixedParent"] as? String ?: DEFAULT_FIXED_PARENT)
            map["parent_resize"] = (parent ?: values["resizeParent"] as? String ?: DEFAULT_RESIZE_PARENT)
            map["index_fixed"] = (index ?: values["fixedIndex"] as Int)
            map["index_resize"] = (index ?: values["resizeIndex"] as Int)
            if (values.containsKey("permanent")) {
                val permanent = values["permanent"] as Boolean
                if (permanent != DEFAULT_PERMANENT) {
                    map["permanent"] = permanent
                }
            }
            map
        }
    }

}
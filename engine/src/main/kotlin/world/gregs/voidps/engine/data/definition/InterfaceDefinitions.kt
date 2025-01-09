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

private const val DEFAULT_TYPE = "main_screen"
private const val DEFAULT_FIXED_PARENT = Interfaces.GAME_FRAME_NAME
private const val DEFAULT_RESIZE_PARENT = Interfaces.GAME_FRAME_RESIZE_NAME
private const val DEFAULT_PERMANENT = true

@Suppress("UNCHECKED_CAST")
class InterfaceDefinitions(
    override var definitions: Array<InterfaceDefinition>
) : DefinitionsDecoder<InterfaceDefinition> {

    override lateinit var ids: Map<String, Int>
    lateinit var componentIds: Map<String, Int>

    fun getComponentId(id: String, component: String) = componentIds["${id}_$component"]

    fun getComponent(id: String, component: String): InterfaceComponentDefinition? {
        return get(id).components?.get(getComponentId(id, component) ?: return null)
    }

    fun getComponent(id: String, component: Int) = get(id).components?.get(component)

    fun getComponent(id: Int, component: Int) = get(id).components?.get(component)

    override fun empty() = InterfaceDefinition.EMPTY

    fun load(
        yaml: Yaml = get(),
        path: String = Settings["interfacesPath"],
        typePath: String = Settings["interfaceTypesPath"]
    ): InterfaceDefinitions {
        timedLoad("interface extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val componentIds = Object2IntOpenHashMap<String>()
            this.componentIds = componentIds
            val config = object : YamlReaderConfiguration(2, 2) {
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

    private fun getOrPut(id: Int, index: Int): InterfaceComponentDefinition {
        val definition = definitions[id]
        var components = definition.components
        if (components == null) {
            components = Int2ObjectOpenHashMap(2)
            definition.components = components
        }
        return components.getOrPut(index) { InterfaceComponentDefinition(id = index + (id shl 16)) }
    }

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
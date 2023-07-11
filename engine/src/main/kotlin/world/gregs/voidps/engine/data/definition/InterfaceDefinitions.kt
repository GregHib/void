package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.GameFrame.Companion.GAME_FRAME_NAME
import world.gregs.voidps.engine.client.ui.GameFrame.Companion.GAME_FRAME_RESIZE_NAME
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

private const val DEFAULT_TYPE = "main_screen"
private const val DEFAULT_FIXED_PARENT = GAME_FRAME_NAME
private const val DEFAULT_RESIZE_PARENT = GAME_FRAME_RESIZE_NAME
private const val DEFAULT_PERMANENT = true

@Suppress("UNCHECKED_CAST")
class InterfaceDefinitions(
    override var definitions: Array<InterfaceDefinition>
) : DefinitionsDecoder<InterfaceDefinition> {


    override lateinit var ids: Map<String, Int>
    private lateinit var componentIds: Map<String, Int>

    fun getComponentId(id: String, component: String) = componentIds["${id}_$component"]

    fun getComponent(id: String, component: String) = get(id).components?.get(getComponentId(id, component))

    fun getComponent(id: String, component: Int) = get(id).components?.get(component)

    fun getComponent(id: Int, component: Int) = get(id).components?.get(component)

    override fun empty() = InterfaceDefinition.EMPTY

    fun load(
        yaml: Yaml = get(),
        path: String = getProperty("interfacesPath"),
        typePath: String = getProperty("interfaceTypesPath")
    ): InterfaceDefinitions {
        timedLoad("interface extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val componentIds = Object2IntOpenHashMap<String>()
            this.componentIds = componentIds
            val config = object : YamlReaderConfiguration() {
                @Suppress("UNCHECKED_CAST")
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (indent == 0 && value is Int) {
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
                            val componentDefinition = definitions[intId].components?.getOrPut(value) { InterfaceComponentDefinition(value) }!!
                            componentDefinition.stringId = key
                            componentDefinition.extras = mapOf<String, Any>("name" to key, "parent" to intId)
                        }
                        is Map<*, *> -> {
                            value as MutableMap<String, Any>
                            val id = value["id"] as Int
                            val componentDefinition = definitions[intId].components?.getOrPut(id) { InterfaceComponentDefinition(id) }!!
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
                                val componentDefinition = definitions[intId].components?.getOrPut(id) { InterfaceComponentDefinition(id) }!!
                                componentDefinition.stringId = name
                                componentDefinition.extras = mapOf<String, Any>("name" to name, "parent" to intId)
                            }
                        }
                    }
                }
            }
            data.size
        }
        return this
    }

    private fun loadTypes(data: Map<String, Map<String, Any>>): Map<String, Map<String, Any>> {
        return data.mapValues { (_, values) ->
            val index = values["index"] as? Int
            val parent = values["parent"] as? String
            val map = Object2ObjectOpenHashMap<String, Any>()
            map["parent_fixed"] = (parent ?: values["fixedParent"] as? String ?: DEFAULT_FIXED_PARENT)
            map["parent_resize"] = (parent ?: values["resizeParent"] as? String ?: DEFAULT_RESIZE_PARENT)
            map["index_fixed"] = (index ?: values["fixedIndex"] as Int)
            map["index_resize"] = (index ?: values["resizeIndex"] as Int)
            map["permanent"] = (values["permanent"] as? Boolean ?: DEFAULT_PERMANENT)
            map
        }
    }

}
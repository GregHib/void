package world.gregs.voidps.engine.data.definition.extra

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.ui.GameFrame.Companion.GAME_FRAME_NAME
import world.gregs.voidps.engine.client.ui.GameFrame.Companion.GAME_FRAME_RESIZE_NAME
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.DefinitionsDecoder
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
    decoder: InterfaceDecoder
) : DefinitionsDecoder<InterfaceDefinition> {

    override lateinit var definitions: Array<InterfaceDefinition>
    override lateinit var ids: Map<String, Int>

    init {
        val start = System.currentTimeMillis()
        definitions = decoder.indices.map {
            decoder.get(it).apply { actualId = id;stringId = id.toString() }
        }.toTypedArray()
        timedLoad("interface definition", definitions.size, start)
    }

    override fun empty() = InterfaceDefinition.EMPTY

    fun load(
        parser: Yaml = get(),
        path: String = getProperty("interfacesPath"),
        typePath: String = getProperty("interfaceTypesPath")
    ): InterfaceDefinitions {
        timedLoad("interface extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val config = object : YamlReaderConfiguration() {
                @Suppress("UNCHECKED_CAST")
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (indent == 0 && value is Int) {
                        val extras = createMap()
                        set(extras, "id", value, 1, parentMap)
                        ids[key] = value
                        definitions[value].stringId = key
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
                        super.set(map, key, value, indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            val data = parser.load<Map<String, Map<String, Any>>>(path, config)
            val typeData: Map<String, Map<String, Any>> = parser.load(typePath)
            val names = definitions.associate { it.id to it.stringId }
            val types = loadTypes(typeData)
            val components = getComponentsMap(data)
            val idToNames = components.mapValues { it.value.toMap() }
            val componentNames = components.mapValues { entry -> entry.value.associate { it.second to it.first } }
            val extras = loadInterfaceExtras(data, types, idToNames, componentNames)
            val componentExtras = loadComponentExtras(data)
            apply(names, extras) {
                val componentExtra = componentExtras[it.stringId]
                it.components?.forEach { (id, component) ->
                    componentExtra?.get(id)?.let { extras ->
                        component.stringId = extras["name"] as String
                        component.extras = Object2ObjectOpenHashMap(extras)
                    }
                }
            }
            names.size
        }
        return this
    }

    private fun getComponentsMap(data: Map<String, Map<String, Any>>): Map<String, List<Pair<String, Int>>> {
        return data.mapNotNull { (name, values) ->
            val map = values["components"] as? Map<*, *> ?: return@mapNotNull null
            name to listComponents(map)
        }.toMap()
    }

    private fun listComponents(map: Map<*, *>): List<Pair<String, Int>> {
        val all = mutableListOf<Pair<String, Int>>()
        for ((key, value) in map) {
            val name = key as String
            val id = when (value) {
                is String -> {
                    addComponentRange(name, value, all)
                    continue
                }
                is Int -> value
                is Map<*, *> -> value["id"] as Int
                else -> continue
            }
            all.add(name to id)
        }
        return all
    }

    private fun addComponentRange(name: String, value: String, all: MutableList<Pair<String, Int>>) {
        val startDigit = name.dropWhile { !it.isDigit() }.toIntOrNull()
        val range = value.toIntRange(inclusive = true)
        if (startDigit != null) {
            val prefix = name.removeSuffix(startDigit.toString())
            for ((index, i) in range.withIndex()) {
                all.add("$prefix${startDigit + index}" to i)
            }
        }
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

    private fun loadInterfaceExtras(
        data: Map<String, Map<String, Any>>,
        types: Map<String, Map<String, Any>>,
        components: Map<String, Map<String, Int>>,
        componentNames: Map<String, Map<Int, String>>
    ) = data.mapValues { (name, values) ->
        val typeName = values["type"] as? String ?: DEFAULT_TYPE
        val type = types[typeName]
        checkNotNull(type) { "Missing interface type $typeName" }
        values.toMutableMap().apply {
            putAll(type)
            this["name"] = name
            components[name]?.let {
                this["componentInts"] = Object2IntOpenHashMap(it)
            }
            componentNames[name]?.let {
                this["componentIds"] = Int2ObjectOpenHashMap(it)
            }
        }
    }

    private fun loadComponentExtras(
        data: Map<String, Map<String, Any>>
    ) = data.mapNotNull { (name, values) ->
        val parent = values["id"] as Int
        val map = values["components"] as? Map<String, Any> ?: return@mapNotNull null
        name to extrasMap(map, parent)
    }.toMap()

    private fun extrasMap(map: Map<String, Any>, parent: Int): Map<Int, MutableMap<String, Any>> {
        val all = Int2ObjectOpenHashMap<MutableMap<String, Any>>()
        for ((name, value) in map) {
            if (value is String) {
                val startDigit = name.dropWhile { !it.isDigit() }.toInt()
                val prefix = name.removeSuffix(startDigit.toString())
                for ((index, i) in value.toIntRange(inclusive = true).withIndex()) {
                    all[i] = Object2ObjectOpenHashMap<String, Any>().apply {
                        put("name", "$prefix${startDigit + index}")
                        put("parent", parent)
                    }
                }
            } else {
                var id = value as? Int
                val out = Object2ObjectOpenHashMap<String, Any>().apply {
                    put("name", name)
                    put("parent", parent)
                }
                (value as? Map<*, *>)?.forEach { (key, value) ->
                    if (key is String) {
                        if (key == "id") {
                            id = value as Int
                        } else if (key == "options") {
                            val it = value as Map<*, *>
                            val options = Array(it.maxOf { it.value as Int } + 1) { "" }
                            it.forEach { (option, index) ->
                                options[index as Int] = option as String
                            }
                            out["options"] = options
                        } else if (value is String) {
                            out[key] = value
                        } else if (value is Boolean) {
                            out[key] = value
                        } else if (value is Int) {
                            out[key] = value
                        }
                    }
                }
                all[id!!] = out
            }
        }
        return all
    }

}

fun InterfaceDefinition.getComponentId(id: Int): String {
    return getOrNull<Map<Int, String>>("componentIds")?.get(id) ?: return ""
}

fun InterfaceDefinition.getComponentIntId(component: String): Int? {
    return getOrNull<Map<String, Int>>("componentInts")?.get(component)
}

fun InterfaceDefinition.getComponentOrNull(component: String): InterfaceComponentDefinition? {
    return components?.get(getComponentIntId(component))
}
package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.timedLoad

private const val DEFAULT_TYPE = "main_screen"
private const val DEFAULT_FIXED_PARENT = Interfaces.GAME_FRAME_NAME
private const val DEFAULT_RESIZE_PARENT = Interfaces.GAME_FRAME_RESIZE_NAME
private const val DEFAULT_PERMANENT = true

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

    override fun empty() = InterfaceDefinition.EMPTY

    fun load(paths: List<String>, typePath: String): InterfaceDefinitions {
        timedLoad("interface extra") {
            val ids = Object2IntOpenHashMap<String>()
            this.ids = ids
            val componentIds = Object2IntOpenHashMap<String>()
            this.componentIds = componentIds

            val typeData = Object2ObjectOpenHashMap<String, Map<String, Any>>(100, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(typePath) {
                while (nextSection()) {
                    val stringId = section()
                    val extras = Object2ObjectOpenHashMap<String, Any>(8, Hash.VERY_FAST_LOAD_FACTOR)
                    typeData[stringId] = extras
                    extras["parent_fixed"] = DEFAULT_FIXED_PARENT
                    extras["parent_resize"] = DEFAULT_RESIZE_PARENT
                    while (nextPair()) {
                        val key = key()
                        when (key) {
                            "index" -> {
                                val index = int()
                                extras["index_fixed"] = index
                                extras["index_resize"] = index
                            }
                            "parent" -> {
                                val parent = string()
                                extras["parent_fixed"] = parent
                                extras["parent_resize"] = parent
                            }
                            "fixedIndex" -> extras["index_fixed"] = int()
                            "resizeIndex" -> extras["index_resize"] = int()
                            "permanent" -> {
                                val permanent = boolean()
                                if (permanent != DEFAULT_PERMANENT) {
                                    extras["permanent"] = permanent
                                }
                            }
                        }
                    }
                }
            }
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val interfaceStringId = section()
                        var interfaceId = -1
                        val extras = Object2ObjectOpenHashMap<String, Any>(1, Hash.VERY_FAST_LOAD_FACTOR)
                        var typed = false
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> {
                                    interfaceId = int()
                                    extras["id"] = interfaceId
                                    if (interfaceId != -1) {
                                        ids[interfaceStringId] = interfaceId
                                        definitions[interfaceId].stringId = interfaceStringId
                                        if (definitions[interfaceId].extras == null) {
                                            definitions[interfaceId].extras = extras
                                        } else {
                                            (definitions[interfaceId].extras as MutableMap<String, Any>).putAll(extras)
                                        }
                                    }
                                }
                                "components" -> components(componentIds, interfaceStringId, interfaceId, extras)
                                "type" -> {
                                    typed = true
                                    val type = string()
                                    extras["type"] = type
                                    extras.putAll(typeData[type]!!)
                                }
                                else -> extras[key] = value()
                            }
                        }
                        if (interfaceId != -1) {
                            if (!typed) {
                                extras.putAll(typeData[DEFAULT_TYPE]!!)
                            }
                            if (definitions[interfaceId].extras == null) {
                                definitions[interfaceId].extras = extras
                            } else {
                                (definitions[interfaceId].extras as MutableMap<String, Any>).putAll(extras)
                            }
                        }
                    }
                }
            }
            ids.size
        }
        return this
    }

    private fun ConfigReader.components(componentIds: Object2IntOpenHashMap<String>, interfaceId: String, interfaceIntId: Int, extras: Object2ObjectOpenHashMap<String, Any>) {
        while (nextEntry()) {
            val key = key()
            when (peek) {
                '{' -> {
                    var componentId = -1
                    val componentExtras = Object2ObjectOpenHashMap<String, Any>(1, Hash.VERY_FAST_LOAD_FACTOR)
                    var optionsArray = emptyArray<String?>()
                    while (nextEntry()) {
                        when (val componentKey = key()) {
                            "id" -> componentId = int()
                            "options" -> {
                                val options = Object2IntOpenHashMap<String>(4, Hash.VERY_FAST_LOAD_FACTOR)
                                var max = 0
                                while (nextEntry()) {
                                    val option = key()
                                    val index = int()
                                    if (index > max) {
                                        max = index
                                    }
                                    options[option] = index
                                }
                                optionsArray = Array(max + 1) { "" }
                                for ((option, index) in options) {
                                    optionsArray[index] = option
                                }
                            }
                            "cast_id", "amount", "bars" -> componentExtras[componentKey] = int()
                            else -> componentExtras[componentKey] = value()
                        }
                    }
                    if (componentId == -1) {
                        throw IllegalArgumentException("Invalid component id.")
                    }
                    componentIds["${interfaceId}_$key"] = componentId
                    val componentDefinition = getOrPut(interfaceIntId, componentId)
                    componentDefinition.stringId = key
                    componentExtras["id"] = componentId
                    if (optionsArray.isNotEmpty()) {
                        componentExtras["options"] = optionsArray
                    }
                    if (componentDefinition.extras == null) {
                        componentDefinition.extras = componentExtras
                    } else {
                        (componentDefinition.extras as MutableMap<String, Any>).putAll(componentExtras)
                    }
                }
                '"' -> {
                    val range = string().toIntRange(inclusive = true)
                    val startDigit = key.takeLastWhile { it.isDigit() }.toInt()
                    val prefix = key.removeSuffix(startDigit.toString())
                    for ((index, id) in range.withIndex()) {
                        val name = "$prefix${startDigit + index}"
                        extras[name] = id
                        componentIds["${interfaceId}_$name"] = id
                        val componentDefinition = getOrPut(interfaceIntId, id)
                        componentDefinition.stringId = name
                    }
                }
                else -> {
                    val value = int()
                    componentIds["${interfaceId}_$key"] = value
                    val componentDefinition = getOrPut(interfaceIntId, value)
                    componentDefinition.stringId = key
                }
            }
        }
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
}
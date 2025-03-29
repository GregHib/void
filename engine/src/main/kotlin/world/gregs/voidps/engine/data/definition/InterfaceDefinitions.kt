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

class InterfaceDefinitions(
    override var definitions: Array<InterfaceDefinition>
) : DefinitionsDecoder<InterfaceDefinition> {

    override lateinit var ids: Map<String, Int>
    lateinit var componentIds: Map<String, Int>

    fun getComponentId(id: String, component: String) = componentIds["${id}:$component"]

    fun getComponent(id: String, component: String): InterfaceComponentDefinition? {
        return get(id).components?.get(getComponentId(id, component) ?: return null)
    }

    override fun empty() = InterfaceDefinition.EMPTY

    fun load(paths: List<String>, typePath: String): InterfaceDefinitions {
        timedLoad("interface extra") {
            val ids = Object2IntOpenHashMap<String>()
            ids.defaultReturnValue(-1)
            val componentIds = Object2IntOpenHashMap<String>()
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val interfaceStringId = section()
                        var interfaceId = -1
                        var type: String? = null
                        while (nextPair()) {
                            when (val key = key()) {
                                "id" -> interfaceId = int()
                                "components" -> components(componentIds, interfaceStringId, interfaceId)
                                "type" -> type = string()
                                else -> throw IllegalArgumentException("Unknown interface key '$key' in ${exception()}.")
                            }
                        }
                        if (interfaceId != -1) {
                            require(!ids.containsKey(interfaceStringId)) { "Found duplicate interface id $interfaceStringId in ${exception()}." }
                            require(definitions[interfaceId].stringId == "") { "Found duplicate interface id $interfaceId $interfaceStringId ${definitions[interfaceId].stringId} in ${exception()}." }
                            ids[interfaceStringId] = interfaceId
                            definitions[interfaceId].stringId = interfaceStringId
                            definitions[interfaceId].type = type
                        }
                    }
                }
            }

            data class Parent(
                val fixed: Int,
                val resize: Int,
                val permanent: Boolean
            )

            val typeData = Object2ObjectOpenHashMap<String, Parent>(100, Hash.VERY_FAST_LOAD_FACTOR)
            val fixedDefault = ids.getInt(Interfaces.GAME_FRAME_NAME)
            val resizeDefault = ids.getInt(Interfaces.GAME_FRAME_RESIZE_NAME)
            Config.fileReader(typePath) {
                while (nextSection()) {
                    val stringId = section()
                    var parentFixed = fixedDefault
                    var parentResize = resizeDefault
                    var indexFixed: Int? = null
                    var indexResize: Int? = null
                    var permanent = true
                    while (nextPair()) {
                        val key = key()
                        when (key) {
                            "index" -> {
                                indexFixed = int()
                                indexResize = indexFixed
                            }
                            "parent" -> {
                                parentFixed = ids.getInt(string())
                                parentResize = parentFixed
                            }
                            "fixedIndex" -> indexFixed = int()
                            "resizeIndex" -> indexResize = int()
                            "permanent" -> if (!boolean()) {
                                permanent = false
                            }
                        }
                    }
                    requireNotNull(indexFixed) { "No fixed index specified for interface type ${stringId}." }
                    requireNotNull(indexResize) { "No resizable index specified for interface type ${stringId}." }
                    val fixed = if (parentFixed == -1) -1 else InterfaceDefinition.pack(parentFixed, indexFixed)
                    val resize = if (parentResize == -1) -1 else InterfaceDefinition.pack(parentResize, indexResize)
                    typeData[stringId] = Parent(fixed, resize, permanent)
                }
            }
            for (definition in definitions) {
                val type = definition.type ?: Interfaces.DEFAULT_TYPE
                val data = typeData[type] ?: continue
                definition.fixed = data.fixed
                definition.resizable = data.resize
                definition.permanent = data.permanent
            }
            this.ids = ids
            this.componentIds = componentIds
            ids.size
        }
        return this
    }

    private fun ConfigReader.components(componentIds: Object2IntOpenHashMap<String>, interfaceId: String, interfaceIntId: Int) {
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
                    componentIds["${interfaceId}:$key"] = componentId
                    val componentDefinition = getOrPut(interfaceIntId, componentId)
                    require(componentDefinition.stringId == "") { "Found duplicate interface component id $key ${componentDefinition.stringId}" }
                    componentDefinition.stringId = key
                    if (optionsArray.isNotEmpty()) {
                        componentExtras["options"] = optionsArray
                    }
                    if (componentExtras.isNotEmpty()) {
                        componentDefinition.extras = componentExtras
                    }
                }
                '"' -> {
                    val range = string().toIntRange(inclusive = true)
                    val startDigit = key.takeLastWhile { it.isDigit() }.toInt()
                    val prefix = key.removeSuffix(startDigit.toString())
                    for ((index, id) in range.withIndex()) {
                        val name = "$prefix${startDigit + index}"
                        componentIds["${interfaceId}:$name"] = id
                        val componentDefinition = getOrPut(interfaceIntId, id)
                        componentDefinition.stringId = name
                    }
                }
                else -> {
                    val value = int()
                    componentIds["${interfaceId}:$key"] = value
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
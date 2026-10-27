package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat

object InterfaceLoader : Loader<InterfaceState> {
    override val name = "interfaces"
    override val files = listOf("definitions.interfaces")
    override val file = listOf("definitions.interfaces.types")
    override val usesCache = true
    override val codec = SnapshotFormat.codec(InterfaceStateSerializer)

    override fun load(context: LoadContext): InterfaceState {
        val definitions = InterfaceDecoder().load(context.requireCache())
        return parse(definitions, context.list("definitions.interfaces"), context.find("definitions.interfaces.types"))
    }

    override fun install(state: InterfaceState) = InterfaceDefinitions.install(state)

    fun parse(definitions: Array<InterfaceDefinition>, paths: List<String>, typePath: String): InterfaceState {
        val ids = Object2IntOpenHashMap<String>()
        ids.defaultReturnValue(-1)
        val componentIds = Object2IntOpenHashMap<String>()
        for (path in paths) {
            Config.fileReader(path) {
                var parentIntId = -1
                val components = mutableListOf<Pair<String, Int>>()
                while (nextSection()) {
                    val interfaceStringId = section()
                    if (interfaceStringId.contains(".")) {
                        val (interfaceId, key) = interfaceStringId.split(".")
                        val componentParams = Int2ObjectOpenHashMap<Any>(1, Hash.VERY_FAST_LOAD_FACTOR)
                        var optionsArray = emptyArray<String?>()
                        components.clear()
                        while (nextPair()) {
                            when (val componentKey = key()) {
                                "id" -> {
                                    if (peek == '"') {
                                        val range = string().toIntRange(inclusive = true)
                                        val startDigit = key.takeLastWhile { it.isDigit() }.toInt()
                                        val prefix = key.removeSuffix(startDigit.toString())
                                        for ((index, id) in range.withIndex()) {
                                            val name = "$prefix${startDigit + index}"
                                            components.add(name to id)
                                        }
                                    } else {
                                        components.add(key to int())
                                    }
                                }
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
                                "cast_id", "amount", "bars" -> componentParams[Params.id(componentKey)] = int()
                                else -> componentParams[Params.id(componentKey)] = value()
                            }
                        }
                        if (components.isEmpty()) {
                            throw IllegalArgumentException("Invalid component id.")
                        }
                        for ((stringId, componentId) in components) {
                            componentIds["$interfaceId:$stringId"] = componentId
                            val componentDefinition = getOrPut(definitions, parentIntId, componentId)
                            require(componentDefinition.stringId == "") { "Found duplicate interface component id $stringId ${componentDefinition.stringId}" }
                            componentDefinition.stringId = stringId
                            if (optionsArray.isNotEmpty()) {
                                componentParams[Params.OPTIONS] = optionsArray
                            }
                            if (componentParams.isNotEmpty()) {
                                componentDefinition.params = componentParams
                            }
                        }
                        continue
                    }
                    var interfaceId = -1
                    var type: String? = null
                    while (nextPair()) {
                        when (val key = key()) {
                            "id" -> {
                                interfaceId = int()
                                parentIntId = interfaceId
                            }
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
            val permanent: Boolean,
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
                requireNotNull(indexFixed) { "No fixed index specified for interface type $stringId." }
                requireNotNull(indexResize) { "No resizable index specified for interface type $stringId." }
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
        return InterfaceState(definitions, ids, componentIds)
    }

    private fun getOrPut(definitions: Array<InterfaceDefinition>, id: Int, index: Int): InterfaceComponentDefinition {
        val definition = definitions[id]
        var components = definition.components
        if (components == null) {
            components = Int2ObjectOpenHashMap(2)
            definition.components = components
        }
        return components.getOrPut(index) { InterfaceComponentDefinition(id = index + (id shl 16)) }
    }
}

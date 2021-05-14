package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame.Companion.GAME_FRAME_NAME
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame.Companion.GAME_FRAME_RESIZE_NAME
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

private const val DEFAULT_TYPE = "main_screen"
private const val DEFAULT_FIXED_PARENT = GAME_FRAME_NAME
private const val DEFAULT_RESIZE_PARENT = GAME_FRAME_RESIZE_NAME

@Suppress("UNCHECKED_CAST")
class InterfaceDefinitions(
    override val decoder: InterfaceDecoder
) : DefinitionsDecoder<InterfaceDefinition, InterfaceDecoder> {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>
    private lateinit var componentExtras: Map<String, Map<Int, Map<String, Any>>>

    fun getComponentName(name: String, id: Int): String {
        val inter = get(name)
        return (inter.getOrNull("componentIds") as? Map<Int, String>)?.get(id) ?: return ""
    }

    fun getComponentOrNull(name: String, component: String): InterfaceComponentDefinition? {
        val inter = get(name)
        val id = (inter.getOrNull("componentNames") as? Map<String, Int>)?.get(component) ?: return null
        return inter.components?.get(id)
    }

    fun getComponent(name: String, component: String) = getComponentOrNull(name, component) ?: InterfaceComponentDefinition()

    override fun setExtras(definition: InterfaceDefinition, name: String, map: Map<String, Any>) {
        super.setExtras(definition, name, map)
        val extras = componentExtras[name] ?: return
        definition.components?.forEach { (id, component) ->
            extras[id]?.let { extra ->
                component.extras = extra
            }
        }
    }

    fun load(
        loader: FileLoader = get(),
        path: String = getProperty("interfacesPath"),
        typePath: String = getProperty("interfaceTypesPath")
    ): InterfaceDefinitions {
        timedLoad("interface") {
            load(loader.load(path), loader.load<Map<String, Map<String, Any>>>(typePath))
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>, typeData: Map<String, Map<String, Any>>): Int {
        this.names = data.map { (name, values) ->
            val id = values["id"] as? Int
            checkNotNull(id) { "Missing interface id $id" }
            id to name
        }.toMap()
        val types = loadTypes(typeData)
        val components = getComponentsMap(data)
        val idToNames = components.mapValues { it.value.toMap() }
        val componentNames = components.mapValues { entry -> entry.value.associate { it.second to it.first } }
        extras = loadInterfaceExtras(data, types, idToNames, componentNames)
        componentExtras = loadComponentExtras(data)
        return names.size
    }

    private fun getComponentsMap(data: Map<String, Map<String, Any>>) = data.mapNotNull { (name, values) ->
        val map = values["components"] as? Map<*, *> ?: return@mapNotNull null
        name to map.mapNotNull components@{
            it.key as String to when (val value = it.value) {
                is Int -> value
                is Map<*, *> -> value["id"] as Int
                else -> return@components null
            }
        }
    }.toMap()

    private fun loadTypes(data: Map<String, Map<String, Any>>) = data.mapValues { (_, values) ->
        val index = values["index"] as? Int
        val parent = values["parent"] as? String
        mapOf(
            "parent_fixed" to (parent ?: values["fixedParent"] as? String ?: DEFAULT_FIXED_PARENT),
            "parent_resize" to (parent ?: values["resizeParent"] as? String ?: DEFAULT_RESIZE_PARENT),
            "index_fixed" to (index ?: values["fixedIndex"] as Int),
            "index_resize" to (index ?: values["resizeIndex"] as Int)
        )
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
                this["componentNames"] = it
            }
            componentNames[name]?.let {
                this["componentIds"] = it
            }

        }
    }

    private fun loadComponentExtras(
        data: Map<String, Map<String, Any>>
    ) = data.mapNotNull { (name, values) ->
        val parent = values["id"] as Int
        val map = values["components"] as? Map<String, Any> ?: return@mapNotNull null
        name to map.map { (name, value) ->
            var id = value as? Int
            val out = mutableMapOf<String, Any>(
                "name" to name,
                "parent" to parent,
            )
            (value as? Map<*, *>)?.let { extras ->
                id = extras["id"] as Int
                (extras["container"] as? String)?.let { out["container"] = it }
                (extras["primary"] as? Boolean)?.let { out["primary"] = it }
                (extras["options"] as? Map<*, *>)?.let {
                    val options = Array(it.maxOf { it.value as Int } + 1) { "" }
                    it.forEach { (option, index) ->
                        options[index as Int] = option as String
                    }
                    out["options"] = options
                }
            }
            id!! to out
        }.toMap()
    }.toMap()

}
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

class InterfaceDefinitions(
    override val decoder: InterfaceDecoder
) : DefinitionsDecoder<InterfaceDefinition, InterfaceDecoder> {

    override lateinit var extras: Map<String, Map<String, Any>>
    override lateinit var names: Map<Int, String>
    private lateinit var componentExtras: Map<String, Map<Int, Map<String, Any>>>
    private lateinit var componentNames: Map<String, Map<Int, String>>

    fun getComponentName(name: String, id: Int): String {
        return componentNames[name]?.get(id) ?: ""
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
            load(
                loader.load(path),
                loader.load<Map<String, Map<String, Any>>>(typePath)
            )
        }
        return this
    }

    fun load(data: Map<String, Map<String, Any>>, typeData: Map<String, Map<String, Any>>): Int {
        this.names = data.map { (name, values) -> values.getId() to name }.toMap()
        val types = loadTypes(typeData)
        extras = loadDetails(data, types, loadComponentNames2(data))

        componentNames = loadComponentNames(data)
        componentExtras = loadComponentDetails(data)
        return names.size
    }


    private fun loadComponentNames(data: Map<String, Map<String, Any>>) = data.mapNotNull { (name, values) ->
        val components = values["components"] as? Map<*, *> ?: return@mapNotNull null
        name to components.mapNotNull components@{
            when (it.value) {
                is Int -> it.value as Int
                is Map<*, *> -> (it.value as Map<*, *>)["id"] as Int
                else -> return@components null
            } to it.key as String
        }.toMap()
    }.toMap()

    private fun loadComponentNames2(data: Map<String, Map<String, Any>>) = data.mapNotNull { (name, values) ->
        val components = values["components"] as? Map<*, *> ?: return@mapNotNull null
        name to components.mapNotNull components@{
            it.key as String to when (it.value) {
                is Int -> it.value as Int
                is Map<*, *> -> (it.value as Map<*, *>)["id"] as Int
                else -> return@components null
            }
        }.toMap()
    }.toMap()

    private fun loadTypes(data: Map<String, Map<String, Any>>) = data.map { (name, values) ->
        val index = values["index"] as? Int
        val fixedIndex = index ?: values["fixedIndex"] as Int
        val resizeIndex = index ?: values["resizeIndex"] as Int

        val parent = values["parent"] as? String
        val fixedParentName = parent ?: values["fixedParent"] as? String ?: DEFAULT_FIXED_PARENT
        val resizeParentName = parent ?: values["resizeParent"] as? String ?: DEFAULT_RESIZE_PARENT
        name to mapOf(
            "parent_fixed" to fixedParentName,
            "parent_resize" to resizeParentName,
            "index_fixed" to fixedIndex,
            "index_resize" to resizeIndex
        )
    }.toMap()

    private fun loadDetails(
        data: Map<String, Map<String, Any>>,
        types: Map<String, Map<String, Any>>,
        components: Map<String, Map<String, Int>>
    ) = data.map { (name, values) ->
        val typeName = values["type"] as? String ?: DEFAULT_TYPE
        val type = types[typeName]
        checkNotNull(type) { "Missing interface type $typeName" }
        name to values.toMutableMap().apply {
            putAll(type)
            this["name"] = name
            components[name]?.let {
                this["componentNames"] = it
            }
        }
    }.toMap()

    private fun loadComponentDetails(
        data: Map<String, Map<String, Any>>
    ) = data.mapNotNull { (name, values) ->
        val parent = values.getId()
        name to ((values["components"] as? Map<*, *>)?.map {
            componentExtras(it.key as String, it.value!!, parent)
        }?.toMap() ?: return@mapNotNull null)
    }.toMap()

    private fun componentExtras(name: String, value: Any, parent: Int): Pair<Int, Map<String, Any>> {
        var id = value as? Int
        val out = mutableMapOf<String, Any>(
            "name" to name,
            "parent" to parent,
        )
        (value as? Map<*, *>)?.let { extras ->
            id = extras["id"] as Int
            (extras["container"] as? String)?.let {
                out["container"] = it
            }
            (extras["primary"] as? Boolean)?.let {
                out["primary"] = it
            }
            (extras["options"] as? Map<*, *>)?.let {
                val options = Array(it.maxOf { it.value as Int } + 1) { "" }
                it.forEach { (option, index) ->
                    options[index as Int] = option as String
                }
                out["options"] = options
            }
        }
        return id!! to out
    }

    private fun Map<String, Any>.getId(): Int {
        val id = this["id"] as? Int
        checkNotNull(id) { "Missing interface id $id" }
        return id
    }

}
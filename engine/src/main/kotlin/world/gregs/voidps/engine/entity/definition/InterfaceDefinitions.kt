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
    private lateinit var componentExtras: Map<String, Map<String, Map<String, Any>>>
    private lateinit var componentNames: Map<String, Map<Int, String>>
    private lateinit var componentNames2: Map<String, Map<String, Int>>

    fun getComponentName(name: String, id: Int): String {
        return componentNames[name]?.get(id) ?: ""
    }

    fun getComponentOrNull(name: String, component: String): InterfaceComponentDefinition? {
        val id = componentNames2[name]?.get(component) ?: return null
        return get(name).components?.get(id)
    }

    fun getComponent(name: String, component: String) = getComponentOrNull(name, component) ?: InterfaceComponentDefinition()

    override fun setExtras(definition: InterfaceDefinition, name: String, map: Map<String, Any>) {
        super.setExtras(definition, name, map)
        val extras = componentExtras[name] ?: return
        val names = componentNames[name] ?: return
        definition.components?.forEach { (id, component) ->
            extras[names[id]]?.let { extra ->
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
        extras = loadDetails(data, types)
        componentNames = loadComponentNames(data)
        componentNames2 = loadComponentNames2(data)
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
        val index = values.readInt("index")
        val fixedIndex = index ?: values.readInt("fixedIndex")!!
        val resizeIndex = index ?: values.readInt("resizeIndex")!!

        val parent = values.readString("parent")
        val fixedParentName = parent ?: values.readString("fixedParent") ?: DEFAULT_FIXED_PARENT
        val resizeParentName = parent ?: values.readString("resizeParent") ?: DEFAULT_RESIZE_PARENT
        name to mapOf(
            "parent_fixed" to fixedParentName,
            "parent_resize" to resizeParentName,
            "index_fixed" to fixedIndex,
            "index_resize" to resizeIndex
        )
    }.toMap()

    private fun loadDetails(
        data: Map<String, Map<String, Any>>,
        types: Map<String, Map<String, Any>>
    ) = data.map { (name, values) ->
        val typeName = values.readString("type") ?: DEFAULT_TYPE
        val type = types[typeName]
        checkNotNull(type) { "Missing interface type $typeName" }
        name to values.toMutableMap().apply {
            this["name"] to name
            putAll(type)
        }
    }.toMap()

    private fun loadComponentDetails(
        data: Map<String, Map<String, Any>>
    ) = data.mapNotNull { (name, values) ->
        val id = values.getId()
        val components = values.getComponents(id)
        name to components
    }.toMap()

    private fun Map<String, Any>.getComponents(parent: Int): Map<String, Map<String, Any>> {
        val value = this["components"] as? Map<*, *>
        val components = value?.map {
            val name = it.key as String
            name to componentExtras(name, it.value!!, parent)
        }?.toMap()
        return components ?: emptyMap()
    }

    private fun componentExtras(name: String, value: Any, parent: Int): Map<String, Any> {
        val out = mutableMapOf<String, Any>(
            "name" to name,
            "parent" to parent,
        )
        (value as? Map<*, *>)?.let { extras ->
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
        return out
    }

    private fun Map<String, Any>.getId(): Int {
        val id = readInt("id")
        checkNotNull(id) { "Missing interface id $id" }
        return id
    }

    private fun Map<String, Any>.readInt(name: String) = this[name] as? Int
    private fun Map<String, Any>.readString(name: String) = this[name] as? String

}
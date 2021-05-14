package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder
import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail
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
    private lateinit var componentExtras: Map<String, Map<String, InterfaceComponentDetail>>
    private lateinit var componentNames: Map<String, Map<Int, String>>

    fun getComponentName(name: String, id: Int): String {
        return componentNames[name]?.get(id) ?: ""
    }

    fun getComponentOrNull(name: String, component: String): InterfaceComponentDetail? {
        return componentExtras[name]?.get(component)
    }

    fun getComponent(name: String, component: String) = getComponentOrNull(name, component) ?: InterfaceComponentDetail(-1, "")

    override fun applyExtras(definition: InterfaceDefinition, name: String) {
        super.applyExtras(definition, name)
//        val extras = componentExtras[name] ?: return
//        val names = componentNames[name] ?: return
//        definition.components?.forEach { (id, component) ->
//            extras[names[id]]?.let { extra ->
//                component.extras = extra
//            }
//        }
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
        this.names = loadNames(data)
        val types = loadTypes(typeData)
        extras = loadDetails(data, types)
        componentNames = loadComponentNames(data)
        componentExtras = loadComponentDetails(data)
        return names.size
    }

    fun loadNames(data: Map<String, Map<String, Any>>) = data.map { (name, values) -> values.getId() to name }.toMap()

    fun loadComponentNames(data: Map<String, Map<String, Any>>) = data.mapNotNull { (name, values) ->
        val components = values["components"] as? Map<*, *> ?: return@mapNotNull null
        name to components.mapNotNull components@{
            when (it.value) {
                is Int -> it.value as Int
                is Map<*, *> -> (it.value as Map<*, *>)["id"] as Int
                else -> return@components null
            } to it.key as String
        }.toMap()
    }.toMap()

    fun loadTypes(data: Map<String, Map<String, Any>>) = data.map { (name, values) ->
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

    fun loadDetails(
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

    fun loadComponentDetails(
        data: Map<String, Map<String, Any>>
    ) = data.mapNotNull { (name, values) ->
        val id = values.getId()
        val components = values.getComponents()
        components.values.forEach {
            it.parent = id
        }
        name to components
    }.toMap()

    private fun Map<String, Any>.getComponents(): Map<String, InterfaceComponentDetail> {
        val value = this["components"] as? Map<*, *>
        val components = value?.map {
            val name = it.key as String
            name to createComponent(name, it.value!!)
        }?.toMap()
        return components ?: emptyMap()
    }

    fun createComponent(name: String, value: Any): InterfaceComponentDetail {
        return if (value is Int) {
            InterfaceComponentDetail(value, name)
        } else {
            val map = value as Map<*, *>
            val id = map["id"] as Int
            val container = map["container"] as? String ?: ""
            val primary = map["primary"] as? Boolean ?: true
            val options = map["options"] as? Map<*, *>
            InterfaceComponentDetail(id, name, container = container, primaryContainer = primary, options = convert(options))
        }
    }

    private fun convert(map: Map<*, *>?): Array<String> {
        val max = map?.maxByOrNull { it.value as Int }?.value as? Int ?: -1
        val array = Array(max + 1) { "" }
        map?.forEach { (option, index) ->
            array[index as Int] = option as String
        }
        return array
    }

    private fun Map<String, Any>.getId(): Int {
        val id = readInt("id")
        checkNotNull(id) { "Missing interface id $id" }
        return id
    }

    private fun Map<String, Any>.readInt(name: String) = this[name] as? Int
    private fun Map<String, Any>.readString(name: String) = this[name] as? String

}
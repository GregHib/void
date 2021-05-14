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
        return names.size
    }

    fun loadNames(data: Map<String, Map<String, Any>>) = data.map { (name, values) -> values.getId() to name }.toMap()

    fun loadTypes(data: Map<String, Map<String, Any>>) = data.map { (name, values) ->
        val index = values.readInt("index")
        val fixedIndex = index ?: values.readInt("fixedIndex")!!
        val resizeIndex = index ?: values.readInt("resizeIndex")!!

        val parent = values.readString("parent")
        val fixedParentName = parent ?: values.readString("fixedParent") ?: DEFAULT_FIXED_PARENT
        val resizeParentName = parent ?: values.readString("resizeParent") ?: DEFAULT_RESIZE_PARENT
        name to mapOf(
            "index_fixed" to fixedParentName,
            "index_resize" to resizeParentName,
            "parent_fixed" to fixedIndex,
            "parent_resize" to resizeIndex
        )
    }.toMap()

    fun loadDetails(
        data: Map<String, Map<String, Any>>,
        types: Map<String, Map<String, Any>>
    ) = data.map { (name, values) ->
        val id = values.getId()
        val typeName = values.readString("type") ?: DEFAULT_TYPE
        val type = types[typeName]
        checkNotNull(type) { "Missing interface type $typeName" }
        val components = values.getComponents()
        components.values.forEach {
            it.parent = id
        }
        name to values.toMutableMap().apply {
            putAll(type)
            this["components"] = components
            this["componentNames"] = components.map { it.value.id to it.key }.toMap()
        }
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

fun InterfaceDefinition.getComponentOrNull(name: String): InterfaceComponentDetail? {
    return (getOrNull("components") as? Map<String, InterfaceComponentDetail>)?.get(name)
}
fun InterfaceDefinition.getComponent(name: String): InterfaceComponentDetail {
    return getComponentOrNull(name) ?: InterfaceComponentDetail(-1, "")
}
fun InterfaceDefinition.getComponentName(id: Int): String {
    return (getOrNull("componentNames") as? Map<Int, String>)?.get(id) ?: ""
}
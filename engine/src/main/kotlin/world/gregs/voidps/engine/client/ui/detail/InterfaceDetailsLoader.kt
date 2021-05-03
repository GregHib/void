package world.gregs.voidps.engine.client.ui.detail

import org.koin.dsl.module
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame.Companion.GAME_FRAME_NAME
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame.Companion.GAME_FRAME_RESIZE_NAME
import world.gregs.voidps.engine.timedLoad

private const val DEFAULT_TYPE = "main_screen"
private const val DEFAULT_FIXED_PARENT = GAME_FRAME_NAME
private const val DEFAULT_RESIZE_PARENT = GAME_FRAME_RESIZE_NAME

class InterfaceDetailsLoader(private val loader: FileLoader) {

    fun loadFile(path: String): Map<String, Map<String, Any>> = loader.load(path)

    fun load(details: InterfaceDetails, detailPath: String, typesPath: String) {
        timedLoad("interface") {
            val detailData = loadFile(detailPath)
            val typeData = loadFile(typesPath)
            val names = loadNames(detailData)
            val types = loadTypes(typeData, names.map { it.value to it.key }.toMap())
            details.load(loadDetails(detailData, types), names)
            names.size
        }
    }

    fun loadNames(data: Map<String, Map<String, Any>>) = data.map { (name, values) -> values.getId() to name }.toMap()

    fun loadTypes(data: Map<String, Map<String, Any>>, names: Map<String, Int>) = data.map { (name, values) ->
        val index = values.readInt("index")
        val fixedIndex = index ?: values.readInt("fixedIndex")
        val resizeIndex = index ?: values.readInt("resizeIndex")

        val parent = values.readString("parent")
        val fixedParentName = parent ?: values.readString("fixedParent") ?: DEFAULT_FIXED_PARENT
        val resizeParentName = parent ?: values.readString("resizeParent") ?: DEFAULT_RESIZE_PARENT
        val fixedParent = names.getParentId(fixedParentName)
        val resizeParent = names.getParentId(resizeParentName)

        name to InterfaceData(
            fixedParent,
            resizeParent,
            fixedIndex,
            resizeIndex
        )
    }.toMap()

    fun loadDetails(
        data: Map<String, Map<String, Any>>,
        types: Map<String, InterfaceData>
    ) = data.map { (name, values) ->
        val id = values.getId()
        val typeName = values.readString("type") ?: DEFAULT_TYPE
        val type = types[typeName]
        checkNotNull(type) { "Missing interface type $typeName" }
        val components = values.getComponents()
        name to InterfaceDetail(id, name, typeName, type, components)
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

    private fun Map<String, Int>.getParentId(name: String): Int {
        val id = this[name]
        checkNotNull(id) { "Missing parent $name" }
        return id
    }

    private fun Map<String, Any>.readInt(name: String) = this[name] as? Int
    private fun Map<String, Any>.readString(name: String) = this[name] as? String

}

val interfaceModule = module {
    single(createdAtStart = true) {
        InterfaceDetails().apply {
            InterfaceDetailsLoader(get())
                .load(this, getProperty("interfacesPath"), getProperty("interfaceTypesPath"))
        }
    }
}
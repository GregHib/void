package rs.dusk.engine.client.ui

import org.koin.dsl.module
import rs.dusk.engine.TimedLoader
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.model.entity.character.player.PlayerGameFrame.Companion.GAME_FRAME_NAME
import rs.dusk.engine.model.entity.character.player.PlayerGameFrame.Companion.GAME_FRAME_RESIZE_NAME

private const val DEFAULT_TYPE = "main_screen"
private const val DEFAULT_FIXED_PARENT = GAME_FRAME_NAME
private const val DEFAULT_RESIZE_PARENT = GAME_FRAME_RESIZE_NAME

class InterfaceLoader(private val loader: FileLoader) : TimedLoader<InterfacesLookup>("interfaces") {

    fun loadFile(path: String): Map<String, Map<String, Any>> = loader.load(path)

    override fun load(args: Array<out Any?>): InterfacesLookup {
        return loadAll(args[0] as String, args[1] as String)
    }

    fun loadAll(detailPath: String, typesPath: String): InterfacesLookup {
        val detailData = loadFile(detailPath)
        val typeData = loadFile(typesPath)
        val names = loadNames(detailData)
        val types = loadTypes(typeData, names)
        val details = loadDetails(detailData, types)
        count = names.size
        return InterfacesLookup(details, names)
    }

    fun loadNames(data: Map<String, Map<String, Any>>) = data.map { (name, values) -> name to values.getId() }.toMap()

    fun loadTypes(data: Map<String, Map<String, Any>>, names: Map<String, Int>) = data.map { (name, values) ->
        val index = values.readInt("index")
        val fixedIndex = index ?: values.readInt("fixedIndex")
        val resizeIndex = index ?: values.readInt("resizeIndex")

        val parent = values.readString("parent")
        val fixedParentName = parent ?: values.readString("fixedParent") ?: DEFAULT_FIXED_PARENT
        val resizeParentName = parent ?: values.readString("resizeParent") ?: DEFAULT_RESIZE_PARENT
        val fixedParent = names.getParentId(fixedParentName)
        val resizeParent = names.getParentId(resizeParentName)

        name to InterfaceData(fixedParent, resizeParent, fixedIndex, resizeIndex)
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
        id to Interface(id, name, typeName, type, components)
    }.toMap()

    private fun Map<String, Any>.getComponents(): Map<Int, String> {
        val value = this["components"] as? Map<*, *>
        val components = value?.map { it.value as Int to it.key as String }?.toMap()
        return components ?: emptyMap()
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
        InterfaceLoader(get()).run(getProperty("interfacesPath"), getProperty("interfaceTypesPath"))
    }
}
package rs.dusk.engine.client.ui

import org.koin.dsl.module
import rs.dusk.engine.data.file.FileLoader

private const val DEFAULT_TYPE = "main_screen"
private const val DEFAULT_FIXED_PARENT = "toplevel"
private const val DEFAULT_RESIZE_PARENT = "toplevel_full"

fun load(loader: FileLoader, interfacePath: String, typesPath: String): InterfacesLookup {
    val types: LinkedHashMap<String, LinkedHashMap<String, Any>> = loader.load(typesPath)
    val details: LinkedHashMap<String, LinkedHashMap<String, Any>> = loader.load(interfacePath)
    val names = details.map { it.key to it.value["id"] as Int }.toMap()
    val data = types.map { it.key to convert(names, it.value) }.toMap()
    val interfaces = details.map { it.value["id"] as Int to Interface(it.value["id"] as Int, data[it.value["type"] as? String ?: DEFAULT_TYPE]) }.toMap()
    return InterfacesLookup(interfaces, names)
}

private fun convert(names: Map<String, Int>, map: LinkedHashMap<String, Any>): InterfaceData {
    val parent = map["parent"] as? String
    val fixedParent = parent ?: map["fixedParent"] as? String ?: DEFAULT_FIXED_PARENT
    val resizeParent = parent ?: map["resizeParent"] as? String ?: DEFAULT_RESIZE_PARENT
    val index = map["index"] as? Int
    val fixedIndex = index ?: map["fixedIndex"] as? Int
    val resizeIndex = index ?: map["resizeIndex"] as? Int
    checkNotNull(names[fixedParent]) { "Couldn't find interface with name $fixedParent"}
    checkNotNull(names[resizeParent]) { "Couldn't find interface with name $resizeParent"}
    return InterfaceData(names[fixedParent], names[resizeParent], fixedIndex, resizeIndex)
}

val interfaceModule = module {
    single(createdAtStart = true) { load(get(), getProperty("interfacesPath"), getProperty("interfaceTypesPath")) }
}
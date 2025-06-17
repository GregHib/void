package world.gregs.yaml.read

import it.unimi.dsi.fastutil.Hash.DEFAULT_INITIAL_SIZE
import it.unimi.dsi.fastutil.Hash.DEFAULT_LOAD_FACTOR
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

/**
 * Handles creation and modification of collections to allow custom changes during reading
 * @param ignoreAnchors don't resolve anchors and leave them and aliases as strings
 */
open class YamlReaderConfiguration(
    private val expectedListSize: Int = 10,
    private val expectedMapSize: Int = DEFAULT_INITIAL_SIZE,
    private val mapLoadFactor: Float = DEFAULT_LOAD_FACTOR,
    val ignoreAnchors: Boolean = false,
) {

    open fun createList(): MutableList<Any> = ObjectArrayList(expectedListSize)

    open fun createMap(): MutableMap<String, Any> = Object2ObjectOpenHashMap(expectedMapSize, mapLoadFactor)

    open fun setEmpty(map: MutableMap<String, Any>, key: String) {
        map[key] = ""
    }

    open fun addListItem(reader: YamlReader, list: MutableList<Any>, indentOffset: Int, parentMap: String?) {
        add(list, reader.value(indentOffset, null), parentMap)
    }

    open fun setMapValue(reader: YamlReader, map: MutableMap<String, Any>, key: String, indent: Int, indentOffset: Int, withinMap: String?, parentMap: String?) {
        set(map, key, reader.value(indentOffset, withinMap), indent, parentMap)
    }

    open fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
        list.add(value)
    }

    open fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
        map[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    open fun anchor(anchor: Any): Any = when (anchor) {
        is List<*> -> createList().apply {
            addAll(anchor as List<Any>)
        }
        is Map<*, *> -> createMap().apply {
            putAll(anchor as Map<String, Any>)
        }
        else -> anchor
    }
}

package world.gregs.yaml.read

import it.unimi.dsi.fastutil.Hash.DEFAULT_INITIAL_SIZE
import it.unimi.dsi.fastutil.Hash.DEFAULT_LOAD_FACTOR
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

/**
 * Handles creation and modification of collections to allow custom changes during parsing
 */
open class YamlReaderConfiguration(
    private val expectedListSize: Int = 10,
    private val expectedMapSize: Int = DEFAULT_INITIAL_SIZE,
    private val mapLoadFactor: Float = DEFAULT_LOAD_FACTOR
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
}
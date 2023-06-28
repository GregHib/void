package world.gregs.yaml.config

import world.gregs.yaml.read.YamlReader

/**
 * Handles creation and modification of collections to allow custom changes during parsing
 */
open class CollectionConfiguration {

    open fun createList(): MutableList<Any> = mutableListOf()

    open fun createMap(): MutableMap<String, Any> = mutableMapOf()

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
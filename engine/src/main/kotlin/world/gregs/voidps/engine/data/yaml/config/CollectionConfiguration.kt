package world.gregs.voidps.engine.data.yaml.config

import world.gregs.voidps.engine.data.yaml.parse.Parser

/**
 * Handles creation and modification of collections to allow custom changes during parsing
 */
open class CollectionConfiguration {

    open fun createList(): MutableList<Any> = mutableListOf()

    open fun createMap(): MutableMap<String, Any> = mutableMapOf()

    open fun setEmpty(map: MutableMap<String, Any>, key: String) {
        map[key] = ""
    }

    open fun addListItem(parser: Parser, list: MutableList<Any>, indentOffset: Int, parentMap: String?) {
        add(list, parser.value(indentOffset, null), parentMap)
    }

    open fun setMapValue(parser: Parser, map: MutableMap<String, Any>, key: String, indent: Int, indentOffset: Int, withinMap: String?) {
        set(map, key, parser.value(indentOffset, withinMap), indent)
    }

    open fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
        list.add(value)
    }

    open fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int) {
        map[key] = value
    }
}
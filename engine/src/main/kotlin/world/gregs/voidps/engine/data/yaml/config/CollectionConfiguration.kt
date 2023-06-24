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

    open fun addListItem(parser: Parser, list: MutableList<Any>, indentOffset: Int, withinMap: Boolean) {
        list.add(parser.value(indentOffset, withinMap))
    }

    open fun setMapValue(parser: Parser, map: MutableMap<String, Any>, key: String, indent: Int, indentOffset: Int, withinMap: Boolean) {
        set(map, key, parser.value(indentOffset, withinMap), indent)
    }

    open fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int) {
        map[key] = value
    }
}
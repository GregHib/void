package world.gregs.voidps.engine.data.yaml.manage

import world.gregs.voidps.engine.data.yaml.parse.Parser

/**
 * Handles creation and modification of collections to allow custom changes during parsing
 */
open class CollectionManager {

    open fun createList(): MutableList<Any> = mutableListOf()

    open fun createMap(): MutableMap<String, Any> = mutableMapOf()

    open fun setEmpty(map: MutableMap<String, Any>, key: String) {
        map[key] = ""
    }

    open fun addListItem(parser: Parser, list: MutableList<Any>, indentOffset: Int, withinMap: Boolean) {
        list.add(parser.value(indentOffset, withinMap))
    }

    open fun setMapValue(parser: Parser, map: MutableMap<String, Any>, key: String, indentOffset: Int, withinMap: Boolean) {
        map[key] = parser.value(indentOffset, withinMap)
    }
}
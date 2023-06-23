package world.gregs.voidps.engine.data.yaml

open class CollectionFactory {

    open fun createList(): MutableList<Any> = mutableListOf()

    open fun createMap(): MutableMap<String, Any> = mutableMapOf()

    open fun setEmptyMapValue(map: MutableMap<String, Any>, key: String) {
        map[key] = ""
    }

    open fun setMapValue(map: MutableMap<String, Any>, key: String, value: Any) {
        map[key] = value
    }

    open fun addListItem(list: MutableList<Any>, value: Any) {
        list.add(value)
    }

    open fun addListItem(value: ValueParser, list: MutableList<Any>, indentOffset: Int, withinMap: Boolean) {
        list.add(value.parseValue(indentOffset, withinMap))
    }

    open fun setMapValue(value: ValueParser, map: MutableMap<String, Any>, key: String, indentOffset: Int, withinMap: Boolean) {
        map[key] = value.parseValue(indentOffset, withinMap)
    }
}
package world.gregs.voidps.engine.data.yaml

open class CollectionFactory {

    open fun createList(): MutableList<Any> = mutableListOf()

    open fun createMap(): MutableMap<String, Any> = mutableMapOf()

    open fun setEmptyMapValue(map: MutableMap<String, Any>, key: String) {
        map[key] = ""
    }

    open fun addListItem(value: ValueParser, list: MutableList<Any>, indentOffset: Int, withinMap: Boolean) {
        list.add(value.value(indentOffset, withinMap))
    }

    open fun setMapValue(value: ValueParser, map: MutableMap<String, Any>, key: String, indentOffset: Int, withinMap: Boolean) {
        map[key] = value.value(indentOffset, withinMap)
    }
}
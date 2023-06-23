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
}
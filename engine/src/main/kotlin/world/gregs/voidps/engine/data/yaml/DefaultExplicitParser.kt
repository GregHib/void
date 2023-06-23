package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader

open class DefaultExplicitParser(reader: CharArrayReader, collection: CollectionFactory) : ExplicitParser(reader, collection) {

    override fun setMapValue(map: MutableMap<String, Any>, key: String, withinMap: Boolean) {
        collection.setMapValue(map, key, parseValue(0, withinMap))
    }

    override fun addListItem(list: MutableList<Any>) {
        collection.addListItem(list, parseValue(0, false))
    }
}
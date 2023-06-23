package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader

open class DefaultLineParser(reader: CharArrayReader, collection: CollectionFactory, explicit: ExplicitParser) : LineParser(reader, collection, explicit) {

    override fun addListItem(list: MutableList<Any>) {
        collection.addListItem(list, parseValue(1, false))
    }

    override fun setMapValue(map: MutableMap<String, Any>, key: String) {
        collection.setMapValue(map, key, parseValue(0, true))
    }

}
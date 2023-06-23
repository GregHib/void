package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader
import world.gregs.voidps.engine.data.YamlParserI

open class DefaultExplicitParser(parser: YamlParserI, reader: CharArrayReader) : ExplicitParser(parser, reader) {

    override fun createList(): MutableList<Any> = mutableListOf()

    override fun createMap(): MutableMap<String, Any> = mutableMapOf()

    override fun setEmptyMapValue(map: MutableMap<String, Any>, key: String) {
        map[key] = ""
    }

    override fun setMapValue(map: MutableMap<String, Any>, key: String) {
        map[key] = delegate.parseValue(withinMap = true)
    }

    override fun setExplicitMapValue(map: MutableMap<String, Any>, key: String) {
        map[key] = parseExplicitValue()
    }

    override fun addListItem(list: MutableList<Any>) {
        list.add(parseExplicitValue())
    }
}
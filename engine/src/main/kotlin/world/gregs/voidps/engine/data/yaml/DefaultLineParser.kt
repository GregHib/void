package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader
import world.gregs.voidps.engine.data.YamlParserI

open class DefaultLineParser(val parser: YamlParserI, reader: CharArrayReader) : LineParser(reader) {

    override fun createMap(): MutableMap<String, Any> = mutableMapOf()

    override fun createList(): MutableList<Any> = mutableListOf()

    override fun addListItem(list: MutableList<Any>) {
        list.add(parser.parseValue(1))
    }

    override fun setMapValue(map: MutableMap<String, Any>, key: String) {
        map[key] = parser.parseValue(withinMap = true)
    }

    override fun setEmptyMapValue(map: MutableMap<String, Any>, key: String) {
        map[key] = ""
    }

}
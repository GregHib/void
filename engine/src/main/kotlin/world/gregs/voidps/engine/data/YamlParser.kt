package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.data.yaml.CollectionFactory
import world.gregs.voidps.engine.data.yaml.ExplicitParser
import world.gregs.voidps.engine.data.yaml.LineParser

class YamlParser(
    var factory: CollectionFactory = CollectionFactory(),
    val reader: CharArrayReader = CharArrayReader(),
    private val explicit: ExplicitParser = ExplicitParser(reader, factory),
    private val lineParser: LineParser = LineParser(reader, factory, explicit)
) {
    fun parse(charArray: CharArray, length: Int): Any {
        reader.set(charArray, length)
        reader.nextLine()
        return lineParser.parseValue(0, false)
    }
}
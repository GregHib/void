package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.yaml.factory.CollectionFactory
import world.gregs.voidps.engine.data.yaml.parse.ExplicitParser
import world.gregs.voidps.engine.data.yaml.parse.LineParser

class YamlParser(
    var collection: CollectionFactory = CollectionFactory(),
    val reader: CharReader = CharReader(),
    private val explicit: ExplicitParser = ExplicitParser(reader, collection),
    private val line: LineParser = LineParser(reader, collection, explicit)
) {
    fun parse(charArray: CharArray, length: Int = charArray.size): Any {
        explicit.collection = collection
        reader.set(charArray, length)
        reader.nextLine()
        return line.value(0, false)
    }
}
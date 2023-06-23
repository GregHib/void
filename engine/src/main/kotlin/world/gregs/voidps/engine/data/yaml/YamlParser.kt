package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.yaml.factory.CollectionFactory
import world.gregs.voidps.engine.data.yaml.parse.ExplicitParser
import world.gregs.voidps.engine.data.yaml.parse.NormalParser

/**
 * High performance parser for simplified YAML
 */
class YamlParser(
    var collection: CollectionFactory = CollectionFactory(),
    val reader: CharReader = CharReader(),
    private val explicit: ExplicitParser = ExplicitParser(reader, collection),
    private val normal: NormalParser = NormalParser(reader, collection, explicit)
) {
    fun parse(charArray: CharArray, length: Int = charArray.size): Any {
        explicit.collection = collection
        reader.set(charArray, length)
        reader.nextLine()
        return normal.value(indentOffset = 0, withinMap = false)
    }
}
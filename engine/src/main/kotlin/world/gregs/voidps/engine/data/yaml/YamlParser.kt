package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.yaml.manage.CollectionManager
import world.gregs.voidps.engine.data.yaml.parse.ExplicitParser
import world.gregs.voidps.engine.data.yaml.parse.NormalParser

/**
 * High performance parser for simplified YAML
 */
class YamlParser(
    var collection: CollectionManager = CollectionManager(),
    val reader: CharReader = CharReader(collection.createMap()),
    private val explicit: ExplicitParser = ExplicitParser(reader, collection),
    private val normal: NormalParser = NormalParser(reader, collection, explicit)
) {
    fun parse(string: String) = parse(string.toCharArray())

    fun parse(charArray: CharArray, length: Int = charArray.size): Any {
        explicit.collection = collection
        reader.anchors.clear()
        reader.set(charArray, length)
        reader.nextLine()
        return normal.value(indentOffset = 0, withinMap = false)
    }
}
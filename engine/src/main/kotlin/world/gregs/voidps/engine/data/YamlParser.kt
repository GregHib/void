package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.data.yaml.CollectionFactory
import world.gregs.voidps.engine.data.yaml.ExplicitParser
import world.gregs.voidps.engine.data.yaml.LineParser

class YamlParser(
    var factory: CollectionFactory = CollectionFactory(),
    val reader: CharArrayReader = CharArrayReader(),
    private val explicit: ExplicitParser = ExplicitParser(reader, factory),
    private val lineParser: LineParser = LineParser(reader, factory, explicit)
) : YamlParserI {

    override fun parse(charArray: CharArray, length: Int): Any {
        reader.set(charArray, length)
        reader.nextLine()
        return parseValue()
    }

    override fun parseValue(indentOffset: Int, withinMap: Boolean): Any {
        return when (reader.char) {
            '[' -> explicit.parseExplicitList()
            '{' -> explicit.parseExplicitMap()
            '&' -> {
                reader.skipAnchorString()
                reader.nextLine()
                parseValue()
            }
            else -> if (reader.isListItem()) {
                lineParser.list(withinMap)
            } else {
                val value = lineParser.parseType()
                if (reader.inBounds && reader.char == ':') {
                    lineParser.map(value.toString(), indentOffset)
                } else {
                    return value
                }
            }
        }
    }
}
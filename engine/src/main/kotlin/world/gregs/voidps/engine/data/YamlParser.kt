package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.data.yaml.Explicit
import world.gregs.voidps.engine.data.yaml.Normal

class YamlParser : YamlParserI {
    val reader = CharArrayReader()

    val explicit = Explicit(this, reader)
    val normal = Normal(this, reader)

    override var mapModifier: (key: String, value: Any) -> Any = { _, value -> value }
    override var listModifier: (value: Any) -> Any = { it }

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
                normal.list(withinMap)
            } else {
                val value = normal.parseType()
                if (reader.inBounds && reader.char == ':') {
                    normal.map(value.toString(), indentOffset)
                } else {
                    return value
                }
            }
        }
    }

    companion object {
        const val EXPECTED_LIST_SIZE = 2
        const val EXPECTED_EXPLICIT_LIST_SIZE = 2
        const val EXPECTED_MAP_SIZE = 8
        const val EXPECTED_EXPLICIT_MAP_SIZE = 5
    }
}
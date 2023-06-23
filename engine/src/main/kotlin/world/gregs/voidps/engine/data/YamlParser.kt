package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.data.yaml.Explicit
import world.gregs.voidps.engine.data.yaml.Normal

class YamlParser : CharArrayReader(), Explicit, Normal {
    override var mapModifier: (key: String, value: Any) -> Any = { _, value -> value }
    override var listModifier: (value: Any) -> Any = { it }

    override fun parse(charArray: CharArray, length: Int): Any {
        set(charArray, length)
        nextLine()
        return parseVal()
    }

    override fun parseVal(indentOffset: Int, withinMap: Boolean): Any {
        return when (input[index]) {
            '[' -> parseExplicitList()
            '{' -> parseExplicitMap()
            '&' -> skipAnchorString()
            else -> if (isListItem()) {
                list(withinMap)
            } else {
                val value = parseType()
                if (index < size && input[index] == ':') {
                    map(value.toString(), indentOffset)
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
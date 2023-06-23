package world.gregs.voidps.engine.data.yaml.parse

import world.gregs.voidps.engine.data.yaml.CharReader
import world.gregs.voidps.engine.data.yaml.manage.CollectionManager

/**
 * Parses maps and lists wrapped in square or curley brackets
 */
class ExplicitParser(
    reader: CharReader,
    var collection: CollectionManager
) : Parser(reader) {

    override fun isClosingTerminator(char: Char) = super.isClosingTerminator(char) || char == '}' || char == ']' || char == ','

    override fun isOpeningTerminator(char: Char) = super.isOpeningTerminator(char) || char == '{' || char == '['

    override fun collection(indentOffset: Int, withinMap: Boolean): Any {
        val type = type()
        return if (reader.inBounds && reader.char == ':') {
            keyValuePair(type.toString())
        } else {
            type
        }
    }

    private fun keyValuePair(key: String): Map<String, Any> {
        val map = collection.createMap()
        reader.skip() // skip colon
        reader.skipSpaces()
        if (reader.outBounds) {
            collection.setEmpty(map, key)
            return map
        }
        val currentIndent = reader.indentation
        if (isOpeningTerminator(reader.char)) {
            reader.nextLine()
            if (reader.indentation < currentIndent) {
                collection.setEmpty(map, key)
                return map
            } else if (reader.indentation == currentIndent && reader.char != '-') {
                collection.setEmpty(map, key)
            } else {
                collection.setMapValue(this, map, key, indentOffset = 0, withinMap = true)
            }
        } else {
            collection.setMapValue(this, map, key, indentOffset = 0, withinMap = true)
        }
        return map
    }

    override fun explicitMap(): Map<String, Any> {
        val map = collection.createMap()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            val key = type().toString()
            if (reader.inBounds && reader.char != ':') {
                throw IllegalArgumentException("Expected key-pair value ${reader.exception}")
            }
            reader.skip() // skip colon
            reader.nextLine()
            collection.setMapValue(this, map, key, indentOffset = 0, withinMap = false)
            reader.nextLine()
            val char = reader.char
            reader.skip()// skip comma/closing char
            reader.nextLine()
            if (char == '}') {
                return map
            } else if (char != ',') {
                throw IllegalArgumentException("Expecting key-value pair or end of map ${reader.exception}")
            }
        }
        return map
    }

    override fun explicitList(): List<Any> {
        val list = collection.createList()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            collection.addListItem(this, list, indentOffset = 0, withinMap = false)
            reader.nextLine()
            val char = reader.char
            reader.skip() // skip comma / closing char
            reader.nextLine()
            if (char == ']') {
                return list
            } else if (char != ',') {
                throw IllegalArgumentException("Expecting item or end of list ${reader.exception}")
            }
        }
        return list
    }

}
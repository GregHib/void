package world.gregs.yaml.read

import world.gregs.yaml.CharReader

/**
 * Reads maps and lists wrapped in square or curley brackets
 */
class ExplicitCollectionReader(
    reader: CharReader,
    config: YamlReaderConfiguration,
) : YamlReader(reader, config) {

    override fun isClosingTerminator(char: Char) = super.isClosingTerminator(char) || char == '}' || char == ']' || char == ','

    override fun isOpeningTerminator(char: Char) = super.isOpeningTerminator(char) || char == '{' || char == '['

    override fun collection(indentOffset: Int, withinMap: String?): Any {
        val type = type()
        return if (reader.inBounds && reader.char == ':') {
            keyValuePair(type.toString(), withinMap)
        } else {
            type
        }
    }

    private fun keyValuePair(key: String, withinMap: String?): Map<String, Any> {
        val map = config.createMap()
        reader.skip() // skip colon
        reader.skipSpaces()
        if (reader.outBounds) {
            config.setEmpty(map, key)
            return map
        }
        val currentIndent = reader.indentation
        if (isOpeningTerminator(reader.char)) {
            reader.nextLine()
            if (reader.indentation < currentIndent) {
                config.setEmpty(map, key)
                return map
            } else if (reader.indentation == currentIndent && reader.char != '-') {
                config.setEmpty(map, key)
            } else {
                config.setMapValue(this, map, key, currentIndent, indentOffset = 0, withinMap = key, parentMap = withinMap)
            }
        } else {
            config.setMapValue(this, map, key, currentIndent, indentOffset = 0, withinMap = key, parentMap = withinMap)
        }
        return map
    }

    override fun explicitMap(): Map<String, Any> {
        val indent = reader.indentation
        val map = config.createMap()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            if (reader.char == '}') {
                reader.skip() // closing char
                reader.nextLine()
                return map
            }
            val key = type().toString()
            if (reader.inBounds && reader.char != ':') {
                throw IllegalArgumentException("Expected key-pair value ${reader.exception}")
            }
            reader.skip() // skip colon
            reader.nextLine()
            reader.indentation = indent + 1
            config.setMapValue(this, map, key, indent, indentOffset = 0, withinMap = key, parentMap = key)
            reader.nextLine()
            val char = reader.char
            reader.skip() // skip comma/closing char
            reader.nextLine()
            if (char == '}') {
                return map
            } else if (char != ',') {
                throw IllegalArgumentException("Expecting key-value pair or end of map ${reader.exception}")
            }
        }
        return map
    }

    override fun explicitList(withinMap: String?): List<Any> {
        val list = config.createList()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            if (reader.char == ']') {
                reader.skip() // skip closing char
                reader.nextLine()
                return list
            }
            config.addListItem(this, list, indentOffset = 0, parentMap = withinMap)
            reader.nextLine()
            val char = reader.char
            reader.skip() // skip comma / closing char
            reader.nextLine()
            if (char == ']') {
                return list
            } else if (char != ',') {
                throw IllegalArgumentException("Expecting item or end of list '$char' ${reader.exception}")
            }
        }
        return list
    }
}

package world.gregs.yaml.read

import world.gregs.yaml.CharReader

/**
 * Reads regular lists and maps
 */
class NormalCollectionReader(
    reader: CharReader,
    config: YamlReaderConfiguration,
    private val explicit: ExplicitCollectionReader,
) : YamlReader(reader, config) {

    override fun explicitList(withinMap: String?) = explicit.explicitList(withinMap)

    override fun explicitMap() = explicit.explicitMap()

    override fun collection(indentOffset: Int, withinMap: String?): Any {
        return if (isListItem()) {
            list(withinMap)
        } else {
            val value = type()
            if (reader.inBounds && reader.char == ':') {
                map(value.toString(), indentOffset, withinMap)
            } else {
                return value
            }
        }
    }

    private fun isListItem() = reader.char == '-' && reader.nextCharEmpty()

    private fun list(withinMap: String?): Any {
        val list = config.createList()
        val currentIndent = reader.indentation
        while (reader.inBounds) {
            // Finished if found dented
            if (reader.indentation < currentIndent) {
                return list
            }
            if (reader.indentation > currentIndent) {
                throw IllegalArgumentException("Expected aligned list item at ${reader.exception}")
            }
            if (reader.char != '-' || reader.peekNext != ' ') {
                if (withinMap != null) {
                    return list
                }
                throw IllegalArgumentException("Expected list item at ${reader.exception}")
            }
            reader.skip(2)
            reader.skipSpaces()
            config.addListItem(this, list, indentOffset = 1, parentMap = withinMap)
            reader.nextLine()
        }
        return list
    }

    private fun map(firstKey: String, indentOffset: Int, withinMap: String?): Any {
        val map = config.createMap()
        var openEnded = false
        val currentIndent = reader.indentation + indentOffset

        fun addValue(key: String): Boolean {
            reader.skip() // skip :
            reader.skipSpaces()
            if (reader.outBounds) {
                config.setEmpty(map, key)
                return true
            } else if (explicit.isOpeningTerminator(reader.char)) {
                reader.nextLine()
                if (reader.indentation < currentIndent || reader.indentation == currentIndent && reader.char != '-') {
                    config.setEmpty(map, key)
                } else {
                    openEnded = false
                    config.setMapValue(this, map, key, currentIndent, indentOffset = 0, withinMap = key, parentMap = withinMap)
                }
            } else {
                openEnded = false
                config.setMapValue(this, map, key, currentIndent, indentOffset = 0, withinMap = key, parentMap = withinMap)
            }
            return false
        }

        if (addValue(firstKey)) {
            return map
        }
        reader.nextLine()
        while (reader.inBounds) {
            if (!openEnded && reader.indentation > currentIndent) {
                throw IllegalArgumentException("Not allowed indented values after a key-value pair. Line ${reader.exception}")
            }
            if (reader.indentation < currentIndent) {
                return map
            }
            if (isListItem()) {
                if (openEnded) {
                    config.setMapValue(this, map, firstKey, currentIndent, indentOffset = 0, withinMap = firstKey, parentMap = withinMap)
                    continue
                } else {
                    throw IllegalArgumentException("Not allowed list items in a map. Line ${reader.exception}")
                }
            }
            val key = type().toString()
            if (reader.outBounds) {
                config.setEmpty(map, key)
                return map
            } else if (reader.char == ':') {
                if (addValue(key)) {
                    return map
                }
            } else if (explicit.isOpeningTerminator(reader.char)) {
                openEnded = true
                config.setEmpty(map, key)
            } else {
                throw IllegalArgumentException("Found unknown map value for key '$key' at ${reader.exception}")
            }
            reader.nextLine()
        }
        return map
    }
}

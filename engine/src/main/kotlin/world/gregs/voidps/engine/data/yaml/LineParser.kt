package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader

class LineParser(
    reader: CharArrayReader,
    private val collection: CollectionFactory,
    private val explicit: ExplicitParser
) : ValueParser(reader) {

    override fun explicitList() = explicit.explicitList()

    override fun explicitMap() = explicit.explicitMap()

    override fun collection(indentOffset: Int, withinMap: Boolean): Any {
        return if (isListItem()) {
            list(withinMap)
        } else {
            val value = type()
            if (reader.inBounds && reader.char == ':') {
                map(value.toString(), indentOffset)
            } else {
                return value
            }
        }
    }

    private fun isListItem() = reader.char == '-' && reader.nextCharEmpty()

    private fun list(withinMap: Boolean): Any {
        val list = collection.createList()
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
                if (withinMap) {
                    return list
                }
                throw IllegalArgumentException("Expected list item at ${reader.exception}")
            }
            reader.skip(2)
            reader.skipSpaces()
            collection.addListItem(this, list, indentOffset = 1, withinMap = false)
            reader.nextLine()
        }
        return list
    }

    private fun map(firstKey: String, indentOffset: Int): Any {
        val map = collection.createMap()
        var openEnded = false
        val currentIndent = reader.indentation + indentOffset

        fun addValue(key: String): Boolean {
            reader.skip() // skip :
            reader.skipSpaces()
            if (reader.outBounds) {
                collection.setEmptyMapValue(map, key)
                return true
            } else if (explicit.isOpeningTerminator(reader.char)) {
                reader.nextLine()
                if (reader.indentation < currentIndent || reader.indentation == currentIndent && reader.char != '-') {
                    collection.setEmptyMapValue(map, key)
                } else {
                    openEnded = true
                    collection.setMapValue(this, map, key, indentOffset = 0, withinMap = true)
                }
            } else {
                openEnded = false
                collection.setMapValue(this, map, key, indentOffset = 0, withinMap = true)
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
                    collection.setMapValue(this, map, firstKey, indentOffset = 0, withinMap = true)
                    continue
                } else {
                    throw IllegalArgumentException("Not allowed list items in a map. Line ${reader.exception}")
                }
            }
            val key = type().toString()
            if (reader.outBounds) {
                collection.setEmptyMapValue(map, key)
                return map
            } else if (reader.char == ':') {
                if (addValue(key)) {
                    return map
                }
            } else if (explicit.isOpeningTerminator(reader.char)) {
                openEnded = true
                collection.setEmptyMapValue(map, key)
            } else {
                throw IllegalArgumentException("Found unknown map value for key '$key' at ${reader.exception}")
            }
            reader.nextLine()
        }
        return map
    }
}
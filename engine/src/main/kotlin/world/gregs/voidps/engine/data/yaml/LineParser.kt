package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader

open class LineParser(
    reader: CharArrayReader,
    val collection: CollectionFactory,
    private val explicit: ExplicitParser
) : ValueParser(reader) {

    override fun parseExplicitList() = explicit.parseExplicitList()

    override fun parseExplicitMap() = explicit.parseExplicitMap()

    override fun parseCollection(indentOffset: Int, withinMap: Boolean): Any {
        return if (reader.isListItem()) {
            list(withinMap)
        } else {
            val value = parseType()
            if (reader.inBounds && reader.char == ':') {
                map(value.toString(), indentOffset)
            } else {
                return value
            }
        }
    }

    fun map(firstKey: String, indentOffset: Int): Any {
        val map = collection.createMap()
        reader.skip() // skip colon
        reader.skipSpaces()
        if (reader.outBounds) {
            collection.setEmptyMapValue(map, firstKey)
            return map
        }
        var openEnded = false
        val currentIndent = reader.indentation + indentOffset
        if (reader.isLineEnd()) {
            reader.nextLine()
            if (reader.indentation < currentIndent) {
                collection.setEmptyMapValue(map, firstKey)
                return map
            } else if (reader.indentation == currentIndent && !reader.isListItem()) {
                openEnded = true
                collection.setEmptyMapValue(map, firstKey)
            } else {
                collection.setMapValue(this, map, firstKey, 0, true)
            }
        } else {
            collection.setMapValue(this, map, firstKey, 0, true)
        }
        reader.nextLine()
        while (reader.inBounds) {
            if (!openEnded && reader.indentation > currentIndent) {
                throw IllegalArgumentException("Not allowed indented values after a key-value pair. Line ${reader.exception}")
            }
            if (reader.indentation < currentIndent) {
                return map
            }
            if (reader.isListItem()) {
                if (openEnded) {
                    collection.setMapValue(this, map, firstKey, 0, true)
                    continue
                } else {
                    throw IllegalArgumentException("Not allowed list items in a map. Line ${reader.exception}")
                }
            }
            val key = parseType().toString()
            if (reader.outBounds) {
                collection.setEmptyMapValue(map, key)
                return map
            } else if (reader.char == ':') {
                reader.skip() // skip :
                reader.skipSpaces()
                if (reader.outBounds) {
                    collection.setEmptyMapValue(map, key)
                    return map
                } else if (reader.isLineEnd()) {
                    reader.nextLine()
                    if (reader.indentation < currentIndent || reader.indentation == currentIndent && !reader.isListItem()) {
                        collection.setEmptyMapValue(map, key)
                    } else {
                        openEnded = true
                        collection.setMapValue(this, map, key, 0, true)
                    }
                } else {
                    openEnded = false
                    collection.setMapValue(this, map, key, 0, true)
                }
            } else if (reader.isLineEnd()) {
                openEnded = true
                collection.setEmptyMapValue(map, key)
            } else {
                throw IllegalArgumentException("Found unknown map value for key '$key' at ${reader.exception}")
            }
            reader.nextLine()
        }
        return map
    }

    fun list(withinMap: Boolean): Any {
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
            if (reader.char != '-' || reader.next != ' ') {
                if (withinMap) {
                    return list
                }
                throw IllegalArgumentException("Expected list item at ${reader.exception}")
            }
            reader.skip(2)
            reader.skipSpaces()
            collection.addListItem(this, list, 1, false)
            reader.nextLine()
        }
        return list
    }
}
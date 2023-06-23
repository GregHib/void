package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader

abstract class LineParser(reader: CharArrayReader, val collection: CollectionFactory, override val explicit: ExplicitParser) : ValueParser(reader) {

    abstract fun addListItem(list: MutableList<Any>)

    abstract fun setMapValue(map: MutableMap<String, Any>, key: String)

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
            addListItem(list)
            reader.nextLine()
        }
        return list
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
                setMapValue(map, firstKey)
            }
        } else {
            setMapValue(map, firstKey)
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
                    setMapValue(map, firstKey)
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
                        setMapValue(map, key)
                    }
                } else {
                    openEnded = false
                    setMapValue(map, key)
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

    private fun reachedEnd(): Boolean {
        reader.skipSpaces()
        if (reader.end) {
            return true
        }
        return reader.isTerminator(reader.char)
    }

    fun parseType(): Any {
        if (reader.outBounds) {
            return ""
        } else if (reader.char == '"') {
            val quoted = reader.parseQuote()
            if (reader.inBounds && reader.char == ' ') {
                reader.skipSpaces()
            }
            return quoted
        }
        val start = reader.index
        var char = reader.char
        if (reader.isTrue(char)) {
            reader.skip(4)
            if (reachedEnd() || (reader.char == ':' && reader.nextCharEmpty())) {
                return true
            }
        } else if (reader.isFalse(char)) {
            reader.skip(5)
            if (reachedEnd() || (reader.char == ':' && reader.nextCharEmpty())) {
                return false
            }
        } else if (char == '-' || reader.isNumber(char)) {
            val number = number(start)
            if (number != null) {
                return number
            }
        }
        var end = -1
        var previous = ' '
        while (reader.inBounds) {
            char = reader.char
            if (reader.isTerminator(char)) {
                break
            } else if (char == ' ' && previous != ' ') {
                end = reader.index
            } else if (char == ':' && (reader.index + 1 == reader.size || (reader.index + 1 < reader.size && (reader.next == ' ' || reader.isTerminator(reader.next))))) {
                return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the key
            }
            previous = char
            reader.skip()
        }
        return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the value
    }

    private fun number(start: Int): Any? {
        reader.skip() // skip first
        var decimal = false
        while (reader.inBounds) {
            when (reader.char) {
                '\n', '\r', '#' -> return reader.number(decimal, start, reader.index)
                ' ' -> {
                    val end = reader.index
                    return if (reachedEnd()) reader.number(decimal, start, end) else null
                }
                '.' -> if (!decimal) decimal = true else return null
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                }
                ':' -> return if (reader.nextCharEmpty()) {
                    reader.number(decimal, start, reader.index)
                } else {
                    null
                }
                else -> return null
            }
            reader.skip()
        }
        return reader.number(decimal, start, reader.index) // End of file
    }
}
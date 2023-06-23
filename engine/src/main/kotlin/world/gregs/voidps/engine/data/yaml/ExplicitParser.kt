package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader

open class ExplicitParser(reader: CharArrayReader, val collection: CollectionFactory) : ValueParser(reader) {

    override val explicit: ExplicitParser = this

    override fun parseCollection(indentOffset: Int, withinMap: Boolean): Any {
        val value = parseExplicitType()
        return if (reader.inBounds && reader.char == ':') {
            mapExplicit(value.toString())
        } else {
            value
        }
    }

    private fun mapExplicit(key: String): Map<String, Any> {
        val map = collection.createMap()
        reader.skip() // skip colon
        reader.skipSpaces()
        if (reader.outBounds) {
            collection.setEmptyMapValue(map, key)
            return map
        }
        val currentIndent = reader.indentation
        if (reader.isLineEnd()) {
            reader.nextLine()
            if (reader.indentation < currentIndent) {
                collection.setEmptyMapValue(map, key)
                return map
            } else if (reader.indentation == currentIndent && !reader.isListItem()) {
                collection.setEmptyMapValue(map, key)
            } else {
                collection.setMapValue(this, map, key, 0, true)
            }
        } else {
            collection.setMapValue(this, map, key, 0, true)
        }
        return map
    }


    fun parseExplicitType(): Any {
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
            if (reachedExplicitEnd() || (reader.char == ':' && reader.nextCharEmpty())) {
                return true
            }
        } else if (reader.isFalse(char)) {
            reader.skip(5)
            if (reachedExplicitEnd() || (reader.char == ':' && reader.nextCharEmpty())) {
                return false
            }
        } else if (char == '-' || reader.isNumber(char)) {
            val number = explicitNumber(start)
            if (number != null) {
                return number
            }
        }
        var end = -1
        var previous = ' '
        while (reader.inBounds) {
            char = reader.char
            if (isClosingTerminator(char)) {
                break
            } else if (char == ' ' && previous != ' ') {
                end = reader.index
            } else if (char == ':' && (reader.index + 1 == reader.size || (reader.index + 1 < reader.size && (reader.next == ' ' || reader.isOpeningTerminator(reader.next))))) {
                return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the key
            }
            previous = char
            reader.skip()
        }
        return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the value
    }


    private fun explicitNumber(start: Int): Any? {
        reader.skip() // skip first
        var decimal = false
        while (reader.inBounds) {
            when (reader.char) {
                '\n', '\r', '#', ',', '}', ']' -> return reader.number(decimal, start, reader.index)
                ' ' -> {
                    val end = reader.index
                    return if (reachedExplicitEnd()) reader.number(decimal, start, end) else null
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


    fun parseExplicitMap(): Map<String, Any> {
        val map = collection.createMap()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            val key = parseExplicitType().toString()
            if (reader.inBounds && reader.char != ':') {
                throw IllegalArgumentException("Expected key-pair value ${reader.exception}")
            }
            reader.skip() // skip colon
            reader.nextLine()
            collection.setMapValue(this, map, key, 0, false)
            reader.nextLine()
            val char = reader.char
            reader.skip()// skip comma/closing char
            when (char) {
                ',' -> reader.nextLine()
                '}' -> return map
                else -> throw IllegalArgumentException("Expecting key-value pair or end of map ${reader.exception}")
            }
        }
        return map
    }

    fun parseExplicitList(): List<Any> {
        val list = collection.createList()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            collection.addListItem(this, list, 0, false)
            reader.nextLine()
            val char = reader.char
            reader.skip() // skip comma / closing char
            when (char) {
                ',' -> reader.nextLine()
                ']' -> return list
                else -> throw IllegalArgumentException("Expecting item or end of list ${reader.exception}")
            }
        }
        return list
    }

    private fun isClosingTerminator(char: Char) = reader.linebreak(char) || char == '#' || char == '}' || char == ']' || char == ','

    private fun reachedExplicitEnd(): Boolean {
        reader.skipSpaces()
        if (reader.end) {
            return true
        }
        return isClosingTerminator(reader.char)
    }
}
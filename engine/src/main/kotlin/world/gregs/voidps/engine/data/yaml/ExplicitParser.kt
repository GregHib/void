package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader
import world.gregs.voidps.engine.data.YamlParserI

abstract class ExplicitParser(val delegate: YamlParserI, val reader: CharArrayReader) {

    abstract fun createList(): MutableList<Any>

    abstract fun createMap(): MutableMap<String, Any>

    abstract fun setEmptyMapValue(map: MutableMap<String, Any>, key: String)

    abstract fun setMapValue(map: MutableMap<String, Any>, key: String)

    abstract fun setExplicitMapValue(map: MutableMap<String, Any>, key: String)

    abstract fun addListItem(list: MutableList<Any>)


    fun parseExplicitValue(): Any {
        return when (reader.char) {
            '[' -> parseExplicitList()
            '{' -> parseExplicitMap()
            '&' -> {
                reader.skipAnchorString()
                reader.nextLine()
                delegate.parseValue()
            }
            else -> {
                val value = parseExplicitType()
                if (reader.inBounds && reader.char == ':') {
                    mapExplicit(value.toString())
                } else {
                    value
                }
            }
        }
    }

    private fun mapExplicit(key: String): Map<String, Any> {
        val map = createMap()
        reader.skip() // skip colon
        reader.skipSpaces()
        if (reader.outBounds) {
            setEmptyMapValue(map, key)
            return map
        }
        val currentIndent = reader.indentation
        if (reader.isLineEnd()) {
            reader.nextLine()
            if (reader.indentation < currentIndent) {
                setEmptyMapValue(map, key)
                return map
            } else if (reader.indentation == currentIndent && !reader.isListItem()) {
                setEmptyMapValue(map, key)
            } else {
                setMapValue(map, key)
            }
        } else {
            setMapValue(map, key)
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
        val map = createMap()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            val key = parseExplicitType().toString()
            if (reader.inBounds && reader.char != ':') {
                throw IllegalArgumentException("Expected key-pair value ${reader.exception}")
            }
            reader.skip() // skip colon
            reader.nextLine()
            setExplicitMapValue(map, key)
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
        val list = createList()
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            addListItem(list)
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
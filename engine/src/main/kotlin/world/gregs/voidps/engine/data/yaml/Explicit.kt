package world.gregs.voidps.engine.data.yaml

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.CharArrayReader
import world.gregs.voidps.engine.data.YamlParser
import world.gregs.voidps.engine.data.YamlParserI

class Explicit(val delegate: YamlParserI, val reader: CharArrayReader) {

    fun parseExplicitVal(): Any {
        return when (reader.char) {
            '[' -> parseExplicitList()
            '{' -> parseExplicitMap()
            '&' -> skipAnchorString()
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
        val map = Object2ObjectOpenHashMap<String, Any>(YamlParser.EXPECTED_MAP_SIZE)
        reader.skip() // skip colon
        reader.skipSpaces()
        if (reader.outBounds) {
            map[key] = ""
            return map
        }
        val currentIndent = reader.indentation
        if (reader.isLineEnd()) {
            reader.nextLine()
            if (reader.indentation < currentIndent) {
                map[key] = ""
                return map
            } else if (reader.indentation == currentIndent && !reader.isListItem()) {
                map[key] = ""
            } else {
                val value = delegate.parseVal(withinMap = true)
                map[key] = delegate.mapModifier(key, value)
            }
        } else {
            val value = delegate.parseVal(withinMap = true)
            map[key] = delegate.mapModifier(key, value)
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
        val map = Object2ObjectOpenHashMap<String, Any>(YamlParser.EXPECTED_EXPLICIT_MAP_SIZE)
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            val key = parseExplicitType().toString()
            if (reader.inBounds && reader.char != ':') {
                throw IllegalArgumentException("Expected key-pair value ${reader.exception}")
            }
            reader.skip() // skip colon
            reader.nextLine()
            val value = parseExplicitVal()
            map[key] = delegate.mapModifier(key, value)
            reader.nextLine()
            if (reader.char == ',') {
                reader.skip()
                reader.nextLine()
            } else if (reader.char == '}') {
                reader.skip() // skip closing char
                return map
            } else {
                throw IllegalArgumentException("Expecting key-value pair or end of map ${reader.exception}")
            }
        }
        return map
    }

    fun parseExplicitList(): List<Any> {
        val list = ObjectArrayList<Any>(YamlParser.EXPECTED_EXPLICIT_LIST_SIZE)
        reader.skip() // skip opening char
        reader.nextLine()
        while (reader.inBounds) {
            val value = parseExplicitVal()
            list.add(delegate.listModifier(value))
            reader.nextLine()
            if (reader.char == ',') {
                reader.skip()
                reader.nextLine()
            } else if (reader.char == ']') {
                reader.skip() // skip closing char
                return list
            } else {
                throw IllegalArgumentException("Expecting item or end of list ${reader.exception}")
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

    fun skipAnchorString(): Any {
        while (reader.inBounds) {
            val char = reader.char
            if (char == ' ') {
                break
            }
            if (reader.linebreak(char)) {
                break
            }
            reader.skip()
        }
        reader.nextLine()
        return delegate.parseVal()
    }
}
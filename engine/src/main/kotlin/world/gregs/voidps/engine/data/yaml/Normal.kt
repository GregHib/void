package world.gregs.voidps.engine.data.yaml

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.CharArrayReader
import world.gregs.voidps.engine.data.YamlParser
import world.gregs.voidps.engine.data.YamlParserI

class Normal(val parserI: YamlParserI, val reader: CharArrayReader) {

    fun list(withinMap: Boolean): Any {
        val list = ObjectArrayList<Any>(YamlParser.EXPECTED_LIST_SIZE)
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
            list.add(parserI.listModifier(parserI.parseValue(1)))
            reader.nextLine()
        }
        return list
    }

    fun map(key: String, indentOffset: Int): Any {
        val map = Object2ObjectOpenHashMap<String, Any>(YamlParser.EXPECTED_MAP_SIZE)
        reader.skip() // skip colon
        reader.skipSpaces()
        if (reader.outBounds) {
            map[key] = ""
            return map
        }
        var openEnded = false
        val currentIndent = reader.indentation + indentOffset
        if (reader.isLineEnd()) {
            reader.nextLine()
            if (reader.indentation < currentIndent) {
                map[key] = ""
                return map
            } else if (reader.indentation == currentIndent && !reader.isListItem()) {
                openEnded = true
                map[key] = ""
            } else {
                val value = parserI.parseValue(withinMap = true)
                map[key] = parserI.mapModifier(key, value)
            }
        } else {
            val value = parserI.parseValue(withinMap = true)
            map[key] = parserI.mapModifier(key, value)
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
                    map[key] = parserI.mapModifier(key, list(true))
                    continue
                } else {
                    throw IllegalArgumentException("Not allowed list items in a map. Line ${reader.exception}")
                }
            }
            val key = parseType().toString()
            if (reader.outBounds) {
                map[key] = ""
                return map
            } else if (reader.char == ':') {
                reader.skip() // skip :
                reader.skipSpaces()
                if (reader.outBounds) {
                    map[key] = ""
                    return map
                } else if (reader.isLineEnd()) {
                    reader.nextLine()
                    if (reader.indentation < currentIndent || reader.indentation == currentIndent && !reader.isListItem()) {
                        map[key] = ""
                    } else {
                        openEnded = true
                        val type = parserI.parseValue(withinMap = true)
                        map[key] = parserI.mapModifier(key, type)
                    }
                } else {
                    openEnded = false
                    map[key] = parserI.mapModifier(key, parserI.parseValue(withinMap = true))
                }
            } else if (reader.isLineEnd()) {
                openEnded = true
                map[key] = ""
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
package world.gregs.voidps.engine.data.yaml

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.YamlParser
import world.gregs.voidps.engine.data.YamlParserI

class Explicit(val delegate: YamlParserI) : YamlParserI by delegate {

    fun parseExplicitVal(): Any {
        return when (input[index]) {
            '[' -> parseExplicitList()
            '{' -> parseExplicitMap()
            '&' -> skipAnchorString()
            else -> {
                val value = parseExplicitType()
                if (index < size && input[index] == ':') {
                    mapExplicit(value.toString())
                } else {
                    value
                }
            }
        }
    }

    private fun mapExplicit(key: String): Map<String, Any> {
        val map = Object2ObjectOpenHashMap<String, Any>(YamlParser.EXPECTED_MAP_SIZE)
        index++ // skip colon
        skipSpaces()
        if (index >= size) {
            map[key] = ""
            return map
        }
        val currentIndent = indentation
        if (isLineEnd()) {
            nextLine()
            if (indentation < currentIndent) {
                map[key] = ""
                return map
            } else if (indentation == currentIndent && !isListItem()) {
                map[key] = ""
            } else {
                val value = parseVal(withinMap = true)
                map[key] = mapModifier(key, value)
            }
        } else {
            val value = parseVal(withinMap = true)
            map[key] = mapModifier(key, value)
        }
        return map
    }



    fun parseExplicitType(): Any {
        if (index >= size) {
            return ""
        } else if (input[index] == '"') {
            val quoted = parseQuote()
            if (index < size && input[index] == ' ') {
                skipSpaces()
            }
            return quoted
        }
        val start = index
        var char = input[index]
        if (isTrue(char)) {
            index += 4
            if (reachedExplicitEnd() || (input[index] == ':' && nextCharEmpty())) {
                return true
            }
        } else if (isFalse(char)) {
            index += 5
            if (reachedExplicitEnd() || (input[index] == ':' && nextCharEmpty())) {
                return false
            }
        } else if (char == '-' || isNumber(char)) {
            val number = explicitNumber(start)
            if (number != null) {
                return number
            }
        }
        var end = -1
        var previous = ' '
        while (index < size) {
            char = input[index]
            if (isClosingTerminator(char)) {
                break
            } else if (char == ' ' && previous != ' ') {
                end = index
            } else if (char == ':' && (index + 1 == size || (index + 1 < size && (input[index + 1] == ' ' || isOpeningTerminator(input[index + 1]))))) {
                return substring(start, if (previous != ' ' || end == -1) index else end) // Return the key
            }
            previous = char
            index++
        }
        return substring(start, if (previous != ' ' || end == -1) index else end) // Return the value
    }



    private fun explicitNumber(start: Int): Any? {
        index++ // skip first
        var decimal = false
        while (index < size) {
            when (input[index]) {
                '\n', '\r', '#', ',', '}', ']' -> return number(decimal, start, index)
                ' ' -> {
                    val end = index
                    return if (reachedExplicitEnd()) number(decimal, start, end) else null
                }
                '.' -> if (!decimal) decimal = true else return null
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                }
                ':' -> return if (nextCharEmpty()) {
                    number(decimal, start, index)
                } else {
                    null
                }
                else -> return null
            }
            index++
        }
        return number(decimal, start, index) // End of file
    }



    fun parseExplicitMap(): Map<String, Any> {
        val map = Object2ObjectOpenHashMap<String, Any>(YamlParser.EXPECTED_EXPLICIT_MAP_SIZE)
        index++ // skip opening char
        nextLine()
        while (index < size) {
            val key = parseExplicitType().toString()
            if (index < size && input[index] != ':') {
                throw IllegalArgumentException("Expected key-pair value line=$lineCount char=$charInLine '$line'")
            }
            index++ // skip colon
            nextLine()
            val value = parseExplicitVal()
            map[key] = mapModifier(key, value)
            nextLine()
            if (input[index] == ',') {
                index++
                nextLine()
            } else if (input[index] == '}') {
                index++ // skip closing char
                return map
            } else {
                throw IllegalArgumentException("Expecting key-value pair or end of map line=$lineCount char=$charInLine '$line'")
            }
        }
        return map
    }

    fun parseExplicitList(): List<Any> {
        val list = ObjectArrayList<Any>(YamlParser.EXPECTED_EXPLICIT_LIST_SIZE)
        index++ // skip opening char
        nextLine()
        while (index < size) {
            val value = parseExplicitVal()
            list.add(listModifier(value))
            nextLine()
            if (input[index] == ',') {
                index++
                nextLine()
            } else if (input[index] == ']') {
                index++ // skip closing char
                return list
            } else {
                throw IllegalArgumentException("Expecting item or end of list line=$lineCount char=$charInLine '$line'")
            }
        }
        return list
    }

    private fun isClosingTerminator(char: Char) = linebreak(char) || char == '#' || char == '}' || char == ']' || char == ','

    private fun reachedExplicitEnd(): Boolean {
        skipSpaces()
        if (index == size) {
            return true
        }
        return isClosingTerminator(input[index])
    }

    fun skipAnchorString(): Any {
        while (index < size) {
            val char = input[index]
            if (char == ' ') {
                break
            }
            if (linebreak(char)) {
                break
            }
            index++
        }
        nextLine()
        return parseVal()
    }
}
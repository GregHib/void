package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

class YamlParser : CharArrayReader() {
    var mapModifier: (key: String, value: Any) -> Any = { _, value -> value }
    var listModifier: (value: Any) -> Any = { it }

    fun parse(charArray: CharArray, length: Int = charArray.size): Any {
        set(charArray, length)
        nextLine()
        return parseVal()
    }

    fun parseVal(indentOffset: Int = 0, withinMap: Boolean = false): Any {
        return when (input[index]) {
            '[' -> parseExplicitList()
            '{' -> parseExplicitMap()
            '&' -> skipAnchorString()
            else -> if (isListItem()) {
                list(withinMap)
            } else {
                val value = parseType()
                if (index < size && input[index] == ':') {
                    map(indentOffset, value.toString())
                } else {
                    value
                }
            }
        }
    }

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

    private fun list(withinMap: Boolean = false): List<Any> {
        val list = ObjectArrayList<Any>(EXPECTED_LIST_SIZE)
        index += 2
        skipSpaces()
        val currentIndent = indentation
        val parsed = listModifier(parseVal(1))
        list.add(parsed)
        while (index < size) {
            nextLine()
            // Finished if found dented
            if (indentation < currentIndent || index >= size) {
                return list
            }
            if (indentation > currentIndent) {
                throw IllegalArgumentException("Expected aligned list item at line=$lineCount char=$char '$line'")
            }
            if (input[index] != '-' || input[index + 1] != ' ') {
                if (withinMap) {
                    return list
                }
                throw IllegalArgumentException("Expected list item at line=$lineCount char=$char '$line'")
            }
            index += 2
            skipSpaces()
            list.add(listModifier(parseVal(1)))
        }
        return list
    }

    private fun mapExplicit(key: String): Map<String, Any> {
        val map = Object2ObjectOpenHashMap<String, Any>(EXPECTED_MAP_SIZE)
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

    private fun map(indentOffset: Int, key: String): Map<String, Any> {
        val map = Object2ObjectOpenHashMap<String, Any>(EXPECTED_MAP_SIZE)
        index++ // skip colon
        skipSpaces()
        if (index >= size) {
            map[key] = ""
            return map
        }
        var openEnded = false
        val currentIndent = indentation + indentOffset
        if (isLineEnd()) {
            nextLine()
            if (indentation < currentIndent) {
                map[key] = ""
                return map
            } else if (indentation == currentIndent && !isListItem()) {
                openEnded = true
                map[key] = ""
            } else {
                val value = parseVal(withinMap = true)
                map[key] = mapModifier(key, value)
            }
        } else {
            val value = parseVal(withinMap = true)
            map[key] = mapModifier(key, value)
        }
        while (index < size) {
            nextLine()
            if (!openEnded && indentation > currentIndent) {
                throw IllegalArgumentException("Not allowed indented values after a key-value pair. Line $lineCount '$line'")
            }
            if (indentation < currentIndent || index >= size) {
                return map
            }
            if (isListItem()) {
                if (openEnded) {
                    map[key] = mapModifier(key, list(true))
                    continue
                } else {
                    throw IllegalArgumentException("Not allowed list items in a map. Line $lineCount '$line'")
                }
            }
            val key = parseType().toString()
            if (index >= size) {
                map[key] = ""
                return map
            } else if (input[index] == ':') {
                index++ // skip :
                skipSpaces()
                if (index >= size) {
                    map[key] = ""
                    return map
                } else if (isLineEnd()) {
                    nextLine()
                    if (indentation == currentIndent && !isListItem()) {
                        map[key] = ""
                    } else {
                        openEnded = true
                        val type = parseVal(withinMap = true)
                        map[key] = mapModifier(key, type)
                    }
                } else {
                    openEnded = false
                    map[key] = mapModifier(key, parseVal(withinMap = true))
                }
            } else if (isLineEnd()) {
                openEnded = true
                map[key] = ""
            } else {
                println("Unknown '$key'")
            }
        }
        return map
    }

    private fun isLineEnd() = isOpeningTerminator(input[index])

    private fun nextCharEmpty() = index + 1 < size && input[index + 1] == ' '

    private fun isListItem() = input[index] == '-' && nextCharEmpty()

    private fun isTerminator(char: Char) = linebreak(char) || char == '#'

    private fun isClosingTerminator(char: Char) = linebreak(char) || char == '#' || char == '}' || char == ']' || char == ','

    private fun isOpeningTerminator(char: Char) = linebreak(char) || char == '#' || char == '{' || char == '['

    private fun isFalse(char: Char) = char == 'f' && index + 4 < size && input[index + 1] == 'a' && input[index + 2] == 'l' && input[index + 3] == 's' && input[index + 4] == 'e'

    private fun isTrue(char: Char) = char == 't' && index + 3 < size && input[index + 1] == 'r' && input[index + 2] == 'u' && input[index + 3] == 'e'

    private fun reachedEnd(): Boolean {
        skipSpaces()
        if (index == size) {
            return true
        }
        return isTerminator(input[index])
    }

    private fun reachedExplicitEnd(): Boolean {
        skipSpaces()
        if (index == size) {
            return true
        }
        return isClosingTerminator(input[index])
    }

    private fun isNumber(char: Char) = char == '0' || char == '1' || char == '2' || char == '3' || char == '4' || char == '5' || char == '6' || char == '7' || char == '8' || char == '9'

    fun parseType(): Any {
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
            if (reachedEnd() || (input[index] == ':' && nextCharEmpty())) {
                return true
            }
        } else if (isFalse(char)) {
            index += 5
            if (reachedEnd() || (input[index] == ':' && nextCharEmpty())) {
                return false
            }
        } else if (char == '-' || isNumber(char)) {
            val number = number(start)
            if (number != null) {
                return number
            }
        }
        var end = -1
        var previous = ' '
        while (index < size) {
            char = input[index]
            if (isTerminator(char)) {
                break
            } else if (char == ' ' && previous != ' ') {
                end = index
            } else if (char == ':' && (index + 1 == size || (index + 1 < size && (input[index + 1] == ' ' || isTerminator(input[index + 1]))))) {
                return substring(start, if (previous != ' ' || end == -1) index else end) // Return the key
            }
            previous = char
            index++
        }
        return substring(start, if (previous != ' ' || end == -1) index else end) // Return the value
    }

    private fun parseQuote(): String {
        index++ // skip opening quote
        val start = index
        while (index < size) {
            if (input[index] == '"') {
                break
            }
            index++
        }
        return substring(start, index++) // skip closing quote
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

    private fun number(start: Int): Any? {
        index++ // skip first
        var decimal = false
        while (index < size) {
            when (input[index]) {
                '\n', '\r', '#' -> return number(decimal, start, index)
                ' ' -> {
                    val end = index
                    return if (reachedEnd()) number(decimal, start, end) else null
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

    private fun number(decimal: Boolean, start: Int, end: Int): Any {
        val string = substring(start, end)
        return if (decimal) {
            string.toDouble()
        } else {
            val long = string.toLong()
            if (long <= Int.MAX_VALUE) long.toInt() else long
        }
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

    fun parseExplicitMap(): Map<String, Any> {
        val map = Object2ObjectOpenHashMap<String, Any>(EXPECTED_EXPLICIT_MAP_SIZE)
        index++ // skip opening char
        while (index < size) {
            nextLine()
            if (index >= size) {
                return map
            }
            val key = parseExplicitType().toString()
            if (index < size && input[index] != ':') {
                throw IllegalArgumentException("Expected key-pair value line=$lineCount char=$char '$line'")
            }
            index++ // skip colon
            nextLine()
            val value = parseExplicitVal()
            map[key] = mapModifier(key, value)
            nextLine()
            if (input[index] == ',') {
                index++
            } else if (input[index] == '}') {
                index++ // skip closing char
                return map
            }
        }
        return map
    }

    fun parseExplicitList(): List<Any> {
        val list = ObjectArrayList<Any>(EXPECTED_EXPLICIT_LIST_SIZE)
        index++ // skip opening char
        while (index < size) {
            nextLine()
            if (index >= size) {
                return list
            }
            val value = parseExplicitVal()
            list.add(listModifier(value))
            nextLine()
            if (input[index] == ',') {
                index++
            } else if (input[index] == ']') {
                index++ // skip closing char
                return list
            }
        }
        return list
    }

    companion object {
        private const val EXPECTED_LIST_SIZE = 2
        private const val EXPECTED_EXPLICIT_LIST_SIZE = 2
        private const val EXPECTED_MAP_SIZE = 8
        private const val EXPECTED_EXPLICIT_MAP_SIZE = 5
    }
}
package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

class FinalYamlParser : CharArrayReader() {
    var mapModifier: (key: String, value: Any) -> Any = { _, value -> value }
    var listModifier: (value: Any) -> Any = { it }

    fun parse(charArray: CharArray, length: Int = charArray.size): Any {
        set(charArray, length)
        nextLine()
        return parseVal(0, size)
    }

    fun parseVal(indentOffset: Int, limit: Int, withinMap: Boolean = false): Any {
        return when (input[index]) {
            '[' -> parseExplicitList(limit)
            '{' -> parseExplicitMap(limit)
            '&' -> skipAnchorString(limit)
            else -> if (isListItem(size)) {
                list(limit, withinMap)
            } else {
                val value = parseType(limit)
                if (index < limit && input[index] == ':') {
                    map(indentOffset, value.toString(), limit)
                } else {
                    value
                }
            }
        }
    }

    fun parseExplicitVal(limit: Int): Any {
        return when (input[index]) {
            '[' -> parseExplicitList(limit)
            '{' -> parseExplicitMap(limit)
            '&' -> skipAnchorString(limit)
            else -> if (isListItem(size)) {
                throw IllegalArgumentException("List items not allowed within explicit collections. $lineCount $char '$line'")
            } else {
                val value = parseType(limit)
                if (index < limit && input[index] == ':') {
                    map(0, value.toString(), limit) // TODO
                } else {
                    value
                }
            }
        }
    }

    private fun list(limit: Int, withinMap: Boolean = false): List<Any> {
        val list = ObjectArrayList<Any>(EXPECTED_LIST_SIZE)
        index += 2
        skipSpaces(limit)
        val currentIndent = indentation// + indentOffset
        val parsed = listModifier(parseVal(1, limit))
        list.add(parsed)
        while (index < limit) {
            nextLine()
            // Finished if found dented
            if (indentation < currentIndent || index >= limit) {
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
            skipSpaces(limit)
            list.add(listModifier(parseVal(1, limit)))
        }
        return list
    }

    private fun map(indentOffset: Int, key: String, limit: Int): Map<String, Any> {
        val map = Object2ObjectOpenHashMap<String, Any>(EXPECTED_MAP_SIZE)
        index++ // skip colon
        skipSpaces(limit)
        if (index >= limit) {
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
            } else if (indentation == currentIndent && !isListItem(limit)) {
                openEnded = true
                map[key] = ""
            } else {
                val value = parseVal(0, limit, true)
                map[key] = mapModifier(key, value)
            }
        } else {
            val value = parseVal(0, limit, true)
            map[key] = mapModifier(key, value)
        }
        while (index < limit) {
            nextLine()
            if (!openEnded && indentation > currentIndent) {
                throw IllegalArgumentException("Not allowed indented values after a key-value pair. Line $lineCount '$line'")
            }
            if (indentation < currentIndent || index >= limit) {
                return map
            }
            if (isListItem(limit)) {
                if (openEnded) {
                    map[key] = mapModifier(key, list(limit, true))
                    continue
                } else {
                    throw IllegalArgumentException("Not allowed list items in a map. Line $lineCount '$line'")
                }
            }
            val key = parseType(limit).toString()
            if (index >= limit) {
                map[key] = ""
                return map
            } else if (input[index] == ':') {
                index++ // skip :
                skipSpaces(limit)
                if (index >= limit) {
                    map[key] = ""
                    return map
                } else if (isLineEnd()) {
                    nextLine()
                    if (indentation == currentIndent && !isListItem(limit)) {
                        map[key] = ""
                    } else {
                        openEnded = true
                        val type = parseVal(0, limit, true)
                        map[key] = mapModifier(key, type)
                    }
                } else {
                    openEnded = false
                    map[key] = mapModifier(key, parseVal(0, limit, true))
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

    private fun isLineEnd() = linebreak(input[index]) || input[index] == '#' || input[index] == '{' || input[index] == '['

    private fun isListItem(limit: Int) = input[index] == '-' && index + 1 < limit && input[index + 1] == ' '

    private fun reachedEnd(limit: Int): Boolean {
        skipSpaces(limit)
        if (index == limit) {
            return true
        }
        if (linebreak(input[index])) {
            return true
        }
        return input[index] == '#' || input[index] == '}' || input[index] == ']' || input[index] == ','
    }

    fun parseType(limit: Int = size): Any {
        if (index >= limit) {
            return ""
        } else if (input[index] == '"') {
            index++ // skip opening quote
            val start = index
            while (index < limit) {
                if (input[index] == '"') {
                    break
                }
                index++
            }
            val quoted = substring(start, index)
            index++ // skip closing quote
            if (index < limit && input[index] == ' ') {
                skipSpaces(limit)
            }
            return quoted
        }
        val start = index
        var char = input[index]
        if (char == 't' && index + 3 < limit && input[index + 1] == 'r' && input[index + 2] == 'u' && input[index + 3] == 'e') {
            index += 4
            if (reachedEnd(limit) || (input[index] == ':' && index + 1 < limit && input[index + 1] == ' ')) {
                return true
            }
        } else if (char == 'f' && index + 4 < limit && input[index + 1] == 'a' && input[index + 2] == 'l' && input[index + 3] == 's' && input[index + 4] == 'e') {
            index += 5
            if (reachedEnd(limit) || (input[index] == ':' && index + 1 < limit && input[index + 1] == ' ')) {
                return false
            }
        } else if (char == '-' || char == '0' || char == '1' || char == '2' || char == '3' || char == '4' || char == '5' || char == '6' || char == '7' || char == '8' || char == '9') {
            val number = number(start, limit)
            if (number != null) {
                return number
            }
        }
        var end = -1
        var previous = ' '
        while (index < limit) {
            char = input[index]
            if (linebreak(char) || char == '#' || char == '}' || char == ']' || char == ',') {
                break
            } else if (char == ' ' && previous != ' ') {
                end = index
            } else if (char == ':' && (index + 1 == limit || (index + 1 < limit && (input[index + 1] == ' ' || linebreak(input[index + 1]) || input[index + 1] == '#' || input[index + 1] == '{' || input[index + 1] == '[')))) {
                return substring(start, if (previous != ' ') index else end) // Return the key
            }
            previous = char
            index++
        }
        return substring(start, if (previous != ' ' || end == -1) index else end) // Return the value
    }

    private fun number(start: Int, limit: Int): Any? {
        index++ // skip first
        var decimal = false
        while (index < limit) {
            when (input[index]) {
                '\n', '\r', '#', ',', '}', ']' -> return number(decimal, start, index)
                ' ' -> {
                    val end = index
                    return if (reachedEnd(limit)) number(decimal, start, end) else null
                }
                '.' -> if (!decimal) decimal = true else return null
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                }
                ':' -> {
                    if (index + 1 < limit && input[index + 1] == ' ') {
                        return number(decimal, start, index)
                    } else {
                        return null
                    }
                }
                else -> return null
            }
            index++
        }
        return number(decimal, start, index) // End of file
    }

    private fun parseQuotedString(limit: Int = size): String {
        index++ // skip opening quote
        val start = index
        while (index < limit) {
            when (input[index]) {
                '\\' -> index++ // escaped
                '"' -> {
                    val string = substring(start, index)
                    index++ // skip closing quote
                    return string
                }
            }
            index++
        }
        throw IllegalArgumentException("Expected closing quote at index $index")
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

    fun skipAnchorString(limit: Int = size): Any {
        while (index < limit) {
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
        return parseVal(0, limit)
    }

    fun parseExplicitMap(limit: Int = size): Map<String, Any> {
        val map = Object2ObjectOpenHashMap<String, Any>(EXPECTED_EXPLICIT_MAP_SIZE)
        index++ // skip opening char
        while (index < limit) {
            nextLine()
            if (index >= limit) {
                return map
            }

            val key = parseType(limit).toString()
            if (index < limit && input[index] != ':') {
                throw IllegalArgumentException("Expected key-pair value line=$lineCount char=$char '$line'")
            }
            index++ // skip colon
            nextLine()
            val value = parseExplicitVal(limit)
            map[key] = mapModifier(key, value)
            if (input[index] == ',') {
                index++
            } else if (input[index] == '}') {
                index++ // skip closing char
                return map
            }
        }
        return map
    }

    fun parseExplicitList(limit: Int = size): List<Any> {
        val list = ObjectArrayList<Any>(EXPECTED_EXPLICIT_LIST_SIZE)
        index++ // skip opening char
        while (index < limit) {
            nextLine()
            if (index >= limit) {
                return list
            }
            val value = parseExplicitVal(limit)
            list.add(listModifier(value))
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
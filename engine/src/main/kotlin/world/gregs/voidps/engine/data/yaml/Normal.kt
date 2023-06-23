package world.gregs.voidps.engine.data.yaml

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.YamlParser
import world.gregs.voidps.engine.data.YamlParserI

class Normal(val delegate: YamlParserI) : YamlParserI by delegate {

    fun list(withinMap: Boolean): Any {
        val list = ObjectArrayList<Any>(YamlParser.EXPECTED_LIST_SIZE)
        val currentIndent = indentation
        while (index < size) {
            // Finished if found dented
            if (indentation < currentIndent) {
                return list
            }
            if (indentation > currentIndent) {
                throw IllegalArgumentException("Expected aligned list item at line=$lineCount char=$charInLine '$line'")
            }
            if (input[index] != '-' || input[index + 1] != ' ') {
                if (withinMap) {
                    return list
                }
                throw IllegalArgumentException("Expected list item at line=$lineCount char=$charInLine '$line'")
            }
            index += 2
            skipSpaces()
            list.add(listModifier(parseVal(1)))
            nextLine()
        }
        return list
    }

    fun map(key: String, indentOffset: Int): Any {
        val map = Object2ObjectOpenHashMap<String, Any>(YamlParser.EXPECTED_MAP_SIZE)
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
        nextLine()
        while (index < size) {
            if (!openEnded && indentation > currentIndent) {
                throw IllegalArgumentException("Not allowed indented values after a key-value pair. Line $lineCount '$line'")
            }
            if (indentation < currentIndent) {
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
                    if (indentation < currentIndent || indentation == currentIndent && !isListItem()) {
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
                throw IllegalArgumentException("Found unknown map value for key '$key' at line=${lineCount} char=${charInLine} '${line}'")
            }
            nextLine()
        }
        return map
    }

    private fun reachedEnd(): Boolean {
        skipSpaces()
        if (index == size) {
            return true
        }
        return isTerminator(input[index])
    }

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
}
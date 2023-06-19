package world.gregs.voidps.engine.data

class FinalYamlParser {

    var input = ""
    var index = 0

    val pretty: String
        get() = input.substring(index, (index + 25).coerceAtMost(input.length)).replace("\n", "\\n")

    fun parse(text: String): Any {
        set(text)
        return parseValue(0)
    }

    fun set(input: String) {
        this.input = input
        this.index = 0
    }

    fun skipComment(limit: Int = input.length, breaks: Boolean = true) {
        if (index < limit && input[index] == '#') {
            while (index < limit && input[index] != '\n' && input[index] != '\r') {
                index++
            }
            if (breaks)
                skipLineBreaks()
        }
    }

    /**
     * Skip space or line breaks
     */
    fun skipWhitespace(limit: Int = input.length) {
        while (index < limit && input[index].isWhitespace()) {
            index++
        }
    }

    fun skipSpaces(limit: Int = input.length) {
        while (index < limit && input[index] == ' ') {
            index++
        }
    }

    fun skipLineBreaks(limit: Int = input.length) {
        while (index < limit && (input[index] == '\n' || input[index] == '\r')) {
            index++
        }
    }

    fun parseKey(limit: Int = input.length): String {
        skipSpaces(limit)
        val key = if (index < input.length && input[index] == '"') {
            parseQuotedKey()
        } else {
            parseScalarKey()
        }
        index++ // skip ':'
        skipSpaces(limit)
        skipComment(limit, false)
        return key
    }

    private fun parseQuotedKey(): String {
        val key = parseQuotedString()
        skipSpaces()
        if (index == input.length || (index < input.length && input[index] != ':')) {
            throw IllegalArgumentException("Expected ':' at index $index")
        }
        return key
    }

    private fun parseQuotedString(limit: Int = input.length): String {
        index++ // skip opening '"'
        val start = index
        var escaped = false
        while (index < limit && (!escaped && input[index] != '"') && input[index] != '\n' && input[index] != '\r') {
            escaped = input[index] == '\\'
            index++
        }
        if (index == limit || (index < limit && (input[index] != '"' || input[index] == '\r' || input[index] == '\n'))) {
            throw IllegalArgumentException("Expected '\"' at index $index")
        }
        val end = index
        index++ // skip closing '"'
        skipSpaces(limit)
        skipLineBreaks(limit)
        return input.substring(start, end)
    }

    private fun parseScalarKey(limit: Int = input.length): String {
        val start = index
        index = colonLookAhead(limit, false)
        if (index == input.length || (index < input.length && (input[index] != ':' || input[index] == '\n' || input[index] == '\r'))) {
            throw IllegalArgumentException("Expected ':' at index $index")
        }
        return input.substring(start, index).trim()
    }

    /**
     * Expect no spaces
     */
    private fun parseType(currentIndent: Int, limit: Int): Any {
        if (index == input.length) {
            throw IllegalStateException("Unexpected end of file")
        }
        return when (input[index]) {
            '[' -> parseExplicitList(limit)
            '{' -> parseExplicitMap(limit)
            '"' -> parseQuotedString(limit)
            '&' -> parseAnchorString(currentIndent, limit)
            else -> parseScalar(limit)
        }
    }

    fun parseScalar(limit: Int = input.length): Any {
        if (index == limit) {
            return ""
        }
        val start = index
        val char = input[index]
        if (char == '\n' || char == '\t') {
            index++
            return ""
        }
        if (char == 't' && index + 3 < limit && input[index + 1] == 'r' && input[index + 2] == 'u' && input[index + 3] == 'e' && isEnd(4, limit)) {
            index += 4
            skipComment(limit)
            skipLineBreaks(limit)
            return true
        } else if (char == 'f' && index + 4 < limit && input[index + 1] == 'a' && input[index + 2] == 'l' && input[index + 3] == 's' && input[index + 4] == 'e' && isEnd(5, limit)) {
            index += 5
            skipComment(limit)
            skipLineBreaks(limit)
            return false
        } else if (char.isDigit() || char == '-') {
            index++ // skip first
            var decimals = 0
            var digit = true
            var end = -1
            while (index < limit && input[index] != '\n' && input[index] != '\r' && input[index] != '#') {
                if (input[index] == '.') {
                    decimals++
                } else if (end == -1 && input[index] == ' ') {
                    end = index
                } else if (end != -1 && input[index] != ' ') {
                    digit = false
                    break
                } else if (!input[index].isDigit()) {
                    digit = false
                    break
                }
                index++
            }
            if (digit) {
                if (end == -1) {
                    end = index
                }
                if (decimals == 1) {
                    val double = input.substring(start, end).toDouble()
                    skipComment(limit)
                    skipLineBreaks(limit)
                    return double
                } else if (decimals == 0) {
                    val long = input.substring(start, end).toLong()
                    skipComment(limit)
                    skipLineBreaks(limit)
                    return if (long <= Int.MAX_VALUE) long.toInt() else long
                }
            }
        }
        while (index < limit && input[index] != '\n' && input[index] != '\r' && input[index] != '#') {
            index++
        }
        val end = index
        skipComment(limit)
        skipLineBreaks(limit)
        return input.substring(start, end).trimEnd()
    }

    private fun isEnd(offset: Int, limit: Int) = index + offset == limit || input[index + offset] == '\n' || input[index + offset] == '\r' || input[index + offset] == '#'

    fun parseAnchorString(currentIndent: Int, limit: Int = input.length): Any {
        val start = index
        while (index < limit && input[index] != '\r' && input[index] != '\n') {
            index++
        }
        var end = index
        skipLineBreaks(limit)
        var count = 0
        while (count++ < LIST_MAXIMUM && index < limit) {
            val indent = peekIndent(limit)
            if (indent != currentIndent) {
                break
            } else {
                while (index < limit && input[index] != '\r' && input[index] != '\n') {
                    index++
                }
                end = index
                skipLineBreaks(limit)
            }
        }
        return input.substring(start, end)
    }

    fun parseExplicitList(limit: Int = input.length): List<Any> {
        val list = mutableListOf<Any>()
        skipSpaces(limit)
        index++ // skip '['
        skipWhitespace(limit)
        var depth = 1
        var escaped = false
        var count = 0
        // For n number of items
        outer@ while (count++ < LIST_MAXIMUM && index < limit) {
            // Peek ahead for the index of the next comma
            var nextComma = -1
            var temp = index
            while (depth != 0) {
                if (!escaped && depth == 1 && input[temp] == ',') {
                    // Found a base level comma
                    nextComma = temp
                    break
                } else if (!escaped && input[temp] == '[') { // Enter into a nested list
                    depth++
                } else if (!escaped && input[temp] == ']') { // Exist out of a nested list
                    if (--depth == 0) {
                        // Found the end of the list
                        addListItem(list, limit, temp)
                        if (index < limit && input[index] == ']') {
                            index++ // skip ']'
                            skipSpaces(limit)
                            skipLineBreaks(limit)
                        }
                        break@outer
                    }
                }
                escaped = input[temp] == '\\'
                temp++
            }
            if (nextComma == -1) {
                // Add what's remaining if end of list found
                if (temp < limit && input[temp] == ']') {
                    index = temp + 1 // skip ']'
                    skipSpaces(limit)
                    skipLineBreaks(limit)
                    return list
                } else {
                    throw IllegalStateException("Unable to find comma or end of list.")
                }
            } else {
                addListItem(list, limit, nextComma)
            }
        }
        return list
    }

    private fun addListItem(list: MutableList<Any>, limit: Int, nextComma: Int) {
        skipWhitespace(limit)
        val parsed = parseValue(0, nextComma)
        list.add(parsed)
        skipWhitespace(limit)
        if (index < limit && input[index] == ',') {
            index++ // skip ','
            skipWhitespace(limit)
        }
    }

    fun parseList(currentIndent: Int, limit: Int = input.length, nestedMap: Boolean = false): List<Any> {
        val list = mutableListOf<Any>()
        var count = 0
        while (count++ < LIST_MAXIMUM && index < limit) {
            val indent = peekIndent(limit)
            val peek = input[index + (indent * 2)]
            val second = input.getOrNull(index + (indent * 2) + 1)
            if (peek == '#') {
                skipSpaces(limit)
                skipComment(limit)
            } else if (list.isNotEmpty() && indent < currentIndent) {
                break
            } else if (peek != '-' || second != ' ' || indent > currentIndent) {
                if (indent == currentIndent && nestedMap)
                    break
                throw IllegalArgumentException("Expected list item at index index=${index + (indent * 2)} indent=$indent current=$currentIndent")
            } else {
                skipSpaces(limit)
                index++ // skip -
                skipSpaces(limit)
                val parsed = parseValue(currentIndent + 1, limit)
                list.add(parsed)
                skipLineBreaks()
            }
        }
        return list
    }

    fun parseMap(currentIndent: Int, limit: Int = input.length): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        var count = 0
        while (count++ < MAP_MAXIMUM && index < limit) {
            var indent = peekIndent(limit)
            var peek = input[index + (indent * 2)]
            if (peek == '#') {
                skipSpaces(limit)
                skipComment(limit)
                continue
            }
            if (map.isNotEmpty() && indent < currentIndent) {
                break
            }
            val (key, value) = parseKeyValuePair(currentIndent, limit)
            if (value != null) {
                map[key] = value
            } else {
                indent = peekIndent(limit)
                peek = input[index + (indent * 2)]
                if (peek == '#') {
                    skipSpaces(limit)
                    skipComment(limit)
                }
                // Look ahead to find out the next type of line
                val colonIndex = simpleColonLookAhead(limit)
                // If key-value pair on same level then this value is null
                if (indent == currentIndent && colonIndex < limit && input[colonIndex] == ':') {
                    map[key] = ""
                } else if (input[index + (peekIndent(limit) * 2)] == '#') {
                    skipSpaces(limit)
                    skipComment(limit)
                } else if (indent >= currentIndent) {
                    map[key] = parseValue(currentIndent + 1, limit)
                }
            }
        }
        return map
    }

    fun parseKeyValuePair(currentIndent: Int, limit: Int = input.length): Pair<String, Any?> {
        val key = parseKey(limit)
        return if (index == limit) { // end of file
            key to null
        } else if (index < limit && (input[index] == '\n' || input[index] == '\r')) { // end of line
            skipLineBreaks()
            val index = simpleColonLookAhead(limit)
            if (index >= limit || input[index] == ':') { // if next line is a key-value pair
                key to null
            } else {
                val indent = peekIndent(limit)
                val value = parseValue(if (indent > currentIndent) currentIndent + 1 else currentIndent, limit, true)
                key to value
            }
        } else {
            key to parseValue(currentIndent, limit)
        }
    }

    fun parseValue(currentIndent: Int, limit: Int = input.length, nestedMap: Boolean = false): Any {
        val indent = peekIndent(limit)
        val index = index + (indent * 2)
        return when {
            input[index] == '-' && index + 1 < limit && input[index + 1] == ' ' -> parseList(currentIndent, limit, nestedMap)
            input[index] == '#' -> {
                skipSpaces(limit)
                skipComment(limit)
                parseValue(currentIndent, limit)
            }
            input[index] == '[' -> {
                skipSpaces(limit)
                parseExplicitList(limit)
            }
            input[index] == '{' -> {
                skipSpaces(limit)
                parseExplicitMap(limit)
            }
            input[index] == '&' -> {
                skipSpaces(limit)
                parseAnchorString(currentIndent, limit)
            }
            else -> {
                val colonIndex = colonLookAhead(limit, false)
                if (colonIndex < limit && input[colonIndex] == ':') {
                    parseMap(currentIndent, limit)
                } else {
                    parseType(currentIndent, limit)
                }
            }
        }
    }

    fun simpleColonLookAhead(limit: Int = input.length): Int {
        var temp = index
        // Skip whitespaces
        while (temp < limit && input[temp] == ' ') {
            temp++
        }
        // Find the first colon followed by a space or end line, unless reached a terminator symbol
        while (temp < limit && !(input[temp] == ':' && temp + 1 <= limit && (temp + 1 == limit || input[temp + 1].isWhitespace() || input[temp + 1] == '#')) && input[temp] != '\r' && input[temp] != '\n') {
            temp++
        }
        return temp
    }

    fun colonLookAhead(limit: Int = input.length, skipCommentLines: Boolean = true): Int {
        var temp = index
        // Skip whitespaces
        while (temp < limit && input[temp] == ' ') {
            temp++
        }
        var escaped = false
        // Find the first colon followed by a space or end line, unless reached a terminator symbol
        while (temp < limit && !escaped && !(input[temp] == ':' && temp + 1 <= limit && (temp + 1 == limit || input[temp + 1].isWhitespace() || input[temp + 1] == '#')) && input[temp] != '\r' && input[temp] != '\n' && input[temp] != ',' && input[temp] != '{' && input[temp] != '[') {
            if (input[temp] == '"') {
                temp++
                while (temp < limit && input[temp] != '"' && input[temp] != '\n' && input[temp] != '\r') {
                    temp++
                }
                if (temp < limit && input[temp] == '"') {
                    temp++
                }
            } else if (input[temp] == '#') {
                while (temp < limit && input[temp] != '\n' && input[temp] != '\r') {
                    temp++
                }
                if (skipCommentLines) {
                    if (temp < limit && input[temp] == '\r') {
                        temp++ // skip \r
                    }
                    if (temp < limit && input[temp] == '\n') {
                        temp++ // skip \n
                    }
                }
            } else {
                escaped = input[temp] == '\\'
                temp++
            }
        }
        return temp
    }

    private fun peekIndent(limit: Int): Int {
        var temp = index
        while (temp < limit && input[temp] == ' ') {
            temp++
        }
        return (temp - index) / 2
    }

    fun parseExplicitMap(limit: Int = input.length): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        skipSpaces(limit)
        index++ // skip '{'
        skipWhitespace(limit)
        var depth = 1
        var escaped = false
        var count = 0
        outer@ while (count++ < MAP_MAXIMUM && index < limit) {
            // Peek ahead for the index of the next comma
            var nextComma = -1
            var temp = index
            while (depth != 0) {
                if (!escaped && depth == 1 && input[temp] == ',') {
                    // Found a base level comma
                    nextComma = temp
                    break
                } else if (!escaped && input[temp] == '{') { // Enter into a nested map
                    depth++
                } else if (!escaped && input[temp] == '}') { // Exist out of a nested map
                    if (--depth == 0) {
                        // Found the end of the map
                        addMapEntry(map, limit, temp)
                        if (index < limit && input[index] == '}') {
                            index++ // skip '}'
                            skipSpaces(limit)
                            skipLineBreaks(limit)
                        }
                        break@outer
                    }
                }
                escaped = input[temp] == '\\'
                temp++
            }
            if (nextComma == -1) {
                // Add what's remaining if end of map found
                if (temp < limit && input[temp] == '}') {
                    index = temp + 1 // skip '}'
                    skipSpaces(limit)
                    skipLineBreaks(limit)
                    return map
                } else {
                    throw IllegalStateException("Unable to find comma or end of map.")
                }
            } else {
                addMapEntry(map, limit, nextComma)
            }
        }
        return map
    }

    private fun addMapEntry(map: MutableMap<String, Any>, limit: Int, nextComma: Int) {
        skipWhitespace(limit)
        val key = parseKey(limit)
        skipWhitespace(limit)
        skipComment(limit)
        skipWhitespace(limit)
        val parsed = parseType(0, nextComma)
        map[key] = parsed
        skipWhitespace()
        if (index < limit && input[index] == ',') {
            index++ // skip ','
            skipWhitespace(limit)
        }
    }

    companion object {
        private const val LIST_MAXIMUM = 1_000_000
        private const val MAP_MAXIMUM = 1_000_000
    }

    /*

    TODO handle lists on same line as map
    TODO multi-line {} and [] maps/lists (explicit maps/lists?)
    TODO ignore quotes used for keys too
    TODO ignore escaped characters (just have: escaped = prev == \)
    TODO ignore empty lines
    TODO toggle for filling nulls as empty strings instead
    TODO allow nulls for keys
    TODO allow nulls as types
    TODO impl the single lines and helper functions first as they can be tested stand alone
     */
}
package world.gregs.voidps.engine.data

class FinalYamlParser {

    var input = CharArray(0)
    var size = 0
    var index = 0

    private fun substring(start: Int, end: Int) = String(input, start, end - start)

    val pretty: String
        get() = substring(index, (index + 25).coerceAtMost(size)).replace("\n", "\\n")

    fun parse(text: String): Any {
        return parse(text.toCharArray())
    }

    fun parse(charArray: CharArray, length: Int = charArray.size): Any {
        set(charArray, length)
        return parseValue(0)
    }

    fun set(input: String) {
        set(input.toCharArray())
    }

    fun set(charArray: CharArray, size: Int = charArray.size) {
        this.input = charArray
        this.size = size
        this.index = 0
    }

    fun skipComment(limit: Int = size, breaks: Boolean = true) {
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
    fun skipWhitespace(limit: Int = size) {
        while (index < limit && input[index].isWhitespace()) {
            index++
        }
    }

    fun skipSpaces(limit: Int = size) {
        while (index < limit && input[index] == ' ') {
            index++
        }
    }

    fun skipLineBreaks(limit: Int = size) {
        while (index < limit && (input[index] == '\n' || input[index] == '\r')) {
            index++
        }
    }

    fun parseKey(limit: Int = size): String {
        skipSpaces(limit)
        val key = if (index < size && input[index] == '"') {
            parseQuotedKey()
        } else {
            parseScalarKey()
        }
        skipSpaces(limit)
        index++ // skip ':'
        skipSpaces(limit)
        skipComment(limit, false)
        return key
    }

    private fun parseQuotedKey(): String {
        val key = parseQuotedString()
        skipSpaces()
        if (index == size || (index < size && input[index] != ':')) {
            throw IllegalArgumentException("Expected ':' at index $index")
        }
        return key
    }

    private fun parseQuotedString(limit: Int = size): String {
        index++ // skip opening '"'
        val start = index
        while (index < limit) {
            when (input[index]) {
                '\\' -> index++ // escaped
                '"' -> {
                    val end = index
                    index++ // skip '"'
                    skipSpaces(limit)
                    skipLineBreaks(limit)
                    return substring(start, end)
                }
                '\n', '\r' -> {
                    val end = index
                    skipLineBreaks(limit)
                    return substring(start, end)
                }
            }
            index++
        }
        if (index == limit) {
            return substring(start, index)
        }
        throw IllegalArgumentException("Expected '\"' at index $index")
    }

    private fun parseScalarKey(limit: Int = size): String {
        val start = index
        val colon = colonLookAhead(limit)
            ?: throw IllegalArgumentException("Expected ':' at index $index")
        index = colon
        return substring(start, index)
    }

    /**
     * Expect no spaces
     */
    private fun parseType(currentIndent: Int, limit: Int): Any {
        if (index == size) {
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

    private fun isFalse(limit: Int): Boolean {
        index += 5
        return isEnd(limit)
    }

    private fun isTrue(limit: Int): Boolean {
        index += 4
        return isEnd(limit)
    }

    private fun isEnd(limit: Int): Boolean {
        skipSpaces(limit)
        if (index == limit) {
            return true
        }
        if (input[index] == '\n' || input[index] == '\r') {
            skipLineBreaks(limit)
            return true
        }
        if (input[index] == '#') {
            skipComment(limit)
            skipLineBreaks(limit)
            return true
        }
        return false
    }

    private fun isNumber(start: Int, limit: Int): Any? {
        index++ // skip first
        var decimal = false
        while (index < limit) {
            when (input[index]) {
                '\n', '\r' -> {
                    val number = number(decimal, start, index)
                    skipLineBreaks(limit)
                    return number
                }
                '#' -> {
                    val number = number(decimal, start, index)
                    skipComment(limit)
                    skipLineBreaks(limit)
                    return number
                }
                ' ' -> {
                    val end = index
                    return if (isEnd(limit)) number(decimal, start, end) else null
                }
                '.' -> if (!decimal) decimal = true else return null
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {

                }
                else -> return null
            }
            index++
        }
        return number(decimal, start, index) // End of file
    }

    private fun number(decimal: Boolean, start: Int, end: Int) = if (decimal) {
        substring(start, end).toDouble()
    } else {
        val long = substring(start, end).toLong()
        if (long <= Int.MAX_VALUE) long.toInt() else long
    }


    fun parseScalar(limit: Int = size): Any {
        if (index == limit) {
            return ""
        }
        val start = index
        var char = input[index]
        if (char == '\n' || char == '\t') {
            index++
            return ""
        }
        if (char == 't' && index + 3 < limit && input[index + 1] == 'r' && input[index + 2] == 'u' && input[index + 3] == 'e') {
            if (isTrue(limit)) {
                return true
            }
        } else if (char == 'f' && index + 4 < limit && input[index + 1] == 'a' && input[index + 2] == 'l' && input[index + 3] == 's' && input[index + 4] == 'e') {
            if (isFalse(limit)) {
                return false
            }
        } else if (char == '-' || char == '0' || char == '1' || char == '2' || char == '3' || char == '4' || char == '5' || char == '6' || char == '7' || char == '8' || char == '9') {
            val number = isNumber(start, limit)
            if (number != null) {
                return number
            }
        }
        while (index < limit) {
            char = input[index]
            if (char == '\n' || char == '\r' || char == '#') {
                break
            }
            index++
        }
        val end = index
        skipComment(limit)
        skipLineBreaks(limit)
        return substring(start, end).trimEnd()
    }

    fun parseAnchorString(currentIndent: Int, limit: Int = size): Any {
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
        return substring(start, end)
    }

    fun parseExplicitList(limit: Int = size): List<Any> {
        val list = mutableListOf<Any>()
        skipSpaces(limit)
        index++ // skip '['
        skipWhitespace(limit)
        var depth = 1
        var count = 0
        // For n number of items
        outer@ while (count++ < LIST_MAXIMUM && index < limit) {
            // Peek ahead for the index of the next comma
            var nextComma = -1
            var temp = index
            while (depth != 0) {
                if (input[temp] == '\\') {
                    temp++ // escaped
                } else if (depth == 1 && input[temp] == ',') {
                    // Found a base level comma
                    nextComma = temp
                    break
                } else if (input[temp] == '[') { // Enter into a nested list
                    depth++
                } else if (input[temp] == ']') { // Exist out of a nested list
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

    fun parseList(currentIndent: Int, limit: Int = size, nestedMap: Boolean = false): List<Any> {
        val list = mutableListOf<Any>()
        var count = 0
        while (count++ < LIST_MAXIMUM && index < limit) {
            val indent = peekIndent(limit)
            val peek = input[index + (indent * 2)]
            val second = input.getOrNull(index + (indent * 2) + 1)
            if (peek == '#') {
                skipSpaces(limit)
                skipComment(limit)
            } else if (indent < currentIndent) {
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

    fun parseMap(currentIndent: Int, limit: Int = size): Map<String, Any> {
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

    fun parseKeyValuePair(currentIndent: Int, limit: Int = size): Pair<String, Any?> {
        val key = parseKey(limit) // this doesn't need to check multi-lines
        return if (index == limit) { // end of file
            key to null
        } else if (index < limit && (input[index] == '\n' || input[index] == '\r')) { // end of line
            index++ // skip line break
            if (index < limit && input[index] == '\n') { // skip windows line breaks
                index++
            }
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

    fun parseValue(currentIndent: Int, limit: Int = size, nestedMap: Boolean = false): Any {
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
                val colonIndex = colonLookAhead(limit)
                if (colonIndex != null) {
                    parseMap(currentIndent, limit)
                } else {
                    parseType(currentIndent, limit)
                }
            }
        }
    }

    fun simpleColonLookAhead(limit: Int = size): Int {
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

    fun colonLookAhead(limit: Int = size): Int? {
        var temp = index
        var end = -1
        // Find the first colon followed by a space or end line, unless reached a terminator symbol
        var previous = ' '
        while (temp < limit) {
            when (input[temp]) {
                '\\' -> temp++
                ' ' -> if (previous != ' ') end = temp // Mark end of key
                ':' -> {
                    if ((temp + 1 == limit || (temp + 1 <= limit && (input[temp + 1].isWhitespace() || input[temp + 1] == '#')))) {
                        if (previous == ' ' && end != -1) {
                            return end
                        }
                        return temp
                    }
                }
                ',' -> return null
                '[', '{' -> return null
                '\r', '\n' -> return null
                '"' -> {
                    temp++
                    while (temp < limit && input[temp] != '"' && input[temp] != '\n' && input[temp] != '\r') {
                        temp++
                    }
                    if (temp < limit && input[temp] == '"') {
                        temp++
                    }
                    continue
                }
                '#' -> {
                    while (temp < limit && input[temp] != '\n' && input[temp] != '\r') {
                        temp++
                    }
                    continue
                }
            }
            previous = input[temp]
            temp++
        }
        return null
    }

    private fun peekIndent(limit: Int): Int {
        var temp = index
        while (temp < limit && input[temp] == ' ') {
            temp++
        }
        return (temp - index) / 2
    }

    fun parseExplicitMap(limit: Int = size): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        skipSpaces(limit)
        index++ // skip '{'
        skipWhitespace(limit)
        var depth = 1
        var count = 0
        outer@ while (count++ < MAP_MAXIMUM && index < limit) {
            // Peek ahead for the index of the next comma
            var nextComma = -1
            var temp = index
            while (depth != 0) {
                if (input[temp] == '\\') {
                    temp += 2
                    continue
                }
                if (depth == 1 && input[temp] == ',') {
                    // Found a base level comma
                    nextComma = temp
                    break
                } else if (input[temp] == '{') { // Enter into a nested map
                    depth++
                } else if (input[temp] == '}') { // Exist out of a nested map
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
        val key = parseKey(limit) // this needs to check multi-lines a
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
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

    private val intRegex = Regex("-?\\d+")
    private val longRegex = Regex("-?\\d+L")
    private val doubleRegex = Regex("-?\\d+(\\.\\d+)?")

    fun parseScalar(limit: Int = input.length): Any {
        val start = index
        var digit = true // gives a rough check
        while (!(index >= limit || (input[index] == ':' && input[index + 1].isWhitespace()) || input[index] == '\n' || input[index] == '\r' || input[index] == '#')) {
            if (input[index] != '-' && input[index] != 'L') {
                if (digit && !input[index].isDigit() && input[index] != '.' && input[index] != ' ') {
                    digit = false
                }
            }
            index++
        }
        if (start == index) {
            return ""
        }
        val end = index
        skipComment(limit)
        skipLineBreaks(limit)
        val text = input.substring(start, end).trimEnd()
        return when {
            text.equals("true", true) -> true
            text.equals("false", true) -> false
            digit && text.matches(longRegex) -> text.trimEnd('L').toLong()
            digit && text.matches(intRegex) -> text.toInt()
            digit && text.matches(doubleRegex) -> text.toDouble()
            else -> text
        }
    }

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
                val colonIndex = colonLookAhead(limit)
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
            val index = colonLookAhead(limit, false)
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

    fun colonLookAhead(limit: Int = input.length, skipCommentLines: Boolean = true): Int {
        var temp = index
        // Skip whitespaces
        while (temp < limit && input[temp] == ' ') {
            temp++
        }
        var escaped = false
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


        parse root
            given a start point
            peek first char after spaces
            if first char is '-'
                parse list
            if first char is #
                parse comment
                parse root
            else
                parse map

        parse list
            create list
            while true
                given a start point
                peek first char after spaces
                if first char is #
                    parse comment
                else
                    count number of spaces
                    calculate indent
                    if indent equals previous indent
                        parse value
                        add parsed value
                    if indent greater than previous
                        error - expected block end but found start
                    else
                        break
                save previous indent

        parse comment
            consume until end of line
            consume \n

        parse value
            given a start point
            peek first char after spaces
            if first char is '-'
                consume '-'
                consume excess space
                return parse type
            else if first char is '#'
                parse comment
                return null
            else if peek value has :
                return parse map
            else
                return parse type

        parse type (with limit)
            if char is false
                return false
            else if char is true
                return true
            else if char first is [
                return parse line list
            else if char first is {
                return parse line map
            else if char first is "
                return parse quoted string
            else
                while index isn't ':' or \n or greater than limit
                    increase index
                if char is \n
                    consume \n
                return string

        parse map
            given a start point and indent
            create map
            while true
                count current indent
                if current indent equals indent
                    parse key-value pair
                    set map key-value pair
                else if indent greater than previous
                    parse value
                    set map parsed value
                else
                    error - bad format expected block end
            return map

        parse key-value pair
            given previous indent
            parse key
            if next char is \n
                consume \n
                peek indent
                peek char after spaces
                if indent equals previous indent
                    return key and null as map
                if indent greater than previous
                    parse value
                    return parsed value
            else
                parse type
                return key and parsed type as map

        parse key
            skip excess spaces
            mark start point
            while value isn't ':'
                increase index
            key is string from start point to index
            consume ':'
            consume any excess space
            return key

        parse line list
            create list
            var for open and closed []'s
            consume excess spaces
            consume [
            consume excess spaces
            temp index
            while count is zero and char isn't ] and less than limit
                if char is [
                    increase count
                if char is ]
                    decrease count
                if char is ,
                    parse type with limit of temp index
                    add parse type
                increase temp index
                set last index to temp index
            consume ]
            consume excess spaces
            return list

        parse line map
            create map
            var for open and closed {}'s
            consume excess spaces
            consume {
            consume excess spaces
            temp index
            while count is zero and char isn't } and less than limit
                if char is }
                    increase count
                if char is }
                    decrease count
                if char is ,
                    parse key
                    parse type with limit of temp index
                    set key to parse type
                increase temp index
                set last index to temp index
            consume }
            consume excess spaces
            return map


        "- apple\n- banana\n- orange"
        "# list of fruits\n- apple\n     # my favourite\n- banana \n- orange # not the colour"
        "- \"apple\"\n- \"banana\"\n- \"orange\"\n  - \"pear\""

     */
}
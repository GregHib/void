package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

class FinalYamlParser : CharArrayReader() {


    /*

        ALWAYS

        list + flat list
        - <value>
        - <value>


        # indent < current

        list + lower indent list
          - <value>
        - <value>

        list + lower indent map
          - <value>
        <key>: <value>


        ## key + list item
        key + indented list
        <key>:
          - <value>

        key + flat list
        <key>:
        - <value>

        { key: null, key: value }
        <key>: <null>
        <key>: <value>

        { key: { key: value } }
        <key>: <null>
          <key>: <value>

        NEVER

        list + indented list
        - <value>
          - <value>

        list + flat map
        - <value>
        <key>: <value>


        # Key + value + list
        root key + value + flat list
        <key>: <value>
        - <value>

        root key + value + indented list
        <key>: <value>
          - <value>


        SOMETIMES


        INVALID
        list + non-key-value + indented list
        - <value>
          <key>: <value>

        VALID

        list + key-value + indented list
        - <key>: <value>
          <key>: <value>

        Invalid

        List

        - <value>
        - <value>
          - <value>

        - <value>
        - <value>
        <key>: <value>

        - <value>
        - <value>
          <key>: <value>

        <key>: <value>
        - <value>

        <key>: <value>
          - <value>

        Map


        Valid

        Map of lists
        <key>:
          - <value>
          - <value>
        <key>:
          <key>: <value>
          <key>: <value>

        <key>:
        - <value>
        - <value>
        <key>: <value>

        <key>:
          - <value>
          - <value>
        <key>: <value>

        <key>: // null
        <key>: <value>
        <key>: // null
        <key>: <value>

        List of maps

        - <key>: <value>
        - <key>: <value>

        - <key>: <value>
          <key>: <value>
          <key>:
            - <key>: <value>
              <key>: <value>

        <key>:
          - <key>: <value>
          - <key>: <value>
          - <key>: <value>
     */

    /*

        <intent><operator?><value><spaces, comments, linebreaks>
        <value>
        [ lists, <value> ]
        { key: Maps, <value> }
        "quotes"
        &anchors
        <key>\\s+:\\s+<value>
        - \\s+item
     */
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
            '&' -> parseAnchorString(0, limit)
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

    private fun isLineEnd() = linebreak(input[index]) || input[index] == '#'

    private fun isListItem(limit: Int) = input[index] == '-' && index + 1 < limit && input[index + 1] == ' '

    private fun reachedEnd(limit: Int): Boolean {
        skipSpaces(limit)
        if (index == limit) {
            return true
        }
        if (linebreak(input[index])) {
            return true
        }
        return input[index] == '#'
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
            if (linebreak(char) || char == '#') {
                break
            } else if (char == ' ' && previous != ' ') {
                end = index
            } else if (char == ':' && (index + 1 == limit || (index + 1 < limit && (input[index + 1] == ' ' || linebreak(input[index + 1]) || input[index + 1] == '#')))) {
                return substring(start, if (previous != ' ') index else end) // Return the key
            }
            previous = char
            index++
        }
        return substring(start, if (previous != ' ') index else end) // Return the value
    }

    private fun number(start: Int, limit: Int): Any? {
        index++ // skip first
        var decimal = false
        while (index < limit) {
            when (input[index]) {
                '\n', '\r', '#' -> return number(decimal, start, index)
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
        if (linebreak(input[index])) {
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

    private fun number(decimal: Boolean, start: Int, end: Int): Any {
        val string = substring(start, end)
        return if (decimal) {
            string.toDouble()
        } else {
            val long = string.toLong()
            if (long <= Int.MAX_VALUE) long.toInt() else long
        }
    }

    fun parseScalar(limit: Int = size): Any {
        if (index == limit) {
            return ""
        }
        val start = index
        val char = input[index]
        if (linebreak(char)) {
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
        val end = skipValueIndex(limit)
        skipIfComment(limit)
        skipLineBreaks(limit)
        return substring(start, end)
    }

    fun parseAnchorString(currentIndent: Int, limit: Int = size): Any {
        val start = index
        skipExceptLineBreaks(limit)
        var end = index
        skipLineBreaks(limit)
        var count = 0
        while (count++ < MAP_MAXIMUM && index < limit) {
            val indent = peekIndent(limit)
            if (indent != currentIndent) {
                return substring(start, end)
            } else {
                skipExceptLineBreaks(limit)
                end = index
                skipLineBreaks(limit)
            }
        }
        return substring(start, end)
    }

    fun parseExplicitList(limit: Int = size): List<Any> {
        val list = ObjectArrayList<Any>(EXPECTED_EXPLICIT_LIST_SIZE)
        return parseExplicit(list, limit, '[', ']', ::addListItem)
    }

    private inline fun <T> parseExplicit(list: T, limit: Int = size, open: Char, close: Char, add: (T, Int, Int) -> Unit): T {
        index++ // skip opening char
        skipWhitespace(limit)
        var depth = 1
        var count = 0
        // For n number of items
        while (count++ < LIST_MAXIMUM && index < limit) {
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
                } else if (input[temp] == open) { // Enter into a nested list
                    skipWhitespace(limit)
                    depth++
                } else if (input[temp] == close) { // Exist out of a nested list
                    if (--depth == 0) {
                        // Found the end of the list
                        add(list, limit, temp)
                        if (index < limit && input[index] == close) {
                            index++ // skip closing char
                        }
                        return list
                    }
                }
                temp++
            }
            if (nextComma == -1) {
                // Add what's remaining if end of list found
                if (temp < limit && input[temp] == close) {
                    index = temp + 1 // skip closing char
                    return list
                } else {
                    throw IllegalStateException("Unable to find comma or end of list.")
                }
            } else {
                add(list, limit, nextComma)
            }
        }
        return list
    }

    private fun addListItem(list: MutableList<Any>, limit: Int, nextComma: Int) {
        val parsed = parseValue(0, nextComma)
        list.add(listModifier(parsed))
        skipWhitespace(limit)
        if (index < limit && input[index] == ',') {
            index++ // skip ','
            skipWhitespace(limit)
        }
    }

    fun parseList(currentIndent: Int, limit: Int = size, nestedMap: Boolean = false): List<Any> {
        val list = ObjectArrayList<Any>(EXPECTED_LIST_SIZE)
        var count = 0
        while (count++ < LIST_MAXIMUM && index < limit) {
            val indent = peekIndent(limit)
            val spaceIndex = indentIndex(indent)
            val peek = input[spaceIndex]
            val second = input.getOrNull(spaceIndex + 1)
            if (peek == '#') {
                index = spaceIndex
                skipComment(limit)
                skipLineBreaks(limit)
            } else if (indent < currentIndent) {
                return list
            } else if (peek != '-' || second != ' ' || indent > currentIndent) {
                if (indent == currentIndent && nestedMap) {
                    return list
                }
                throw IllegalArgumentException("Expected list item at index index=${indentIndex(indent)} indent=$indent current=$currentIndent")
            } else {
                index = spaceIndex + 1 // skip '-'
                skipSpaces(limit)
                val parsed = parseValue(currentIndent + 1, limit)
                list.add(listModifier(parsed))
                skipLineBreaks()
            }
        }
        return list
    }

    fun parseMap(currentIndent: Int, limit: Int = size): Map<String, Any> {
        val map = Object2ObjectOpenHashMap<String, Any>(EXPECTED_MAP_SIZE)
        var count = 0
        while (count++ < MAP_MAXIMUM && index < limit) {
            var indent = peekIndent(limit)
            var spaceIndex = indentIndex(indent)
            var peek = input[spaceIndex]
            if (peek == '#') {
                index = spaceIndex
                skipComment(limit)
                skipLineBreaks(limit)
                continue
            }
            if (map.isNotEmpty() && indent < currentIndent) {
                return map
            }
            index = spaceIndex
            val key = if (index < size && input[index] == '"') {
                parseQuotedString()
            } else {
                val start = index
                val end = skipKeyIndex(limit)
                    ?: throw IllegalArgumentException("Expected ':' at index $index")
                substring(start, end)// this doesn't need to check multi-lines
            }
            skipSpaces(limit)
            index++ // skip ':'
            skipSpaces(limit)
            skipIfComment(limit, false)
            if (index < limit && linebreak(input[index])) { // end of line
                index++ // skip line break
                if (index < limit && input[index] == '\n') {
                    index++ // skip windows line breaks
                }
                // if next line is a key-value pair
                if (!peekHasKeyValuePair(limit)) {
                    // support both flat and indented lists after open-ended map pairs
                    val indentation = if (peekIndent(limit) > currentIndent) currentIndent + 1 else currentIndent
                    map[key] = mapModifier(key, parseValue(indentation, limit, true))
                    continue
                }
            } else if (index != limit) {
                map[key] = mapModifier(key, parseValue(currentIndent, limit))
                continue
            }
            indent = peekIndent(limit)
            spaceIndex = indentIndex(indent)
            peek = input[spaceIndex]
            if (peek == '#') {
                index = spaceIndex
                skipComment(limit)
                skipLineBreaks(limit)
            }
            // If key-value pair on same level then this value is null
            spaceIndex = indentIndex(peekIndent(limit))
            peek = input[spaceIndex]
            if (indent == currentIndent) {
                map[key] = ""
            } else if (peek == '#') {
                index = spaceIndex
                skipComment(limit)
                skipLineBreaks(limit)
            } else if (indent >= currentIndent) {
                map[key] = mapModifier(key, parseValue(currentIndent + 1, limit))
            }
            // end of file
        }
        return map
    }

    fun parseValue(currentIndent: Int, limit: Int = size, nestedMap: Boolean = false): Any {
        val indent = peekIndent(limit)
        val index = indentIndex(indent)
        return when (input[index]) {
            '#' -> {
                this.index = index
                skipComment(limit)
                skipLineBreaks(limit)
                parseValue(currentIndent, limit)
            }
            '[' -> {
                this.index = index
                parseExplicitList(limit)
            }
            '{' -> {
                this.index = index
                parseExplicitMap(limit)
            }
            '&' -> {
                this.index = index
                parseAnchorString(currentIndent, limit)
            }
            else -> if (input[index] == '-' && index + 1 < limit && input[index + 1] == ' ') {
                parseList(currentIndent, limit, nestedMap)
            } else if (isKeyValuePair(limit)) {
                parseMap(currentIndent, limit)
            } else if (input[index] == '"') {
                this.index = index
                val string = parseQuotedString(limit)
                skipSpaces(limit)
                skipLineBreaks(limit)
                string
            } else {
                this.index = index
                parseScalar(limit)
            }
        }
    }

    /**
     * Checks if there's a valid key-value pair on the current line
     * Simplified version of [skipKeyIndex]
     */
    fun peekHasKeyValuePair(limit: Int = size): Boolean {
        var temp = index
        // Skip whitespaces
        while (temp < limit && input[temp] == ' ') {
            temp++
        }
        // Find the first colon followed by a space or end line, unless reached a terminator symbol
        while (temp < limit) {
            when (input[temp]) {
                '\r', '\n' -> return false
                ':' -> {
                    if (temp + 1 == limit) {
                        return true
                    }
                    if (temp + 1 > limit) {
                        return false
                    }
                    if (input[temp + 1].isWhitespace() || input[temp + 1] == '#') {
                        return true
                    }
                }
            }
            temp++
        }
        return true
    }

    private fun peekQuote(temp: Int, limit: Int): Int? {
        var index = temp + 1 // skip opening quote
        while (index < limit) {
            val char = input[index]
            if (char == '"') {
                return index + 1
            }
            if (linebreak(char)) {
                return null
            }
            index++
        }
        return null
    }

    fun isKeyValuePair(limit: Int = size): Boolean {
        var temp = index
        when (input[temp]) {
            '-', '[', '{', '\r', '\n', '#' -> return false
            '"' -> temp = peekQuote(temp, limit) ?: return false
        }
        // Find the first colon followed by a space or end line, unless reached a terminator symbol
        while (temp < limit) {
            when (input[temp]) {
                ',', '\r', '\n', '#' -> return false
                ':' -> if (temp + 1 == limit || input[temp + 1] == ' ' || linebreak(input[temp + 1]) || input[temp + 1] == '#') {
                    return true
                }
                '\\' -> temp++
            }
            temp++
        }
        return false
    }

    /**
     * Finds the end index of the next valid key or null
     * Unlike [peekHasKeyValuePair] this method ignores line comments and quotes
     * Assumes no spaces prefixed after [start]
     */
    fun skipKeyIndex(limit: Int = size): Int? {
        var end = -1
        if (index == limit) {
            return null
        }
        when (input[index]) {
            '-', '[', '{', '\r', '\n', '#' -> return null
            '"' -> index = peekQuote(index, limit) ?: return null
        }
        // Find the first colon followed by a space or end line, unless reached a terminator symbol
        var previous = if (index <= 1) ' ' else input[index - 1]
        while (index < limit) {
            when (input[index]) {
                ',', '\r', '\n', '#' -> return null
                ' ' -> if (previous != ' ') end = index // Mark end of key
                ':' -> if (index + 1 == limit || input[index + 1] == ' ' || linebreak(input[index + 1]) || input[index + 1] == '#') {
                    if (previous == ' ' && end != -1) {
                        return end
                    }
                    return index
                }
                '\\' -> index++
            }
            previous = input[index]
            index++
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

    private fun indentIndex(indent: Int) = index + (indent * 2)

    fun parseExplicitMap(limit: Int = size): Map<String, Any> {
        val map = Object2ObjectOpenHashMap<String, Any>(EXPECTED_EXPLICIT_MAP_SIZE)
        return parseExplicit(map, limit, '{', '}', ::addMapEntry)
    }

    private fun addMapEntry(map: MutableMap<String, Any>, limit: Int, nextComma: Int) {
        if (index < size && input[index] == '"') {
            index++ // skip opening quote
            val start = index
            while (index < limit) {
                when (input[index]) {
                    '\\' -> index++ // escaped
                    '"' -> {
                        val key = substring(start, index)
                        index++ // skip closing quote
                        skipWhitespaceCommentColon(limit)
                        val parsed = parseMapValue(nextComma)
                        map[key] = mapModifier(key, parsed)
                        skipWhitespace()
                        if (index < limit && input[index] == ',') {
                            index++ // skip ','
                            skipWhitespace(limit)
                        }
                    }
                }
                index++
            }
            throw IllegalArgumentException("Expected closing quote at index $index")
        } else {
            val start = index
            var end = -1
            if (index == limit) {
                throw IllegalArgumentException("Expected ':' at index $index")
            }
            when (input[index]) {
                '-', '[', '{', '\r', '\n', '#' -> throw IllegalArgumentException("Expected ':' at index $index")
                '"' -> index = peekQuote(index, limit) ?: throw IllegalArgumentException("Expected ':' at index $index")
            }
            // Find the first colon followed by a space or end line, unless reached a terminator symbol
            var previous = if (index <= 1) ' ' else input[index - 1]
            while (index < limit) {
                when (input[index]) {
                    ',', '\r', '\n', '#' -> throw IllegalArgumentException("Expected ':' at index $index")
                    ' ' -> if (previous != ' ') end = index // Mark end of key
                    ':' -> {
                        if (index + 1 == limit) {
                            map[substring(start, index)] = ""
                            return
                        }
                        val key = substring(start, if (previous == ' ' && end != -1) end else index)
                        index++ // skip ':'
                        // Skip spaces, lines and comments until the next value
                        skipWhitespaceComments(limit)
                        val parsed = parseMapValue(nextComma)
                        map[key] = mapModifier(key, parsed)
                        skipWhitespace()
                        if (index < limit && input[index] == ',') {
                            index++ // skip ','
                            skipWhitespace(limit)
                        }
                        return
                    }
                    '\\' -> index++
                }
                previous = input[index]
                index++
            }
            throw IllegalArgumentException("Expected ':' at index $index")
        }
    }

    /**
     * Expect no spaces
     */
    private fun parseMapValue(limit: Int): Any {
        if (index == size) {
            throw IllegalStateException("Unexpected end of file")
        }
        return when (input[index]) {
            '[' -> parseExplicitList(limit)
            '{' -> parseExplicitMap(limit)
            '"' -> parseQuotedString(limit)
            '&' -> parseAnchorString(0, limit)
            else -> parseScalar(limit)
        }
    }

    companion object {
        private const val EXPECTED_LIST_SIZE = 2
        private const val EXPECTED_EXPLICIT_LIST_SIZE = 2
        private const val EXPECTED_MAP_SIZE = 8
        private const val EXPECTED_EXPLICIT_MAP_SIZE = 5
        private const val LIST_MAXIMUM = 1_000_000
        private const val MAP_MAXIMUM = 1_000_000
    }
}
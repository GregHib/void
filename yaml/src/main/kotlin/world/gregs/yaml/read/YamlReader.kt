package world.gregs.yaml.read

import world.gregs.yaml.CharReader

/**
 * Reads keys and values as their original types.
 * Supported types:
 * - String (with or without double quotes)
 * - Boolean (lower case only)
 * - Double
 * - Int
 * - Long
 * - Map (explicit or normal)
 * - List (explicit or normal)
 */
abstract class YamlReader(val reader: CharReader, var config: YamlReaderConfiguration) {

    abstract fun explicitList(withinMap: String?): Any

    abstract fun explicitMap(): Any

    fun value(indentOffset: Int, withinMap: String?): Any = when (reader.char) {
        '[' -> explicitList(withinMap)
        '{' -> explicitMap()
        '&' -> anchor()
        '*' -> inlineAnchor(withinMap)
        else -> collection(indentOffset, withinMap)
    }

    @Suppress("UNCHECKED_CAST")
    private fun anchor(): Any {
        val alias = alias()
        val value = value(indentOffset = 0, withinMap = null)
        reader.anchors[alias] = value
        if (!config.ignoreAnchors) {
            return value
        }
        return when (value) {
            is Map<*, *> -> (value as MutableMap<String, Any>).apply { put("&", alias) }
            is List<*> -> (value as MutableList<Any>).apply { add(0, "&$alias") }
            else -> "&$alias $value"
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun inlineAnchor(withinMap: String?): Any {
        val alias = alias()
        val anchor = reader.anchors[alias] ?: throw IllegalArgumentException("Unable to find anchor for alias '$alias'")
        return if (config.ignoreAnchors) {
            if (reader.outBounds || withinMap == "<<" || anchor !is List<*>) {
                return "*$alias"
            }
            reader.nextLine()
            when (val value = value(indentOffset = 0, withinMap = null)) {
                is Map<*, *> -> (value as MutableMap<String, Any>).apply { put("<<", "*$alias") }
                is List<*> -> (value as MutableList<Any>).apply { add(0, "*$alias") }
                else -> "*$alias $value"
            }
        } else {
            if (reader.outBounds) {
                return anchor
            }
            return config.anchor(anchor)
        }
    }

    private fun alias(): String {
        reader.skip() // skip anchor or alias marker
        val start = reader.index
        while (reader.inBounds) {
            val char = reader.char
            if (char == ' ' || char == '\r' || char == '\n' || char == ',') {
                val alias = reader.substring(start, reader.index)
                reader.nextLine()
                return alias
            }
            reader.skip()
        }
        return reader.substring(start, reader.index) // end of line
    }

    abstract fun collection(indentOffset: Int, withinMap: String?): Any

    fun type(): Any {
        if (reader.char == '"') {
            return quote()
        }
        val start = reader.index
        return when (reader.char) {
            't' -> if (isTrue()) true else string(start)
            'f' -> if (isFalse()) false else string(start)
            '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' ->
                return number(start) ?: string(start)
            else -> string(start)
        }
    }

    private fun quote(): String {
        reader.skip() // skip opening quote
        val start = reader.index
        var prev = ' '
        while (reader.inBounds) {
            if (reader.char == '"' && prev != '\\') {
                reader.skip() // skip closing quote
                break
            }
            prev = reader.char
            reader.skip()
        }
        val quoted = reader.substring(start, reader.index - 1)
        if (reader.inBounds && reader.char == ' ') {
            reader.skipSpaces()
        }
        return quoted
    }

    private fun string(start: Int): String {
        var char: Char
        var end = -1
        var previous = ' '
        while (reader.inBounds) {
            char = reader.char
            when (char) {
                ':' -> if ((reader.index + 1 >= reader.size || reader.peekNext == ' ' || isOpeningTerminator(reader.peekNext))) {
                    return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the key
                }
                ' ' -> if (previous != ' ') end = reader.index
                else -> if (isClosingTerminator(char)) break
            }
            previous = char
            reader.skip()
        }
        return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the value
    }

    private fun isFalse(): Boolean = reader.inBounds(4) && reader.next() == 'a' && reader.next() == 'l' && reader.next() == 's' && reader.next() == 'e' && end()

    private fun isTrue(): Boolean = reader.inBounds(3) && reader.next() == 'r' && reader.next() == 'u' && reader.next() == 'e' && end()

    private fun end(): Boolean {
        reader.skip()
        if (reader.outBounds) {
            return true
        }
        if (reader.char == ' ') {
            reader.skipSpaces()
            if (reader.outBounds) {
                return true
            }
        }
        return (reader.char == ':' && reader.nextCharEmpty()) || isClosingTerminator(reader.char)
    }

    open fun isClosingTerminator(char: Char) = char == '\r' || char == '\n' || char == '#'

    open fun isOpeningTerminator(char: Char) = char == '\r' || char == '\n' || char == '#'

    fun number(start: Int): Any? {
        var decimal = reader.char == '.'
        reader.skip() // skip first
        while (reader.inBounds) {
            when (reader.char) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                }
                '.' -> if (!decimal) decimal = true else return null
                ' ' -> {
                    val end = reader.index
                    reader.skipSpaces()
                    return if (reader.outBounds || isClosingTerminator(reader.char)) reader.number(decimal, start, end) else null
                }
                ':' -> return if (reader.nextCharEmpty()) reader.number(decimal, start, reader.index) else null
                else -> return if (isClosingTerminator(reader.char)) reader.number(decimal, start, reader.index) else null
            }
            reader.skip()
        }
        return reader.number(decimal, start, reader.index) // End of file
    }
}

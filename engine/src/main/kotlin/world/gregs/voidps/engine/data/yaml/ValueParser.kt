package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.CharArrayReader

abstract class ValueParser(val reader: CharArrayReader) {

    abstract fun parseExplicitList(): Any

    abstract fun parseExplicitMap(): Any

    fun parseValue(indentOffset: Int, withinMap: Boolean): Any {
        return when (reader.char) {
            '[' -> parseExplicitList()
            '{' -> parseExplicitMap()
            '&' -> {
                reader.skipAnchorString()
                reader.nextLine()
                parseValue(0, false)
            }
            else -> parseCollection(indentOffset, withinMap)
        }
    }

    abstract fun parseCollection(indentOffset: Int, withinMap: Boolean): Any

    fun parseType(): Any {
        if (reader.char == '"') {
            return parseQuote()
        }
        val start = reader.index
        return when (reader.char) {
            't' -> if (isTrue()) true else readString(start)
            'f' -> if (isFalse()) false else readString(start)
            '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                return number(start) ?: readString(start)
            else -> readString(start)
        }
    }

    private fun parseQuote(): String {
        reader.skip() // skip opening quote
        val start = reader.index
        while (reader.inBounds) {
            if (reader.char == '"') {
                reader.skip() // skip closing quote
                break
            }
            reader.skip()
        }
        val quoted = reader.substring(start, reader.index - 1)
        if (reader.inBounds && reader.char == ' ') {
            reader.skipSpaces()
        }
        return quoted
    }

    private fun readString(start: Int): String {
        var char: Char
        var end = -1
        var previous = ' '
        while (reader.inBounds) {
            char = reader.char
            if (isClosingTerminator(char)) {
                break
            } else if (char == ' ' && previous != ' ') {
                end = reader.index
            } else if (char == ':' && (reader.index + 1 == reader.size || (reader.inBounds(1) && (reader.next == ' ' || isOpeningTerminator(reader.next))))) {
                return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the key
            }
            previous = char
            reader.skip()
        }
        return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the value
    }

    private fun isFalse(): Boolean {
        return reader.inBounds(4) && reader.next() == 'a' && reader.next() == 'l' && reader.next() == 's' && reader.next() == 'e' && atEnd()
    }

    private fun isTrue(): Boolean {
        return reader.inBounds(3) && reader.next() == 'r' && reader.next() == 'u' && reader.next() == 'e' && atEnd()
    }

    private fun atEnd(): Boolean {
        reader.skip()
        if (reader.end) {
            return true
        }
        if (reader.char == ' ') {
            reader.skipSpaces()
            if (reader.end) {
                return true
            }
        }
        return (reader.char == ':' && reader.nextCharEmpty()) || isClosingTerminator(reader.char)
    }

    open fun isClosingTerminator(char: Char) = char == '\r' || char == '\n' || char == '#'

    open fun isOpeningTerminator(char: Char) = char == '\r' || char == '\n' || char == '#'

    private fun number(start: Int): Any? {
        reader.skip() // skip first
        var decimal = false
        while (reader.inBounds) {
            when (reader.char) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                }
                '.' -> if (!decimal) decimal = true else return null
                ' ' -> {
                    val end = reader.index
                    reader.skipSpaces()
                    return if (reader.end || isClosingTerminator(reader.char)) reader.number(decimal, start, end) else null
                }
                ':' -> return if (reader.nextCharEmpty()) reader.number(decimal, start, reader.index) else null
                else -> return if (isClosingTerminator(reader.char)) reader.number(decimal, start, reader.index) else null
            }
            reader.skip()
        }
        return reader.number(decimal, start, reader.index) // End of file
    }
}
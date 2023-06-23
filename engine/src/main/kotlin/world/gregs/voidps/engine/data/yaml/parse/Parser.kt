package world.gregs.voidps.engine.data.yaml.parse

import world.gregs.voidps.engine.data.yaml.CharReader

abstract class Parser(val reader: CharReader) {

    abstract fun explicitList(): Any

    abstract fun explicitMap(): Any

    fun value(indentOffset: Int, withinMap: Boolean): Any {
        return when (reader.char) {
            '[' -> explicitList()
            '{' -> explicitMap()
            '&' -> {
                reader.skipAnchorString()
                reader.nextLine()
                value(0, false)
            }
            else -> collection(indentOffset, withinMap)
        }
    }

    abstract fun collection(indentOffset: Int, withinMap: Boolean): Any

    fun type(): Any {
        if (reader.char == '"') {
            return quote()
        }
        val start = reader.index
        return when (reader.char) {
            't' -> if (isTrue()) true else string(start)
            'f' -> if (isFalse()) false else string(start)
            '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                return number(start) ?: string(start)
            else -> string(start)
        }
    }

    private fun quote(): String {
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

    private fun string(start: Int): String {
        var char: Char
        var end = -1
        var previous = ' '
        while (reader.inBounds) {
            char = reader.char
            when (char) {
                ':' -> if ((reader.index + 1 >= reader.size || reader.peekNext == ' ' || isOpeningTerminator(reader.peekNext)))
                    return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the key
                ' ' -> if (previous != ' ') end = reader.index
                else -> if (isClosingTerminator(char)) break
            }
            previous = char
            reader.skip()
        }
        return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the value
    }

    private fun isFalse(): Boolean {
        return reader.inBounds(4) && reader.next() == 'a' && reader.next() == 'l' && reader.next() == 's' && reader.next() == 'e' && end()
    }

    private fun isTrue(): Boolean {
        return reader.inBounds(3) && reader.next() == 'r' && reader.next() == 'u' && reader.next() == 'e' && end()
    }

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
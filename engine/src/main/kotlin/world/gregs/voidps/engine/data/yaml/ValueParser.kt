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
        if (reader.outBounds) {
            return ""
        } else if (reader.char == '"') {
            val quoted = reader.parseQuote()
            if (reader.inBounds && reader.char == ' ') {
                reader.skipSpaces()
            }
            return quoted
        }
        val start = reader.index
        var char = reader.char
        if (isTrue(char)) {
            return true
        } else if (isFalse(char)) {
            return false
        } else if (char == '-' || char == '0' || char == '1' || char == '2' || char == '3' || char == '4' || char == '5' || char == '6' || char == '7' || char == '8' || char == '9') {
            val number = number(start)
            if (number != null) {
                return number
            }
        }
        var end = -1
        var previous = ' '
        while (reader.inBounds) {
            char = reader.char
            if (isClosingTerminator(char)) {
                break
            } else if (char == ' ' && previous != ' ') {
                end = reader.index
            } else if (char == ':' && (reader.index + 1 == reader.size || (reader.index + 1 < reader.size && (reader.next == ' ' || isOpeningTerminator(reader.next))))) {
                return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the key
            }
            previous = char
            reader.skip()
        }
        return reader.substring(start, if (previous != ' ' || end == -1) reader.index else end) // Return the value
    }

    fun isFalse(char: Char): Boolean {
        return char == 'f' && reader.inBounds(4) && reader.next() == 'a' && reader.next() == 'l' && reader.next() == 's' && reader.next() == 'e' && atEnd()
    }


    fun isTrue(char: Char): Boolean {
        return char == 't' && reader.inBounds(3) && reader.next() == 'r' && reader.next() == 'u' && reader.next() == 'e' && atEnd()
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

    fun number(start: Int): Any? {
        reader.skip() // skip first
        var decimal = false
        while (reader.inBounds) {
            if (isClosingTerminator(reader.char)) {
                return reader.number(decimal, start, reader.index)
            }
            when (reader.char) {
                ' ' -> {
                    val end = reader.index
                    reader.skipSpaces()
                    return if (reader.end || isClosingTerminator(reader.char)) {
                        reader.number(decimal, start, end)
                    } else {
                        null
                    }
                }
                '.' -> if (!decimal) decimal = true else return null
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                }
                ':' -> return if (reader.nextCharEmpty()) {
                    reader.number(decimal, start, reader.index)
                } else {
                    null
                }
                else -> return null
            }
            reader.skip()
        }
        return reader.number(decimal, start, reader.index) // End of file
    }
}
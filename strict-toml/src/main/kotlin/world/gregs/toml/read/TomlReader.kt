@file:Suppress("UNCHECKED_CAST")

package world.gregs.toml.read

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList

class TomlReader(private val reader: CharReader) {

    private fun list() = ObjectArrayList<Any>(2)
    private fun map() = Object2ObjectOpenHashMap<String, Any>(8, .25f)

    fun read(root: MutableMap<String, Any>): Map<String, Any> {
        var map = root
        var previous = map
        reader.nextLine()
        while (reader.inBounds) {
            when (reader.char) {
                '[' -> {
                    val inherit = (reader.inBounds(1) && reader.peek(1) == '.') || (reader.inBounds(2) && reader.peek(1) == '[' && reader.peek(2) == '.')
                    if (inherit) {
                        map = title(previous)
                    } else {
                        map = title(root)
                        previous = map
                    }
                }
                '=' -> throw IllegalArgumentException("Expected variable or table at start of line.")
                else -> {
                    variable(map)
                    if (reader.inBounds) {
                        reader.expectLineBreak()
                    }
                }
            }
            reader.nextLine()
        }
        return root
    }

    internal fun title(map: MutableMap<String, Any>): MutableMap<String, Any> {
        reader.expect('[')
        if (reader.char == '[') {
            return arrayOfTables(map)
        }
        reader.skipSpaces()
        val childMap = tableTitle(map)
        reader.expect(']')
        return childMap
    }

    private fun tableTitle(map: MutableMap<String, Any>): MutableMap<String, Any> {
        if (reader.inBounds && reader.char == '.') {
            reader.skip(1)
        }
        val label = label()
        reader.skipSpaces()
        var child: MutableMap<String, Any> = when (val entry = map[label]) {
            null -> {
                // Set new element
                val child: MutableMap<String, Any> = map()
                map[label] = child
                child
            }
            // Get last element
            is List<*> -> (entry as MutableList<*>).last() as MutableMap<String, Any>
            is Map<*, *> -> entry as MutableMap<String, Any>
            else -> throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
        }
        if (reader.inBounds) {
            when (reader.char) {
                '.' -> {
                    reader.expect('.')
                    reader.skipSpaces()
                    child = tableTitle(child)
                }
                '"', '\'' -> child = tableTitle(child)
            }
        }
        return child
    }

    private fun arrayOfTables(map: MutableMap<String, Any>): MutableMap<String, Any> {
        reader.expect('[')
        val childMap = arrayOfTablesTitle(map)
        reader.expect(']')
        reader.expect(']')
        return childMap
    }

    private fun arrayOfTablesTitle(map: MutableMap<String, Any>): MutableMap<String, Any> {
        if (reader.inBounds && reader.char == '.') {
            reader.skip(1)
        }
        val label = label()
        reader.skipSpaces()
        // Nest inside a list if at end of title
        if (reader.char == ']') {
            val child = when (val current = map[label]) {
                null -> {
                    // Set new list and element
                    val list = list()
                    map[label] = list
                    val element: MutableMap<String, Any> = map()
                    list.add(element)
                    element
                }
                is List<*> -> {
                    // Append new element
                    val list = (current as MutableList<Any>)
                    val element: MutableMap<String, Any> = map()
                    list.add(element)
                    element
                }
                else -> throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
            }
            return child
        }
        var child = when (val current = map[label]) {
            null -> {
                // Set new element
                val element = map()
                map[label] = element
                element
            }
            is List<*> -> {
                // Get current last list element
                val last = (current as List<Any>).last()
                if (last !is Map<*, *>) {
                    throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
                }
                last as MutableMap<String, Any>
            }
            else -> throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
        }
        if (reader.inBounds) {
            when (reader.char) {
                '.' -> {
                    reader.expect('.')
                    reader.skipSpaces()
                    child = arrayOfTablesTitle(child)
                }
                '"', '\'' -> child = arrayOfTablesTitle(child)
            }
        }
        return child
    }

    fun variable(map: MutableMap<String, Any>) {
        val label = label()
        reader.skipSpaces()
        when (reader.char) {
            '.' -> {
                reader.expect('.')
                reader.skipSpaces()
            }
            else -> {
                if (map.containsKey(label)) {
                    throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
                }
                reader.expect('=')
                reader.skipSpaces()
                val value = value()
                map[label] = value
                reader.skipSpacesComment()
                return
            }
        }
        val child = if (map.containsKey(label)) {
            val value = map.getValue(label)
            if (value !is MutableMap<*, *>) {
                throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
            }
            value as MutableMap<String, Any>
        } else {
            val child = map()
            map[label] = child
            child
        }
        variable(child)
    }

    fun label(): String = when (reader.char) {
        '"', '\'' -> {
            // Read between quotes
            val quote = reader.char
            reader.expect(quote)
            val start = reader.index
            while (reader.inBounds) {
                when (reader.char) {
                    '\r', '\n' -> throw IllegalArgumentException("Unterminated string at ${reader.exception}")
                    quote -> break
                    else -> reader.skip(1)
                }
            }
            val label = reader.substring(start)
            reader.expect(quote)
            label
        }
        else -> {
            val start = reader.index
            var end = -1
            // Read until reached a new level of nesting or end of table title
            while (reader.inBounds) {
                when (reader.char) {
                    '\r', '\n' -> throw IllegalArgumentException("Unterminated string at ${reader.exception}")
                    '"', '\'', '.', ']', '=' -> {
                        end = reader.index
                        break
                    }
                    ' ', '\t' -> {
                        end = reader.index
                        reader.skipSpaces()
                        when (reader.char) {
                            '"', '\'', '.', ']', '=' -> break
                        }
                        break
                    }
                }
                reader.skip(1)
            }
            val label = reader.substring(start, end)
            label
        }
    }

    fun value(): Any = when (reader.char) {
        '[' -> inlineArray()
        '{' -> inlineTable()
        '\'' -> stringLiteral()
        '"' -> basicString()
        't' -> booleanTrue()
        'f' -> booleanFalse()
        '#' -> throw IllegalArgumentException("Invalid comment")
        '0' -> when (reader.peek) {
            'x' -> hex()
            'o' -> octal()
            'b' -> binary()
            else -> number()
        }
        '-' -> {
            reader.skip(1)
            number(negative = true)
        }
        '+' -> {
            reader.skip(1)
            number()
        }
        '1', '2', '3', '4', '5', '6', '7', '8', '9' -> number()
        else -> throw IllegalArgumentException("Unexpected character, expecting string, number, boolean, inline array or inline table at ${reader.exception}")
    }

    fun basicString(): String {
        reader.skip(1)
        if (reader.char == '"') {
            if (reader.peek != '"') {
                throw IllegalArgumentException("Expected character '\"' at ${reader.exception}")
            }
            reader.skip(2)
            return multiLineString()
        }
        val start = reader.index
        while (reader.inBounds) {
            when (reader.char) {
                '\r', '\n' -> throw IllegalArgumentException("Unterminated string at ${reader.exception}")
                '"' -> if (reader.peek(-1) != '\\') break
            }
            reader.skip(1)
        }
        val value = reader.substring(start)
        reader.expect('"')
        return value
    }

    fun multiLineString(): String {
        if (reader.inBounds && (reader.char == '\r' || reader.char == '\n')) {
            reader.markLine()
        }
        val builder = StringBuilder()
        while (reader.inBounds) {
            when (reader.char) {
                '\\' -> {
                    reader.skip(1)
                    while (reader.inBounds) {
                        when (reader.char) {
                            ' ', '\t' -> reader.skip(1)
                            '\r', '\n' -> reader.markLine()
                            else -> break
                        }
                    }
                }
                '"' -> {
                    if (reader.peek(-1) == '\\') {
                        builder.append(reader.char)
                        reader.skip(1)
                    } else if (reader.peek(1) == '"' && reader.peek(2) == '"') {
                        while (reader.inBounds && reader.char == '"') {
                            builder.append(reader.char)
                            reader.skip(1)
                        }
                        builder.deleteCharAt(builder.lastIndex)
                        builder.deleteCharAt(builder.lastIndex)
                        builder.deleteCharAt(builder.lastIndex)
                        reader.skip(-3)
                        break
                    } else {
                        builder.append(reader.char)
                        reader.skip(1)
                    }
                }
                else -> {
                    builder.append(reader.char)
                    reader.skip(1)
                }
            }
        }
        reader.expect('"')
        reader.expect('"')
        reader.expect('"')
        return builder.toString()
    }

    fun stringLiteral(): String {
        reader.skip(1)
        if (reader.char == '\'') {
            return multilineLiteral()
        }
        val start = reader.index
        while (reader.inBounds) {
            when (reader.char) {
                '\r', '\n' -> if (reader.peek(-1) == '\\') {
                    reader.skip(1)
                } else {
                    throw IllegalArgumentException("Unterminated string literal at ${reader.exception}")
                }
                '\'' -> break
                else -> reader.skip(1)
            }
        }
        val value = reader.substring(start)
        reader.expect('\'')
        return value
    }

    fun multilineLiteral(): String {
        reader.expect('\'')
        reader.expect('\'')
        if (reader.char == '\r' || reader.char == '\n') {
            reader.markLine()
        }
        val start = reader.index
        while (reader.inBounds) {
            if (reader.char == '\'' && reader.peek(1) == '\'' && reader.peek(2) == '\'') {
                while (reader.inBounds && reader.char == '\'') {
                    reader.skip(1)
                }
                reader.skip(-3)
                break
            }
            reader.skip(1)
        }
        val value = reader.substring(start)
        reader.expect('\'')
        reader.expect('\'')
        reader.expect('\'')
        return value
    }

    fun comment() {
        reader.nextLine()
    }

    fun booleanTrue(): Boolean {
        reader.skip(1)
        if (reader.matches('r', 'u', 'e')) {
            reader.skip(3)
            if (!reader.inBounds) {
                return true
            }
            when (reader.char) {
                ' ', '\t', '\r', '\n', '#' -> {
                    reader.skipSpacesComment()
                    return true
                }
            }
        }
        throw IllegalArgumentException("Unexpected character, expected whitespace or comments till end of line at ${reader.exception}")
    }

    fun booleanFalse(): Boolean {
        reader.skip(1)
        if (reader.matches('a', 'l', 's', 'e')) {
            reader.skip(4)
            if (!reader.inBounds) {
                return false
            }
            when (reader.char) {
                ' ', '\t', '\r', '\n', '#' -> {
                    reader.skipSpacesComment()
                    return false
                }
            }
        }
        throw IllegalArgumentException("Unexpected character, expected whitespace or comments till end of line at ${reader.exception}")
    }

    fun number(negative: Boolean = false): Any {
        var decimal = false
        val builder = StringBuilder()
        while (reader.inBounds) {
            when (reader.char) {
                '.' -> {
                    if (decimal || reader.index + 1 == reader.size) {
                        throw IllegalArgumentException("Unexpected character at ${reader.exception}")
                    }
                    decimal = true
                    builder.append(reader.char)
                    reader.skip(1)
                }
                '_' -> reader.skip(1)
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    builder.append(reader.char)
                    reader.skip(1)
                }
                ' ', '\t', '\r', '\n', '#', ',', ']', '}' -> {
                    if (reader.peek(-1) == '_') {
                        throw IllegalArgumentException("Incomplete number at ${reader.exception}")
                    }
                    break
                }
                else -> throw IllegalArgumentException("Unexpected character at ${reader.exception}")
            }
        }
        return number(decimal, negative, builder.toString())
    }

    fun number(decimal: Boolean, negative: Boolean, string: String): Number {
        return if (negative) {
            if (decimal) {
                -string.toDouble()
            } else if (string.length < 10) {
                -string.toInt()
            } else {
                -string.toLong()
            }
        } else if (decimal) {
            string.toDouble()
        } else if (string.length < 10) {
            string.toInt()
        } else {
            string.toLong()
        }
    }

    fun hex(): Any {
        reader.skip(2)
        var long = 0L
        var index = 0
        while (reader.inBounds) {
            when (reader.char) {
                '_' -> {}
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> long = (long shl 4) or reader.char.digitToInt(16).toLong()
                '#', ' ', '\t', '\r', '\n' -> break
                else -> throw IllegalArgumentException("Unexpected character, expecting 0-9, A-F, whitespace, comment or line break at ${reader.exception}.")
            }
            reader.skip(1)
            if (index++ == 16) {
                throw IllegalArgumentException("Unexpected character length, maximum 16 hex digits are allowed at ${reader.exception}.")
            }
        }
        return long
    }

    fun octal(): Any {
        reader.skip(2)
        var long = 0L
        var index = 0
        while (reader.inBounds) {
            when (reader.char) {
                '_' -> {}
                '0', '1', '2', '3', '4', '5', '6', '7' -> long = (long shl 3) or reader.char.digitToInt(8).toLong()
                '#', ' ', '\t', '\r', '\n' -> break
                else -> throw IllegalArgumentException("Unexpected character, expecting 0-7, whitespace, comment or line break at ${reader.exception}.")
            }
            reader.skip(1)
            if (index++ == 21) {
                throw IllegalArgumentException("Unexpected character length, maximum 21 octal digits are allowed at ${reader.exception}.")
            }
        }
        return long
    }

    fun binary(): Any {
        reader.skip(2)
        var long = 0L
        var index = 0
        while (reader.inBounds) {
            when (reader.char) {
                '_' -> {}
                '1' -> long = (long shl 1) or 1
                '0' -> long = (long shl 1)
                '#', ' ', '\t', '\r', '\n' -> break
                else -> throw IllegalArgumentException("Unexpected character, expecting 0, 1, whitespace, comment or line break at ${reader.exception}.")
            }
            reader.skip(1)
            if (index++ >= 64) {
                throw IllegalArgumentException("Unexpected character length, maximum 64 binary bits are allowed at ${reader.exception}.")
            }
        }
        return long
    }

    fun inlineArray(): List<Any> {
        reader.skip(1)
        val list = list()
        while (reader.inBounds) {
            reader.nextLine()
            if (reader.char == ']') {
                break
            }
            val value = value()
            list.add(value)
            reader.nextLine()
            if (reader.char == ']') {
                break
            }
            reader.expect(',')
        }
        reader.expect(']')
        return list
    }

    fun inlineTable(): Map<String, Any> {
        reader.skip(1)
        val map: MutableMap<String, Any> = map()
        while (reader.inBounds) {
            reader.nextLine()
            if (reader.char == '}') {
                break
            }
            variable(map)
            reader.nextLine()
            if (reader.char == '}') {
                break
            }
            reader.expect(',')
        }
        reader.expect('}')
        return map
    }

}
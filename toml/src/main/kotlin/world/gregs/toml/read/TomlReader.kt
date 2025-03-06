@file:Suppress("UNCHECKED_CAST")

package world.gregs.toml.read

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.toml.Toml
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TomlReader(private val reader: CharReader, private val settings: Toml.Settings) {

    fun read(root: MutableMap<String, Any>): Map<String, Any> {
        var map = root
        while (reader.inBounds) {
            reader.nextLine()
            if (!reader.inBounds) {
                break
            }
            when {
                reader.char == '[' -> map = title(root)
                reader.char == '#' -> comment()
                (reader.char.isLetterOrDigit() || reader.char == '"' || reader.char == '\'') -> variable(map)
                else -> throw IllegalArgumentException("Expected variable or table at start of line.")
            }
        }
        return root
    }

    internal fun title(map: MutableMap<String, Any>): MutableMap<String, Any> {
        reader.expect('[')
        if (reader.char == '[') {
            return arrayOfTables(map)
        }
        val childMap = tableTitle(map)
        reader.expect(']')
        return childMap
    }

    private fun tableTitle(map: MutableMap<String, Any>): MutableMap<String, Any> {
        val label = label()
        reader.skipSpaces()
        val entry = map[label]
        var child: MutableMap<String, Any> = when {
            entry == null -> {
                // Set new element
                val child: MutableMap<String, Any> = Object2ObjectOpenHashMap()
                map[label] = child
                child
            }
            reader.inBounds && reader.char == ']' -> {
                if (entry is Map<*, *> && (entry as Map<String, Any>).values.any { it is Map<*, *> }) {
                    entry as MutableMap<String, Any>
                } else {
                    throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
                }
            }
            // Get last element
            entry is List<*> -> (entry as MutableList<*>).last() as MutableMap<String, Any>
            entry is Map<*, *> -> entry as MutableMap<String, Any>
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
        val label = label()
        reader.skipSpaces()
        // Nest inside a list if at end of title
        if (reader.char == ']') {
            val child = when (val current = map[label]) {
                null -> {
                    // Set new list and element
                    val list = ObjectArrayList<Any>()
                    map[label] = list
                    val element: MutableMap<String, Any> = Object2ObjectOpenHashMap()
                    list.add(element)
                    element
                }
                is List<*> -> {
                    // Append new element
                    val list = (current as MutableList<Any>)
                    val element: MutableMap<String, Any> = Object2ObjectOpenHashMap()
                    list.add(element)
                    element
                }
                is Array<*> -> throw IllegalArgumentException("Can't extend an inline array at ${reader.exception}.")
                else -> throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
            }
            return child
        }
        var child = when (val current = map[label]) {
            null -> {
                // Set new element
                val element = Object2ObjectOpenHashMap<String, Any>()
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

    fun variable(map: MutableMap<String, Any>, requireEnd: Boolean = true) {
        if (!reader.inBounds) {
            return
        }
        val label = label()
        reader.skipSpaces()
        when (reader.char) {
            '.' -> {
                reader.expect('.')
                reader.skipSpaces()
            }
            '"', '\'' -> {}
            else -> {
                if (map.containsKey(label)) {
                    throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
                }
                reader.expect('=')
                reader.skipSpaces()
                map[label] = value()
                reader.skipSpacesComment()
                if (reader.inBounds && requireEnd) {
                    reader.expectLineBreak()
                }
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
            val child = Object2ObjectOpenHashMap<String, Any>()
            map[label] = child
            child
        }
        variable(child, requireEnd)
    }

    fun label(): String = when (reader.char) {
        '"', '\'' -> {
            // Read between quotes
            val quote = reader.char
            reader.expect(quote)
            val start = reader.index
            while (reader.inBounds) {
                when (reader.char) {
                    '\n', '\r' -> throw IllegalArgumentException("Unterminated string at ${reader.exception}")
                    quote -> break
                    else -> reader.skip(1)
                }
            }
            val label = reader.substring(start)
            reader.expect(quote)
            label
        }
        else -> buildString {
            reader.skipSpaces()
            // Read until reached a new level of nesting or end of table title
            while (reader.inBounds) {
                when (reader.char) {
                    '\n', '\r' -> throw IllegalArgumentException("Unterminated string at ${reader.exception}")
                    '"', '\'', '.', ']', '=' -> break
                    ' ', '\t' -> {
                        reader.skipSpaces()
                        when (reader.char) {
                            '"', '\'', '.', ']', '=' -> break
                        }
                        reader.expect('.')
                        reader.skipSpaces()
                    }
                    else -> append(reader.char)
                }
                reader.skip(1)
            }
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
        in '0'..'9', '-', '+', 'i', 'n' -> number()
        else -> throw IllegalArgumentException("Unexpected character, expecting string, number, boolean, inline array or inline table at ${reader.exception}")
    }

    fun basicString(): String {
        reader.expect('"')
        if (reader.char == '"') {
            reader.expect('"')
            reader.expect('"')
            return multiLineString()
        }
        val start = reader.index
        while (reader.inBounds) {
            when (reader.char) {
                '\n', '\r' -> throw IllegalArgumentException("Unterminated string at ${reader.exception}")
                '"' -> if (reader.peek(-1) != '\\') break
            }
            reader.skip(1)
        }
        val value = reader.substring(start)
        reader.expect('"')
        return value
    }

    fun multiLineString(): String {
        while (reader.inBounds && (reader.char == '\r' || reader.char == '\n')) {
            reader.skip(1)
        }
        val builder = StringBuilder()
        while (reader.inBounds) {
            when (reader.char) {
                '\\' -> {
                    reader.skip(1)
                    while (reader.inBounds) {
                        when (reader.char) {
                            ' ', '\t', '\n', '\r' -> reader.skip(1)
                            else -> break
                        }
                    }
                }
                '"' -> if (reader.peek(-1) == '\\') {
                    builder.append(reader.char)
                    reader.skip(1)
                } else if (reader.peek(1) == '"' && reader.peek(2) == '"') {
                    while (reader.inBounds && reader.char == '"') {
                        reader.skip(1)
                    }
                    reader.skip(-3)
                    break
                } else {
                    builder.append(reader.char)
                    reader.skip(1)
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
        reader.expect('\'')
        if (reader.char == '\'') {
            return multilineLiteral()
        }
        val start = reader.index
        while (reader.inBounds) {
            when (reader.char) {
                '\n', '\r' -> if (reader.peek(-1) == '\\') {
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
        if (reader.char == '\n' || reader.char == '\r') {
            reader.markLine()
        }
        val start = reader.index
        // TODO will need to validate end of line, whether that's , ], }, #, ' '
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

    fun booleanTrue(): Any {
        if (reader.index + 3 < reader.size) {
            if (reader.peek(1) == 'r' && reader.peek(2) == 'u' && reader.peek(3) == 'e') {
                if (reader.index + 4 == reader.size || reader.peek(4) == ' ' || reader.peek(4) == '\r' || reader.peek(4) == '\n' || reader.peek(4) == '#') {
                    reader.skip(4)
                    reader.skipSpacesComment()
                    return true
                }
            }
        }
        throw IllegalArgumentException("Unexpected character, expected whitespace or comments till end of line at ${reader.exception}")
    }

    fun booleanFalse(): Any {
        if (reader.index + 4 < reader.size) {
            if (reader.peek(1) == 'a' && reader.peek(2) == 'l' && reader.peek(3) == 's' && reader.peek(4) == 'e') {
                if (reader.index + 5 == reader.size || reader.peek(5) == ' ' || reader.peek(5) == '\r' || reader.peek(5) == '\n' || reader.peek(5) == '#') {
                    reader.skip(5)
                    reader.skipSpacesComment()
                    return false
                }
            }
        }
        throw IllegalArgumentException("Unexpected character, expected whitespace or comments till end of line at ${reader.exception}")
    }

    fun number(): Any {
        var negative = false
        when (reader.char) {
            '0' -> {
                when (reader.peek) {
                    'x' -> return hex()
                    'o' -> return octal()
                    'b' -> return binary()
                }
            }
            '+' -> reader.skip(1)
            '-' -> {
                reader.skip(1)
                negative = true
            }
        }

        if (reader.char == 'i') {
            if (reader.peek(1) == 'n' && reader.peek(2) == 'f') {
                reader.skip(3)
                return Double.POSITIVE_INFINITY
            } else {
                throw IllegalArgumentException("Unexpected character, expecting string, number, boolean, inline array or inline table at ${reader.exception}")
            }
        } else if (reader.char == 'n') {
            if (reader.peek(1) == 'a' && reader.peek(2) == 'n') {
                reader.skip(3)
                return Double.NaN
            } else {
                throw IllegalArgumentException("Unexpected character, expecting string, number, boolean, inline array or inline table at ${reader.exception}")
            }
        }
        var decimal = false
        var power = 0
        val builder = StringBuilder()
        while (reader.inBounds) {
            when (reader.char) {
                '.' -> {
                    if (decimal || reader.index + 1 == reader.size || reader.peek(1).lowercaseChar() == 'e') {
                        throw IllegalArgumentException("Unexpected character at ${reader.exception}")
                    }
                    decimal = true
                    reader.skip(1)
                }
                '_' -> reader.skip(1)
                in '0'..'9' -> {
                    builder.append(reader.char)
                    reader.skip(1)
                }
                ' ', '\n', '\r', '#', ',', ']', '}' -> {
                    if (reader.peek(-1) == '_') {
                        throw IllegalArgumentException("Incomplete number at ${reader.exception}")
                    }
                    break
                }
                'E', 'e' -> {
                    power = scientificNotation()
                    break
                }
                '-' -> return localDateTime(builder)
                ':' -> return localTime(builder)
                else -> throw IllegalArgumentException("Unexpected character at ${reader.exception}")
            }
        }
        return reader.number(decimal, negative, power, builder.toString())
    }

    internal fun localDateTime(builder: StringBuilder): Any {
        while (reader.inBounds) {
            when (reader.char) {
                in '0'..'9', '-', '+', ':', '.', 't', 'T', 'z', 'Z' -> {
                    builder.append(reader.char)
                    reader.skip(1)
                }
                ' ' -> {
                    // RFC 3339 support
                    if (builder.length != 10) {
                        break
                    }
                    builder.append('T')
                    reader.skip(1)
                }
                '\t', '#', '\n', '\r' -> break
            }
        }
        if (builder.length == 10) { // e.g. 1979-05-27
            return LocalDate.parse(builder)
        }
        return if (builder.length < 20 || (builder[19] == '.' && builder[builder.length - 6] != '-' && builder[builder.length - 6] != '+')) {
            LocalDateTime.parse(builder)
        } else {
            Instant.parse(builder)
        }
    }

    internal fun localTime(builder: StringBuilder): Any {
        while (reader.inBounds) {
            when (reader.char) {
                in '0'..'9', ':', '.' -> {
                    builder.append(reader.char)
                    reader.skip(1)
                }
                ' ', '\t', '#', '\n', '\r' -> break
            }
        }
        return LocalTime.parse(builder)
    }

    private fun scientificNotation(): Int {
        reader.skip(1)
        var negative = false
        when (reader.char) {
            '-' -> {
                reader.skip(1)
                negative = true
            }
            '+' -> reader.skip(1)
            !in '0'..'9' -> {
                throw IllegalArgumentException("Unexpected character, expecting -, + or digit at ${reader.exception}.")
            }
        }
        val builder = StringBuilder()
        while (reader.inBounds) {
            when (reader.char) {
                '_' -> reader.skip(1)
                in '0'..'9' -> {
                    builder.append(reader.char)
                    reader.skip(1)
                }
                else -> {
                    if (reader.peek(-1) == '_') {
                        throw IllegalArgumentException("Incomplete number at ${reader.exception}")
                    }
                    break
                }
            }
        }
        return reader.number(false, negative, 0, builder.toString()) as Int
    }

    fun hex(): Any {
        reader.skip(2)
        var long = 0L
        var index = 0
        while (reader.inBounds) {
            when (reader.char) {
                '_' -> {}
                in '0'..'9', in 'A'..'F', in 'a'..'f' -> long = (long shl 4) or reader.char.digitToInt(16).toLong()
                '#', ' ', '\n', '\r' -> break
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
                in '0'..'7' -> long = (long shl 3) or reader.char.digitToInt(8).toLong()
                '#', ' ', '\n', '\r' -> break
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
                '#', ' ', '\n', '\r' -> break
                else -> throw IllegalArgumentException("Unexpected character, expecting 0, 1, whitespace, comment or line break at ${reader.exception}.")
            }
            reader.skip(1)
            if (index++ >= 64) {
                throw IllegalArgumentException("Unexpected character length, maximum 64 binary bits are allowed at ${reader.exception}.")
            }
        }
        return long
    }

    fun inlineArray(): Array<Any> {
        reader.skip(1)
        val list = ObjectArrayList<Any>()
        while (reader.inBounds) {
            reader.nextLine()
            if (reader.char == ']') {
                break
            }
            list.add(value())
            reader.nextLine()
            if (reader.char == ']') {
                break
            }
            reader.expect(',')
        }
        reader.expect(']')
        return list.toArray()
    }

    fun inlineTable(): Map<String, Any> {
        reader.skip(1)
        val map: MutableMap<String, Any> = Object2ObjectOpenHashMap()
        while (reader.inBounds) {
            reader.nextLine()
            if (reader.char == '}') {
                break
            }
            variable(map, requireEnd = false)
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
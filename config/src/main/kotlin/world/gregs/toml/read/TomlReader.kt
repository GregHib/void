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
                // Tables
                '[' -> when (reader.peek) {
                    '[' -> when (reader.peek(2)) {
                        '.' -> { // Inherited array of tables
                            reader.skip(3)
                            map = arrayOfTablesTitle(previous)
                            if (reader.char != ']' && reader.peek != ']') {
                                throw IllegalArgumentException("Expected character ']' at ${reader.exception}.")
                            }
                            reader.skip(2)
                        }
                        ' ', '\t' -> { // Array of tables (with spaces)
                            reader.skip(2)
                            reader.skipSpaces()
                            map = arrayOfTablesTitle(root)
                            previous = map
                            if (reader.char != ']' && reader.peek != ']') {
                                throw IllegalArgumentException("Expected character ']' at ${reader.exception}.")
                            }
                            reader.skip(2)
                        }
                        ']' -> throw IllegalArgumentException("Empty bare keys are not allowed at ${reader.exception}.")
                        else -> { // Array of tables
                            reader.skip(2)
                            map = arrayOfTablesTitle(root)
                            previous = map
                            if (reader.char != ']' && reader.peek != ']') {
                                throw IllegalArgumentException("Expected character ']' at ${reader.exception}.")
                            }
                            reader.skip(2)
                        }
                    }
                    ']' -> throw IllegalArgumentException("Empty bare keys are not allowed at ${reader.exception}.")
                    '.' -> { // Inherited table
                        reader.skip(2)
                        map = tableTitle(previous)
                        if (reader.char != ']') {
                            throw IllegalArgumentException("Expected character ']' at ${reader.exception}.")
                        }
                        reader.skip(1)
                    }
                    ' ', '\t' -> { // Tables (with spaces)
                        reader.skip(1)
                        reader.skipSpaces()
                        map = tableTitle(root)
                        previous = map
                        if (reader.char != ']') {
                            throw IllegalArgumentException("Expected character ']' at ${reader.exception}.")
                        }
                        reader.skip(1)
                    }
                    else -> { // Tables
                        reader.skip(1)
                        map = tableTitle(root)
                        previous = map
                        if (reader.char != ']') {
                            throw IllegalArgumentException("Expected character ']' at ${reader.exception}.")
                        }
                        reader.skip(1)
                    }
                }
                '=' -> throw IllegalArgumentException("Expected variable or table at start of line.")
                else -> {
                    variable(map)
                    reader.skipSpaces()
                    if (reader.inBounds && reader.char == '#') {
                        reader.skipComment()
                    }
                    if (reader.inBounds && reader.char != '\r' && reader.char != '\n') {
                        throw IllegalArgumentException("Expected newline at ${reader.exception}.")
                    }
                }
            }
            reader.nextLine()
        }
        return root
    }

    private fun tableTitle(map: MutableMap<String, Any>): MutableMap<String, Any> {
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
            is List<*> -> entry.last() as MutableMap<String, Any>
            is Map<*, *> -> entry as MutableMap<String, Any>
            else -> throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
        }
        if (reader.inBounds) {
            when (reader.char) {
                '.' -> {
                    reader.skip(1)
                    reader.skipSpaces()
                    child = tableTitle(child)
                }
                '"', '\'' -> child = tableTitle(child)
            }
        }
        return child
    }

    private fun arrayOfTablesTitle(map: MutableMap<String, Any>): MutableMap<String, Any> {
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
                val last = current.last()
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
                    reader.skip(1)
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
                reader.skip(1)
                reader.skipSpaces()
            }
            '=' -> {
                if (map.containsKey(label)) {
                    throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
                }
                reader.skip(1)
                reader.skipSpaces()
                val value = value()
                map[label] = value
                return
            }
            else -> throw IllegalArgumentException("Unexpected character, expecting equals at ${reader.exception}.")
        }
        var child = map[label]
        if (child != null && child !is Map<*, *>) {
            throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
        } else if (child == null) {
            child = map()
            map[label] = child
        }
        variable(child as MutableMap<String, Any>)
    }

    fun label(): String = when (reader.char) {
        '"', '\'' -> quotedLabel()
        else -> bareLabel()
    }

    private fun bareLabel(): String {
        val start = reader.index
        // Read until reached a new level of nesting or end of table title
        while (reader.inBounds) {
            when (reader.char) {
                '\r', '\n' -> break
                '"', '\'', '.', ']', '=', ' ', '\t' -> return reader.substring(start)
            }
            reader.skip(1)
        }
        throw IllegalArgumentException("Unterminated string at ${reader.exception}.")
    }

    private fun quotedLabel(): String {
        val quote = reader.char
        reader.skip(1)
        val start = reader.index
        while (reader.inBounds) {
            when (reader.char) {
                '\r', '\n' -> break
                quote -> {
                    reader.skip(1)
                    return reader.substring(start, reader.index - 1)
                }
                else -> reader.skip(1)
            }
        }
        throw IllegalArgumentException("Unterminated string at ${reader.exception}.")
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
            else -> number(negative = false)
        }
        '-' -> {
            reader.skip(1)
            number(negative = true)
        }
        '+' -> {
            reader.skip(1)
            number(negative = false)
        }
        '1', '2', '3', '4', '5', '6', '7', '8', '9' -> number(negative = false)
        else -> throw IllegalArgumentException("Unexpected character, expecting string, number, boolean, inline array or inline table at ${reader.exception}.")
    }

    fun basicString(): String {
        reader.skip(1)
        if (reader.char == '"') {
            if (reader.peek == '"') {
                reader.skip(2)
                return multiLineString()
            }
//            throw IllegalArgumentException("Expected character '\"' at ${reader.exception}.")
        }
        val start = reader.index
        while (reader.inBounds) {
            when (reader.char) {
                '\r', '\n' -> break
                '\\' -> reader.skip(1)
                '"' -> {
                    reader.skip(1)
                    return reader.substring(start, reader.index - 1)
                }
            }
            reader.skip(1)
        }
        throw IllegalArgumentException("Unterminated string at ${reader.exception}.")
    }

    fun spaceOrLine(char: Char) = char == ' ' || char == '\t' || char == '\r' || char == '\n'

    fun multiLineString(): String {
        if (reader.char == '\r' || reader.char == '\n') {
            reader.skipLine()
        }
        val builder = StringBuilder()
        while (reader.inBounds) {
            when (reader.char) {
                '\\' -> {
                    reader.skip(1)
                    if (spaceOrLine(reader.char)) {
                        while (reader.inBounds && spaceOrLine(reader.char)) {
                            reader.skip(1)
                        }
                        continue
                    }
                }
                '"' -> {
                    if (reader.inBounds(2) && reader.peek(1) == '"' && reader.peek(2) == '"') {
                        while (reader.inBounds && reader.char == '"') {
                            builder.append(reader.char)
                            reader.skip(1)
                        }
                        builder.delete(builder.length - 3, builder.length)
                        return builder.toString()
                    }
                }
            }
            builder.append(reader.char)
            reader.skip(1)
        }
        throw IllegalArgumentException("Unterminated multiline string at ${reader.exception}.")
    }

    fun stringLiteral(): String {
        reader.skip(1)
        if (reader.char == '\'') {
            if (reader.peek != '\'') {
                throw IllegalArgumentException("Expected character ''' at ${reader.exception}.")
            }
            reader.skip(2)
            return multilineLiteral()
        }
        val start = reader.index
        while (reader.inBounds) {
            when (reader.char) {
                '\\' -> reader.skip(1)
                '\r', '\n' -> break
                '\'' -> {
                    reader.skip(1)
                    return reader.substring(start, reader.index - 1)
                }
                else -> reader.skip(1)
            }
        }
        throw IllegalArgumentException("Unterminated string literal at ${reader.exception}.")
    }

    fun multilineLiteral(): String {
        if (reader.char == '\r' || reader.char == '\n') {
            reader.skipLine()
        }
        val start = reader.index
        while (reader.inBounds) {
            if (reader.char == '\'' && reader.peek(1) == '\'' && reader.peek(2) == '\'') {
                while (reader.inBounds && reader.char == '\'') {
                    reader.skip(1)
                }
                return reader.substring(start, reader.index - 3)
            }
            reader.skip(1)
        }
        throw IllegalArgumentException("Unterminated multiline string literal at ${reader.exception}.")
    }

    fun booleanTrue(): Boolean {
        if (!reader.inBounds(4) || reader.peek(1) != 'r' || reader.peek(2) != 'u' || reader.peek(3) != 'e') {
            throw IllegalArgumentException("Unexpected character, expected true at ${reader.exception}.")
        }
        reader.skip(4)
        return true
    }

    fun booleanFalse(): Boolean {
        if (!reader.inBounds(5) || reader.peek(1) != 'a' || reader.peek(2) != 'l' || reader.peek(3) != 's' || reader.peek(4) != 'e') {
            throw IllegalArgumentException("Unexpected character, expected false at ${reader.exception}.")
        }
        reader.skip(5)
        return false
    }

    fun number(negative: Boolean): Any {
        var long = 0L
        var double = 0.0
        var decimalFactor = 1.0
        var decimal = false
        while (reader.inBounds) {
            when (reader.char) {
                '.' -> {
                    if (decimal || reader.index + 1 == reader.size) {
                        throw IllegalArgumentException("Unexpected decimal at ${reader.exception}.")
                    }
                    double = long.toDouble()
                    decimal = true
                    reader.skip(1)
                }
                '_' -> {
                    reader.skip(1)
                }
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    val digit = reader.char - '0'
                    if (decimal) {
                        decimalFactor /= 10
                        double += digit * decimalFactor
                    } else {
                        long = long * 10 + digit
                    }
                    reader.skip(1)
                }
                ' ', '\t', '\r', '\n', '#', ',', ']', '}' -> {
                    if (reader.peek(-1) == '_') {
                        throw IllegalArgumentException("Incomplete number at ${reader.exception}.")
                    }
                    break
                }
                else -> throw IllegalArgumentException("Unexpected character at ${reader.exception}.")
            }
        }
        return if (negative) {
            when {
                decimal -> -double
                long < Int.MAX_VALUE -> -(long.toInt())
                else -> -long
            }
        } else {
            when {
                decimal -> double
                long < Int.MAX_VALUE -> long.toInt()
                else -> long
            }
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
                reader.skip(1)
                return list
            }
            val value = value()
            list.add(value)
            reader.nextLine()
            when (reader.char) {
                ']' -> {
                    reader.skip(1)
                    return list
                }
                ',' -> reader.skip(1)
                else -> throw IllegalArgumentException("Expected character ',' at ${reader.exception}.")
            }
        }
        throw IllegalArgumentException("Expected character ']' at ${reader.exception}.")
    }

    fun inlineTable(): Map<String, Any> {
        reader.skip(1)
        val map: MutableMap<String, Any> = map()
        while (reader.inBounds) {
            reader.nextLine()
            if (reader.char == '}') {
                reader.skip(1)
                return map
            }
            variable(map)
            reader.nextLine()
            when (reader.char) {
                '}' -> {
                    reader.skip(1)
                    return map
                }
                ',' -> reader.skip(1)
            }
        }
        throw IllegalArgumentException("Expected character '}' at ${reader.exception}.")
    }

}
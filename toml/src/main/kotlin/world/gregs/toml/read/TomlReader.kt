package world.gregs.toml.read

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.toml.Toml

class TomlReader(private val reader: CharReader, private val settings: Toml.Settings) {

    fun read(root: MutableMap<String, Any>): Map<String, Any> {
        var map = root
        while (reader.inBounds) {
            reader.nextLine()
            if (!reader.inBounds) {
                break
            }
            when {
                reader.char == '[' -> map = tableTitle(root)
                reader.char == '#' -> comment()
                (reader.char.isLetterOrDigit() || reader.char == '"' || reader.char == '\'') -> variable(map)
                else -> throw IllegalArgumentException("Expected variable or table at start of line.")
            }
        }
        return root
    }

    internal fun tableTitle(map: MutableMap<String, Any>): MutableMap<String, Any> {
        reader.expect('[')
        if (reader.char == '[') {
            return arrayOfTables(map)
        }
        val childMap = title(map)
        reader.expect(']')
        return childMap
    }

    private fun title(map: MutableMap<String, Any>): MutableMap<String, Any> {
        val label = titleLabel()
        reader.skipSpaces()
        val sub = map[label]
        if (sub is Map<*, *> && sub[sub.keys.first()] !is Map<*, *>) {
            throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
        }
        var child = childMap(map, label)
        if (reader.inBounds) {
            when (reader.char) {
                '.' -> {
                    reader.expect('.')
                    reader.skipSpaces()
                    child = title(child)
                }
                '"', '\'' -> child = title(child)
            }
        }
        return child
    }

    @Suppress("UNCHECKED_CAST")
    private fun childMap(map: MutableMap<String, Any>, label: String): MutableMap<String, Any> = if (map.containsKey(label)) {
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

    private fun arrayOfTables(map: MutableMap<String, Any>): MutableMap<String, Any> {
        reader.expect('[')
        val childMap = arrayTitle(map)
        reader.expect(']')
        reader.expect(']')
        return childMap
    }

    private fun arrayTitle(map: MutableMap<String, Any>): MutableMap<String, Any> {
        val label = titleLabel()
        reader.skipSpaces()
        // Nest inside a list if at end of title
        if (reader.char == ']') {
            if (map.containsKey(label) && map[label] !is List<*>) {
                throw IllegalArgumentException("Can't redefine existing key at ${reader.exception}.")
            }
            val array = ObjectArrayList<Any>()
            map[label] = array
            val child: MutableMap<String, Any> = Object2ObjectOpenHashMap()
            array.add(child)
            return child
        } else {
            var child = childMap(map, label)
            if (reader.inBounds) {
                when (reader.char) {
                    '.' -> {
                        reader.expect('.')
                        reader.skipSpaces()
                        child = arrayTitle(child)
                    }
                    '"', '\'' -> child = arrayTitle(child)
                }
            }
            return child
        }
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
        val child = childMap(map, label)
        variable(child, requireEnd)
    }

    fun label(): String {
        when (reader.char) {
            '"' -> return string('"')
            '\'' -> return string('\'')
            else -> {
                val start = reader.index
                while (reader.inBounds) {
                    when (reader.char) {
                        '\n', '\r' -> throw IllegalArgumentException("Unterminated string at ${reader.exception}")
                        '"', '\'', '.', ' ', ']' -> break
                        else -> reader.skip(1)
                    }
                }
                return reader.substring(start)
            }
        }
    }

    fun titleLabel(): String {
        when (reader.char) {
            '"' -> return string('"')
            '\'' -> return string('\'')
            else -> {
                val builder = StringBuilder()
                while (reader.inBounds) {
                    when (reader.char) {
                        '\n', '\r' -> throw IllegalArgumentException("Unterminated string at ${reader.exception}")
                        '"', '\'', '.', ']' -> break
                        ' ' -> reader.skip(1)
                        else -> {
                            builder.append(reader.char)
                            reader.skip(1)
                        }
                    }
                }
                return builder.toString()
            }
        }
    }

    private fun string(char: Char): String {
        reader.expect(char)
        val start = reader.index
        while (reader.inBounds) {
            when (reader.char) {
                '\n', '\r' -> throw IllegalArgumentException("Unterminated string at ${reader.exception}")
                char -> break
                else -> reader.skip(1)
            }
        }
        val label = reader.substring(start)
        reader.expect(char)
        return label
    }

    fun value(): Any {
        return when (reader.char) {
            '[' -> inlineArray()
            '{' -> inlineTable()
            '\'' -> stringLiteral()
            '"' -> doubleQuotedString()
            't' -> booleanTrue()
            'f' -> booleanFalse()
            '#' -> throw IllegalArgumentException("Invalid comment")
            in '0'..'9', '-', '+', 'i', 'n' -> number()
            else -> throw IllegalArgumentException("Unexpected character, expecting string, number boolean inline array or inline table at ${reader.exception}")
        }
    }

    fun doubleQuotedString(): String {
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
                '"' -> if (reader.peek(-1) == '\\') {
                    reader.skip(1)
                } else {
                    break
                }
                else -> reader.skip(1)
            }
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
                    'x' -> {
                        reader.skip(2)
                        return hex()
                    }
                    'o' -> {
                        reader.skip(2)
                        return octal()
                    }
                    'b' -> {
                        reader.skip(2)
                        return binary()
                    }
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
                throw IllegalArgumentException("Unexpected character, expecting string, number boolean inline array or inline table at ${reader.exception}")
            }
        } else if (reader.char == 'n') {
            if (reader.peek(1) == 'a' && reader.peek(2) == 'n') {
                reader.skip(3)
                return Double.NaN
            } else {
                throw IllegalArgumentException("Unexpected character, expecting string, number boolean inline array or inline table at ${reader.exception}")
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
                else -> throw IllegalArgumentException("Unexpected character at ${reader.exception}")
            }
        }
        return reader.number(decimal, negative, power, builder.toString())
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
                throw IllegalArgumentException("Unexpected character, expected -, + or digit at ${reader.exception}")
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
        TODO("Hex not yet implemented")
    }

    fun octal(): Any {
        TODO("Octal not yet implemented")
    }

    fun binary(): Any {
        TODO("Binary not yet implemented")
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
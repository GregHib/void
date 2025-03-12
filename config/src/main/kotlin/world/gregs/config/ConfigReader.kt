package world.gregs.config

import java.io.BufferedInputStream

abstract class ConfigReader {
    abstract val buffer: ByteArray
    private var bufferIndex: Int = 0
    private var section: String = ""

    open fun map(): MutableMap<String, Any> = mutableMapOf()

    open fun list(): MutableList<Any> = mutableListOf()

    abstract fun set(section: String, key: String, value: Any)

    fun parse(input: BufferedInputStream) {
        var byte = input.read()
        while (byte != EOF) {
            when (byte) {
                SPACE, TAB -> { // Skip whitespace
                    while (byte == SPACE || byte == TAB) {
                        byte = input.read()
                    }
                    continue
                }
                HASH -> { // Skip comments
                    while (byte != EOF && byte != RETURN && byte != NEWLINE) {
                        byte = input.read()
                    }
                    continue
                }
                RETURN, NEWLINE -> { // Skip newlines
                    while (byte == RETURN || byte == NEWLINE) {
                        byte = input.read()
                    }
                    continue
                }
                OPEN_BRACKET -> { // Section
                    byte = input.read() // skip [
                    val inherit = byte == DOT

                    bufferIndex = 0
                    while (byte != EOF && byte != CLOSE_BRACKET) {
                        buffer[bufferIndex++] = byte.toByte()
                        byte = input.read()
                    }

                    if (inherit) {
                        section += String(buffer, 0, bufferIndex)
                    } else {
                        section = String(buffer, 0, bufferIndex)
                    }
                    byte = input.read() // skip ]
                    require(byte != CLOSE_BRACKET) { "Array of tables are not supported in section '$section'." }
                }
                DOUBLE_QUOTE -> {
                    val key = quotedString(input)
                    byte = skipKeyValueWhitespace(input)
                    val value: Any = parseType(byte, input)
                    set(section, key, value)
                    byte = input.read()
                }
                else -> {
                    val key = bareKey(byte, input)
                    byte = skipKeyValueWhitespace(input)
                    val value: Any = parseType(byte, input)
                    set(section, key, value)
                    byte = input.read()
                    require(byte != DOUBLE_QUOTE) { "Multi-line strings are not supported in '$section'." }
                }
            }
        }

        input.close()
    }

    private fun skipComment(input: BufferedInputStream): Int {
        var byte = input.read()
        while (byte != EOF && byte != RETURN && byte != NEWLINE) {
            byte = input.read()
        }
        return byte
    }

    private fun bareKey(currentByte: Int, input: BufferedInputStream): String {
        bufferIndex = 0
        var byte = currentByte
        while (byte != EOF && byte != SPACE && byte != TAB && byte != EQUALS && byte != RETURN && byte != NEWLINE) {
            buffer[bufferIndex++] = byte.toByte()
            byte = input.read()
        }
        require(bufferIndex > 0) { "Expected key in section '${section}." }
        // Ignore current byte as we know it's junk
        return String(buffer, 0, bufferIndex)
    }

    private fun parseType(currentByte: Int, input: BufferedInputStream): Any = when (currentByte) {
        DOUBLE_QUOTE -> quotedString(input)
        SINGLE_QUOTE -> literalString(input)
        OPEN_BRACKET -> parseArray(input)
        OPEN_BRACE -> parseMap(input)
        T -> parseTrue(input)
        F -> parseFalse(input)
        MINUS -> parseNumber(input, true, 0)
        PLUS -> parseNumber(input, false, 0)
        ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE ->
            parseNumber(input, false, currentByte - ZERO)
        else -> throw IllegalArgumentException("Unexpected character '${currentByte.toChar()}' in section '$section'")
    }

    private fun parseNumber(input: BufferedInputStream, negative: Boolean, initialDigit: Int): Number {
        var value = initialDigit.toLong()
        var byte = input.read()
        while (byte != EOF && byte != DOT && byte != RETURN && byte != NEWLINE && byte != COMMA && byte != CLOSE_BRACE && byte != CLOSE_BRACKET) {
            when (byte) {
                UNDERSCORE -> {
                    // Skip underscores
                }
                ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE -> {
                    val digit = byte - ZERO
                    value = value * 10 + digit
                }
                else -> break
            }
            byte = input.read()
        }
        if (byte == DOT) {
            var decimalFactor = 1.0
            var doubleValue = value.toDouble()
            byte = input.read() // Skip the decimal
            require(byte == ZERO || byte == ONE || byte == TWO || byte == THREE || byte == FOUR || byte == FIVE || byte == SIX || byte == SEVEN || byte == EIGHT || byte == NINE) {
                "Expecting a digit after decimal point in section '$section'."
            }
            while (byte != EOF && byte != DOT && byte != RETURN && byte != NEWLINE && byte != COMMA && byte != CLOSE_BRACE && byte != CLOSE_BRACKET) {
                when (byte) {
                    UNDERSCORE -> {
                        // Skip underscores
                    }
                    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE -> {
                        val digit = byte - ZERO
                        decimalFactor /= 10
                        doubleValue += digit * decimalFactor
                    }
                    else -> break
                }
                byte = input.read()
            }
            buffer[0] = byte.toByte()
            return if (negative) -doubleValue else doubleValue
        }
        buffer[0] = byte.toByte()
        return if (negative) -value else value
    }

    private fun parseTrue(input: BufferedInputStream): Boolean {
        if (input.read() == R && input.read() == U && input.read() == E) {
            return true
        }
        throw IllegalArgumentException("Expected boolean 'true' at section $section")
    }

    private fun parseFalse(input: BufferedInputStream): Boolean {
        if (input.read() == A && input.read() == L && input.read() == S && input.read() == E) {
            return false
        }
        throw IllegalArgumentException("Expected boolean 'false' at section $section")
    }

    private fun parseMap(input: BufferedInputStream): Map<String, Any> {
        val map = map()
        var byte = input.read() // skip opening brace

        while (byte != EOF && byte != CLOSE_BRACE) {
            // Skip whitespace and commas
            byte = skipMultilineWhitespace(byte, input)
            val mapKey = when (byte) {
                CLOSE_BRACE -> {
                    break
                }
                DOUBLE_QUOTE -> quotedString(input)
                else -> bareKey(byte, input)
            }
            byte = skipKeyValueWhitespace(input)
            val value = parseType(byte, input)
            map[mapKey] = value
            byte = input.read()
        }

        return map
    }

    private fun parseArray(input: BufferedInputStream): List<Any> {
        val values = list()
        var byte = input.read() // skip opening bracket
        while (byte != EOF && byte != CLOSE_BRACKET) {
            // Skip whitespace and commas
            byte = skipMultilineWhitespace(byte, input)
            when (byte) {
                CLOSE_BRACKET -> break
                DOUBLE_QUOTE -> {
                    values.add(quotedString(input))
                    byte = input.read()
                }
                SINGLE_QUOTE -> {
                    values.add(literalString(input))
                    byte = input.read()
                }
                OPEN_BRACKET -> {
                    values.add(parseArray(input))
                    byte = input.read()
                }
                OPEN_BRACE -> {
                    values.add(parseMap(input))
                    byte = input.read()
                }
                T -> {
                    values.add(parseTrue(input))
                    byte = input.read()
                }
                F -> {
                    values.add(parseFalse(input))
                    byte = input.read()
                }
                MINUS -> {
                    values.add(parseNumber(input, true, 0))
                    byte = buffer[0].toInt()
                }
                PLUS -> {
                    values.add(parseNumber(input, false, 0))
                    byte = buffer[0].toInt()
                }
                ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE -> {
                    values.add(parseNumber(input, false, byte - ZERO))
                    byte = buffer[0].toInt()
                }
                HASH -> {
                    skipComment(input)
                    byte = input.read()
                }
                else -> throw IllegalArgumentException("Unexpected character '${if (byte == NEWLINE) "\\n" else if (byte == RETURN) "\\r" else byte.toChar()}' in section $section")
            }
        }
        return values
    }

    private fun skipKeyValueWhitespace(input: BufferedInputStream): Int {
        var byte = input.read()
        while (byte == SPACE || byte == TAB || byte == EQUALS) {
            byte = input.read()
        }
        // Next byte is unknown to hand it over to the next process
        return byte
    }

    private fun skipMultilineWhitespace(currentByte: Int, input: BufferedInputStream): Int {
        var byte = currentByte
        while (byte == SPACE || byte == TAB || byte == COMMA || byte == RETURN || byte == NEWLINE) {
            byte = input.read()
        }
        if (byte == HASH) {
            byte = skipComment(input)
        }
        // Next byte is unknown to hand it over to the next process
        return byte
    }

    private fun quotedString(input: BufferedInputStream): String {
        bufferIndex = 0
        var byte = input.read() // skip opening quote
        while (byte != EOF && byte != DOUBLE_QUOTE) {
            if (byte == BACKSLASH) {
                byte = input.read()
                if (byte == DOUBLE_QUOTE) {
                    buffer[bufferIndex++] = DOUBLE_QUOTE.toByte()
                    byte = input.read()
                    continue
                } else {
                    buffer[bufferIndex++] = BACKSLASH.toByte()
                }
            }

            buffer[bufferIndex++] = byte.toByte()
            byte = input.read()
        }
        return String(buffer, 0, bufferIndex)
    }

    private fun literalString(input: BufferedInputStream): String {
        bufferIndex = 0
        var byte = input.read() // skip opening quote
        while (byte != EOF && byte != SINGLE_QUOTE) {
            buffer[bufferIndex++] = byte.toByte()
            byte = input.read()
        }
        return String(buffer, 0, bufferIndex)
    }

    companion object {
        private const val EOF = -1
        private const val SPACE = ' '.code
        private const val TAB = '\t'.code
        private const val HASH = '#'.code
        private const val RETURN = '\r'.code
        private const val NEWLINE = '\n'.code
        private const val OPEN_BRACKET = '['.code
        private const val CLOSE_BRACKET = ']'.code
        private const val DOT = '.'.code
        private const val DOUBLE_QUOTE = '"'.code
        private const val SINGLE_QUOTE = '\''.code
        private const val EQUALS = '='.code
        private const val OPEN_BRACE = '{'.code
        private const val CLOSE_BRACE = '}'.code
        private const val COMMA = ','.code
        private const val T = 't'.code
        private const val F = 'f'.code
        private const val MINUS = '-'.code
        private const val PLUS = '+'.code
        private const val ZERO = '0'.code
        private const val ONE = '1'.code
        private const val TWO = '2'.code
        private const val THREE = '3'.code
        private const val FOUR = '4'.code
        private const val FIVE = '5'.code
        private const val SIX = '6'.code
        private const val SEVEN = '7'.code
        private const val EIGHT = '8'.code
        private const val NINE = '9'.code
        private const val UNDERSCORE = '_'.code
        private const val R = 'r'.code
        private const val U = 'u'.code
        private const val E = 'e'.code
        private const val A = 'a'.code
        private const val L = 'l'.code
        private const val S = 's'.code
        private const val BACKSLASH = '\\'.code
    }
}
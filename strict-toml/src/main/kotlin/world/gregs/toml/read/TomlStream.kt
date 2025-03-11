package world.gregs.toml.read

import java.io.BufferedInputStream

class TomlStream {

    interface Api {
        fun arrayOfTables(addressBuffer: Array<Any>, addressSize: Int) {}
        fun table(addressBuffer: Array<Any>, addressSize: Int) {}
        fun inlineTable(addressBuffer: Array<Any>, addressSize: Int) {}
        fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: Double) {}
        fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: Long) {}
        fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: String) {}
        fun appendMap(addressBuffer: Array<Any>, addressSize: Int, key: String, value: Boolean) {}
        fun mapEnd(addressBuffer: Array<Any>, addressSize: Int) {}

        fun list(addressBuffer: Array<Any>, addressSize: Int) {}
        fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: Double) {}
        fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: Long) {}
        fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: String) {}
        fun appendList(addressBuffer: Array<Any>, addressSize: Int, value: Boolean) {}
        fun listEnd(addressBuffer: Array<Any>, addressSize: Int) {}
    }

    fun read(input: BufferedInputStream, api: Api, buffer: ByteArray, address: Array<Any>) {
        var bufferIndex = 0
        var addressIndex = 0
        var previousIndex = 0
        var byte = input.read()

        while (byte != EOF) {
            byte = when (byte) {
                SPACE, TAB, RETURN -> {
                    // Skip whitespace
                    input.read()
                }
                LINE -> {
                    // New line
                    bufferIndex = 0
                    input.read()
                }
                HASH -> {
                    // Comment, skip to end of line
                    while (byte != LINE && byte != EOF) {
                        byte = input.read()
                    }
                    input.read()
                }
                OPEN_BRACKET -> {
                    // Handle table or array of tables
                    byte = input.read()
                    // Check if it's an array of tables
                    val isArrayOfTables = byte == OPEN_BRACKET
                    if (isArrayOfTables) {
                        byte = input.read()
                    }

                    val absoluteAddress = byte != DOT
                    if (absoluteAddress) {
                        // Reset address index for new table context
                        addressIndex = 0
                    } else {
                        addressIndex = previousIndex
                        byte = input.read() // Skip dot
                    }

                    // Read table name
                    while (byte != CLOSE_BRACKET && byte != EOF) {
                        if (byte == DOT) {
                            // End of table part, store and continue
                            address[addressIndex++] = String(buffer, 0, bufferIndex)
                            bufferIndex = 0
                        } else if (byte != SPACE && byte != TAB) {
                            buffer[bufferIndex++] = byte.toByte()
                        }
                        byte = input.read()
                    }

                    // Store last part of table name
                    if (bufferIndex > 0) {
                        address[addressIndex++] = String(buffer, 0, bufferIndex)
                        bufferIndex = 0
                    }

                    if (absoluteAddress) {
                        previousIndex = addressIndex
                    }

                    // Handle array of tables close bracket
                    if (isArrayOfTables) {
                        byte = input.read()
                        if (byte != CLOSE_BRACKET) {
                            throw IllegalArgumentException("Expected close bracket")
                        }
                        api.arrayOfTables(address, addressIndex)
                    } else {
                        api.table(address, addressIndex)
                    }
                    input.read()
                }
                else -> parseKeyValue(input, buffer, address, api, addressIndex, byte)
            }
        }
    }

    private fun parseSpecialNumbers(input: BufferedInputStream, api: Api, address: Array<Any>, addressIndex: Int, keyName: String?): Int {
        var byte = input.read()
        when (byte) {
            X -> {
                // Hexadecimal
                byte = input.read() // Skip 'x'
                var value = 0L

                while (isNotEndOfValue(byte)) {
                    when (byte) {
                        UNDERSCORE -> {
                            // Skip underscores
                        }
                        ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE -> {
                            value = (value shl 4) or (byte - ZERO).toLong()
                        }
                        a, b, c, d, e, f -> value = (value shl 4) or (byte - a + 10).toLong()
                        A, B, C, D, E, F -> value = (value shl 4) or (byte - A + 10).toLong()
                        else -> break
                    }
                    byte = input.read()
                }

                if (keyName == null) {
                    api.appendList(address, addressIndex, value)
                } else {
                    api.appendMap(address, addressIndex, keyName, value)
                }
            }
            O -> {
                // Octal
                byte = input.read() // Skip 'o'
                var value = 0L

                while (isNotEndOfValue(byte)) {
                    when (byte) {
                        UNDERSCORE -> {
                            // Skip underscores
                        }
                        ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN -> {
                            value = (value shl 3) or (byte - ZERO).toLong()
                        }
                        else -> break
                    }
                    byte = input.read()
                }

                if (keyName == null) {
                    api.appendList(address, addressIndex, value)
                } else {
                    api.appendMap(address, addressIndex, keyName, value)
                }
            }
            b -> {
                // Binary
                byte = input.read() // Skip 'b'
                var value = 0L
                while (isNotEndOfValue(byte)) {
                    when (byte) {
                        UNDERSCORE -> {
                            // Skip underscores
                        }
                        ZERO, ONE -> value = (value shl 1) or (byte - ZERO).toLong()
                        else -> break
                    }
                    byte = input.read()
                }
                if (keyName == null) {
                    api.appendList(address, addressIndex, value)
                } else {
                    api.appendMap(address, addressIndex, keyName, value)
                }
            }
            CLOSE_BRACKET, CLOSE_PAREN -> {
                if (keyName == null) {
                    api.appendList(address, addressIndex, 0L)
                } else {
                    api.appendMap(address, addressIndex, keyName, 0L)
                }
                return byte
            }
            else -> {
                return parseRegularNumber(input, address, addressIndex, keyName, false, api, 0)
            }
        }
        return byte
    }

    private fun parseArray(input: BufferedInputStream, buffer: ByteArray, api: Api, address: Array<Any>, parentAddressIndex: Int, keyName: String?) {
        // Create new address array for this scope
        var addressIndex = parentAddressIndex

        // If we have a keyName, add it to the address
        if (keyName != null) {
            address[addressIndex++] = keyName
        }

        // Create a new list with appropriate address
        api.list(address, addressIndex)

        var byte = input.read() // Skip opening bracket

        // Skip whitespace
        while (byte == SPACE || byte == TAB || byte == LINE || byte == RETURN) {
            byte = input.read()
        }

        // Track array index for nested elements
        var arrayIndex = 0

        while (byte != CLOSE_BRACKET && byte != EOF) {

            // Parse array element
            byte = when (byte) {
                DOUBLE_QUOTE -> {
                    // String element
                    byte = input.read() // Skip opening quote
                    var bufferIndex = 0

                    while (byte != DOUBLE_QUOTE && byte != EOF) {
                        // Handle escapes here if needed
                        buffer[bufferIndex++] = byte.toByte()
                        byte = input.read()
                    }

                    api.appendList(address, addressIndex, String(buffer, 0, bufferIndex))
                    input.read() // Skip closing quote
                }
                OPEN_BRACKET -> {
                    // Nested array - We create a new address including the array index
                    address[addressIndex] = arrayIndex
                    parseArray(input, buffer, api, address, addressIndex + 1, null)
                    input.read() // Skip closing bracket
                }
                OPEN_PAREN -> {
                    // Nested inline table - We create a new address including the array index
                    address[addressIndex] = arrayIndex
                    parseInlineTable(input, buffer, api, address, addressIndex + 1)
                    input.read() // Skip closing brace
                }
                t -> { // True
                    parseTrue(input, api, address, addressIndex, null)
                    input.read()
                }
                f -> { // False
                    parseFalse(input, api, address, addressIndex, null)
                    input.read()
                }
                PLUS -> parseRegularNumber(input, address, addressIndex, null, false, api, 0)
                MINUS -> parseRegularNumber(input, address, addressIndex, null, true, api, 0)
                ZERO -> parseSpecialNumbers(input, api, address, addressIndex, null)
                ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE ->
                    parseRegularNumber(input, address, addressIndex, null, false, api, byte - ZERO)
                else -> byte
            }

            // Skip whitespace and commas
            while (byte == SPACE || byte == TAB || byte == LINE || byte == RETURN || byte == COMMA) {
                byte = input.read()
            }

            // Increment array index for next element
            arrayIndex++
        }

        api.listEnd(address, addressIndex)
    }

    private fun parseTrue(input: BufferedInputStream, api: Api, address: Array<Any>, addressIndex: Int, keyName: String?) {
        if (input.read() != r || input.read() != u || input.read() != e) {
            throw IllegalArgumentException("Expected boolean true.")
        }
        if (keyName == null) {
            api.appendList(address, addressIndex, true)
        } else {
            api.appendMap(address, addressIndex, keyName, true)
        }
    }

    private fun parseFalse(input: BufferedInputStream, api: Api, address: Array<Any>, addressIndex: Int, keyName: String?) {
        val first = input.read()
        val second = input.read()
        val third = input.read()
        val fourth = input.read()
        if (first != a || second != l || third != s || fourth != e) {
            throw IllegalArgumentException("Expected boolean false.")
        }
        if (keyName == null) {
            api.appendList(address, addressIndex, false)
        } else {
            api.appendMap(address, addressIndex, keyName, false)
        }
    }

    private fun parseRegularNumber(
        input: BufferedInputStream,
        address: Array<Any>,
        addressIndex: Int,
        keyName: String?,
        isNegative: Boolean,
        api: Api,
        initialDigit: Int
    ): Int {
        var value = initialDigit.toLong()

        // Parse integer part
        var byte = input.read()
        while (isNotEndOfValue(byte) && byte != DOT && byte != e && byte != E) {
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

        var hasDecimal = byte == DOT
        var doubleValue = value.toDouble()
        if (hasDecimal) {
            // Handle decimal part
            var decimalFactor = 0.1
            byte = input.read() // Skip the dot
            while (isNotEndOfValue(byte) && byte != e && byte != E) {
                when (byte) {
                    UNDERSCORE -> {
                        // Skip underscores
                    }
                    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE -> {
                        val digit = byte - ZERO
                        doubleValue += digit * decimalFactor
                        decimalFactor *= 0.1
                    }
                    else -> break
                }
                byte = input.read()
            }
        }

        if (byte == e || byte == E) {
            hasDecimal = true // Force double type for scientific notation
            byte = input.read() // Skip the 'e' or 'E'

            // Check for explicit sign in exponent
            var exponentIsNegative = false
            if (byte == PLUS) {
                byte = input.read() // Skip the '+'
            } else if (byte == MINUS) {
                exponentIsNegative = true
                byte = input.read() // Skip the '-'
            }

            // Parse exponent value
            var exponent = 0
            while (isNotEndOfValue(byte)) {
                when (byte) {
                    UNDERSCORE -> {
                        // Skip underscores
                    }
                    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE -> {
                        val digit = byte - ZERO
                        exponent = exponent * 10 + digit
                    }
                    else -> break
                }
                byte = input.read()
            }

            // Apply exponent
            val power = if (exponentIsNegative) -exponent else exponent
            if (power >= 0) {
                // For positive exponents, multiply
                var factor = 1.0
                for (i in 0 until power) {
                    factor *= 10.0
                }
                doubleValue *= factor
            } else {
                // For negative exponents, divide
                var factor = 1.0
                for (i in 0 until -power) {
                    factor *= 10.0
                }
                doubleValue /= factor
            }
        }
        if (hasDecimal) {
            val finalValue = if (isNegative) -doubleValue else doubleValue
            if (keyName == null) {
                api.appendList(address, addressIndex, finalValue)
            } else {
                api.appendMap(address, addressIndex, keyName, finalValue)
            }
        } else {
            val finalValue = if (isNegative) -value else value
            if (keyName == null) {
                api.appendList(address, addressIndex, finalValue)
            } else {
                api.appendMap(address, addressIndex, keyName, finalValue)
            }
        }
        return byte
    }

    // Helper function to parse inline tables
    private fun parseInlineTable(
        input: BufferedInputStream,
        buffer: ByteArray,
        api: Api,
        address: Array<Any>,
        addressIndex: Int
    ) {
        // Notify API about new inline table
        api.inlineTable(address, addressIndex)

        var byte = input.read() // Skip opening brace

        // Skip initial whitespace
        while (byte == SPACE || byte == TAB) byte = input.read()

        while (byte != CLOSE_PAREN && byte != EOF) {
            // We pass the current address and index to maintain the scope
            byte = parseKeyValue(input, buffer, address, api, addressIndex, byte)
            // We don't update the parentAddressIndex here as we want to maintain scope

            // Skip whitespace and commas
            while (byte == SPACE || byte == TAB || byte == COMMA) {
                byte = input.read()
            }
        }
        // Mark the end of the inline table
        api.mapEnd(address, addressIndex)
    }

    private fun parseKeyValue(
        input: BufferedInputStream,
        buffer: ByteArray,
        address: Array<Any>,
        api: Api,
        parentAddressIndex: Int,
        byteIn: Int
    ): Int {
        // Read key
        var byte = byteIn
        var addressIndex = parentAddressIndex

        var bufferIndex = 0
        while (byte != EQUALS && byte != DOT && byte != EOF && byte != SPACE && byte != TAB) {
            buffer[bufferIndex++] = byte.toByte()
            byte = input.read()
        }

        val keyName = String(buffer, 0, bufferIndex)

        // Skip whitespace
        while (byte == SPACE || byte == TAB) {
            byte = input.read()
        }

        if (byte == DOT) {
            // Handle dotted key (e.g., a.b.c = value)
            // Store current key in address
            address[addressIndex++] = keyName
            byte = input.read()

            // Skip whitespace after dot
            while (byte == SPACE || byte == TAB) {
                byte = input.read()
            }

            // Continue parsing remaining key parts and value
            // We pass our updated address and index
            return parseKeyValue(input, buffer, address, api, addressIndex, byte)
        }

        // Should be at equals sign now
        if (byte != EQUALS) {
            throw IllegalArgumentException("Expected equals sign, found: ${byte.toChar()}")
        }
        byte = input.read()

        // Skip whitespace after equals
        while (byte == SPACE || byte == TAB) {
            byte = input.read()
        }

        // Parse value
        return when (byte) {
            DOUBLE_QUOTE -> {
                // String value
                byte = input.read() // Skip opening quote
                bufferIndex = 0

                while (byte != DOUBLE_QUOTE && byte != EOF) {
                    // Handle escape sequences here if needed
                    buffer[bufferIndex++] = byte.toByte()
                    byte = input.read()
                }

                val stringValue = String(buffer, 0, bufferIndex)
                api.appendMap(address, addressIndex, keyName, stringValue)

                input.read() // Skip closing quote
            }
            OPEN_BRACKET -> {
                // Array - Pass parent address and index
                parseArray(input, buffer, api, address, addressIndex, keyName)
                input.read() // Skip closing bracket
            }
            OPEN_PAREN -> {
                // Inline table - Add the key to the address for the inline table
                address[addressIndex] = keyName
                parseInlineTable(input, buffer, api, address, addressIndex + 1)
                input.read() // Skip closing brace
            }
            t -> { // True
                parseTrue(input, api, address, addressIndex, keyName)
                input.read()
            }
            f -> { // False
                parseFalse(input, api, address, addressIndex, keyName)
                input.read()
            }
            PLUS -> parseRegularNumber(input, address, addressIndex, keyName, false, api, 0)
            MINUS -> parseRegularNumber(input, address, addressIndex, keyName, true, api, 0)
            ZERO -> parseSpecialNumbers(input, api, address, addressIndex, keyName)
            ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE ->
                parseRegularNumber(input, address, addressIndex, keyName, false, api, byte - ZERO)
            else -> byte
        }
    }

    companion object {

        private fun isNotEndOfValue(byte: Int) = byte != EOF && byte != LINE && byte != SPACE && byte != TAB && byte != COMMA && byte != CLOSE_BRACKET && byte != CLOSE_PAREN

        private const val SPACE = ' '.code
        private const val TAB = '\t'.code
        private const val LINE = '\n'.code
        private const val HASH = '#'.code
        private const val RETURN = '\r'.code
        private const val OPEN_BRACKET = '['.code
        private const val CLOSE_BRACKET = ']'.code
        private const val OPEN_PAREN = '{'.code
        private const val CLOSE_PAREN = '}'.code
        private const val DOT = '.'.code
        private const val DOUBLE_QUOTE = '"'.code
        private const val EQUALS = '='.code
        private const val EOF = -1
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
        private const val PLUS = '+'.code
        private const val MINUS = '-'.code
        private const val t = 't'.code
        private const val r = 'r'.code
        private const val u = 'u'.code
        private const val a = 'a'.code
        private const val b = 'b'.code
        private const val c = 'c'.code
        private const val d = 'd'.code
        private const val e = 'e'.code
        private const val f = 'f'.code
        private const val A = 'A'.code
        private const val B = 'B'.code
        private const val C = 'C'.code
        private const val D = 'D'.code
        private const val E = 'E'.code
        private const val F = 'F'.code
        private const val L = 'L'.code
        private const val l = 'l'.code
        private const val S = 'S'.code
        private const val s = 's'.code
        private const val X = 'x'.code
        private const val O = 'o'.code
        private const val UNDERSCORE = '_'.code
        private const val COMMA = ','.code
    }
}
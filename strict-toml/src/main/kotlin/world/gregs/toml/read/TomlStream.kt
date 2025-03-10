package world.gregs.toml.read

import java.io.BufferedInputStream

class TomlStream {

    interface API {
        fun table(address: Array<String>, addressSize: Int)
        fun inlineTable(address: Array<String>, addressSize: Int)
        fun appendMap(address: Array<String>, addressSize: Int, key: String, value: Double)
        fun appendMap(address: Array<String>, addressSize: Int, key: String, value: Long)
        fun appendMap(address: Array<String>, addressSize: Int, key: String, value: String)
        fun appendMap(address: Array<String>, addressSize: Int, key: String, value: Boolean)
        fun appendMap(address: Array<String>, addressSize: Int, key: String, value: List<Any>)
        fun appendMap(address: Array<String>, addressSize: Int, key: String, value: Map<String, Any>)
        fun mapEnd(address: Array<String>, addressSize: Int)

        fun list(address: Array<String>, addressSize: Int)
        fun appendList(address: Array<String>, addressSize: Int, value: Double)
        fun appendList(address: Array<String>, addressSize: Int, value: Long)
        fun appendList(address: Array<String>, addressSize: Int, value: String)
        fun appendList(address: Array<String>, addressSize: Int, value: Boolean)
        fun appendList(address: Array<String>, addressSize: Int, value: List<Any>)
        fun appendList(address: Array<String>, addressSize: Int, value: Map<String, Any>)
        fun listEnd(address: Array<String>, addressSize: Int)
    }

    /**
     *  TODO
     *      fix empty address for nested inline tables
     *      remove address.copying(), replace with addressIndex-- once out of a scope
     *      remove keyName checks with an array and table version of each method?
     */
    fun read(input: BufferedInputStream, api: API) {
        val buffer = ByteArray(1024)
        var bufferIndex = 0
        val address = Array(10) { "" }
        var addressIndex = 0
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

                    // Reset address index for new table context
                    for (i in 0 until addressIndex) {
                        address[i] = ""
                    }
                    addressIndex = 0

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

                    // Handle array of tables close bracket
                    if (isArrayOfTables) {
                        byte = input.read()
                        if (byte != CLOSE_BRACKET) {
                            throw IllegalArgumentException("Expected close bracket")
                        }
                    }

                    // Notify API about new table
                    api.table(address, addressIndex)
                    input.read()
                }
                else -> parseKeyValue(input, buffer, address, api, addressIndex, byte)
            }
        }
    }

    private fun parseSpecialNumbers(input: BufferedInputStream, api: API, address: Array<String>, addressIndex: Int, keyName: String?): Int {
        var byte = input.read()
        when (byte) {
            X -> {
                // Hexadecimal
                byte = input.read() // Skip 'x'
                var value = 0L

                while (byte != EOF && byte != LINE && byte != SPACE && byte != TAB && byte != COMMA && byte != CLOSE_BRACKET && byte != CLOSE_PAREN) {
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

                while (byte != EOF && byte != LINE && byte != SPACE && byte != TAB && byte != COMMA && byte != CLOSE_BRACKET && byte != CLOSE_PAREN) {
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
                while (byte != EOF && byte != LINE && byte != SPACE && byte != TAB && byte != COMMA && byte != CLOSE_BRACKET && byte != CLOSE_PAREN) {
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

    private fun parseArray(input: BufferedInputStream, buffer: ByteArray, api: API, address: Array<String>, parentAddressIndex: Int, keyName: String?) {
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
                    address[addressIndex] = arrayIndex.toString()
                    parseArray(input, buffer, api, address, addressIndex + 1, null)
                    input.read() // Skip closing bracket
                }
                OPEN_PAREN -> {
                    // Nested inline table - We create a new address including the array index
                    address[addressIndex] = arrayIndex.toString()
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

    private fun parseTrue(input: BufferedInputStream, api: API, address: Array<String>, addressIndex: Int, keyName: String?) {
        if (input.read() != r || input.read() != u || input.read() != e) {
            throw IllegalArgumentException("Expected boolean true.")
        }
        if (keyName == null) {
            api.appendList(address, addressIndex, true)
        } else {
            api.appendMap(address, addressIndex, keyName, true)
        }
    }

    private fun parseFalse(input: BufferedInputStream, api: API, address: Array<String>, addressIndex: Int, keyName: String?) {
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
        address: Array<String>,
        addressIndex: Int,
        keyName: String?,
        isNegative: Boolean,
        api: API,
        initialDigit: Int
    ): Int {
        var value = initialDigit.toLong()

        // Parse integer part
        var byte = input.read()
        while (byte != EOF && byte != LINE && byte != SPACE && byte != TAB &&
            byte != DOT && byte != COMMA && byte != CLOSE_BRACKET && byte != CLOSE_PAREN
        ) {
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

        // TODO support scientific notation with a third section for e?
        if (byte == DOT) {
            // Handle decimal part
            var doubleValue = value.toDouble()
            var decimalFactor = 0.1
            byte = input.read() // Skip the dot
            while (byte != EOF && byte != LINE && byte != SPACE && byte != TAB &&
                byte != COMMA && byte != CLOSE_BRACKET && byte != CLOSE_PAREN
            ) {
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
        api: API,
        address: Array<String>,
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
        address: Array<String>,
        api: API,
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
        const val SPACE = ' '.code
        const val TAB = '\t'.code
        const val LINE = '\n'.code
        const val HASH = '#'.code
        const val RETURN = '\r'.code
        const val OPEN_BRACKET = '['.code
        const val CLOSE_BRACKET = ']'.code
        const val OPEN_PAREN = '{'.code
        const val CLOSE_PAREN = '}'.code
        const val DOT = '.'.code
        const val DOUBLE_QUOTE = '"'.code
        const val EQUALS = '='.code
        const val EOF = -1
        const val ZERO = '0'.code
        const val ONE = '1'.code
        const val TWO = '2'.code
        const val THREE = '3'.code
        const val FOUR = '4'.code
        const val FIVE = '5'.code
        const val SIX = '6'.code
        const val SEVEN = '7'.code
        const val EIGHT = '8'.code
        const val NINE = '9'.code
        const val PLUS = '+'.code
        const val MINUS = '-'.code
        const val t = 't'.code
        const val r = 'r'.code
        const val u = 'u'.code
        const val a = 'a'.code
        const val b = 'b'.code
        const val c = 'c'.code
        const val d = 'd'.code
        const val e = 'e'.code
        const val f = 'f'.code
        const val A = 'A'.code
        const val B = 'B'.code
        const val C = 'C'.code
        const val D = 'D'.code
        const val E = 'E'.code
        const val F = 'F'.code
        const val L = 'L'.code
        const val l = 'l'.code
        const val S = 'S'.code
        const val s = 's'.code
        const val X = 'x'.code
        const val O = 'o'.code
        const val UNDERSCORE = '_'.code
        const val COMMA = ','.code
    }
}
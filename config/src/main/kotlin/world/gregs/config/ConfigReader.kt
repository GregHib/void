package world.gregs.config

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.*
import java.io.Closeable
import java.io.InputStream
import java.math.BigDecimal
import java.math.MathContext

class ConfigReader(
    private val input: InputStream,
    private val stringBuffer: ByteArray = ByteArray(100), // Maximum string length
    private val debug: String = "",
) : Closeable {
    private var byte: Int = input.read()
    private var lastSection = ""
    private var line = 1

    val peek: Char
        get() = byte.toChar()

    constructor(input: InputStream, maxStringLength: Int, debug: String = "") : this(input, ByteArray(maxStringLength), debug)

    init {
        nextLine()
    }

    /**
     * Check if there are anymore sections remaining
     */
    fun nextSection(): Boolean = when (byte) {
        EOF -> false
        else -> true
    }

    /**
     * Read the section title
     */
    fun section(): String {
        if (byte != OPEN_BRACKET) {
            return ""
        }
        byte = input.read() // Skip [
        val inherit = byte == DOT
        var bufferIndex = 0
        while (byte != EOF && byte != CLOSE_BRACKET) {
            stringBuffer[bufferIndex++] = byte.toByte()
            byte = input.read()
        }
        byte = input.read() // Skip ]
        require(byte != CLOSE_BRACKET) { "Array of tables are not supported. ${exception()}" }
        val section = if (inherit) {
            "${lastSection}${String(stringBuffer, 0, bufferIndex)}"
        } else {
            val section = String(stringBuffer, 0, bufferIndex)
            lastSection = section
            section
        }
        nextLine()
        return section
    }

    /**
     * Read all sections into a map, not as performant as reading individually.
     */
    fun sections(expectedSections: Int = 8, expectedSize: Int = 8, loadFactor: Float = Hash.VERY_FAST_LOAD_FACTOR): Map<String, Map<String, Any>> {
        val sections = Object2ObjectOpenHashMap<String, MutableMap<String, Any>>(expectedSections, loadFactor)
        while (nextSection()) {
            val section = section()
            val map = sections.getOrPut(section) { Object2ObjectOpenHashMap(expectedSize, loadFactor) }
            while (nextPair()) {
                map[key()] = value()
            }
        }
        return sections
    }

    /**
     * Check if there are anymore key-value pairs remaining for the current section
     */
    fun nextPair(): Boolean = byte != EOF && byte != OPEN_BRACKET

    fun key(): String {
        val key = when (byte) {
            DOUBLE_QUOTE -> quotedString()
            SINGLE_QUOTE -> literalString()
            else -> bareKey()
        }
        skipEquals()
        return key
    }

    private fun skipEquals() {
        var found = false
        while (byte == SPACE || byte == TAB || byte == EQUALS) {
            found = found || byte == EQUALS
            byte = input.read()
        }
        require(found) { "Expected equals after key. ${exception()}" }
    }

    /**
     * Read a quoted string value
     */
    fun string(): String {
        val string = when (byte) {
            DOUBLE_QUOTE -> quotedString()
            SINGLE_QUOTE -> literalString()
            else -> throw IllegalArgumentException("Strings must be quoted. ${exception()}")
        }
        nextLine()
        return string
    }

    private fun quotedString(): String {
        var bufferIndex = 0
        byte = input.read() // skip opening quote
        while (byte != EOF && byte != DOUBLE_QUOTE) {
            if (byte == BACKSLASH) {
                byte = input.read()
                if (byte == DOUBLE_QUOTE) {
                    stringBuffer[bufferIndex++] = DOUBLE_QUOTE.toByte()
                    byte = input.read()
                    continue
                } else {
                    stringBuffer[bufferIndex++] = BACKSLASH.toByte()
                }
            }

            stringBuffer[bufferIndex++] = byte.toByte()
            byte = input.read()
        }
        byte = input.read() // Skip closing quote
        return String(stringBuffer, 0, bufferIndex)
    }

    private fun literalString(): String {
        var bufferIndex = 0
        byte = input.read() // skip opening quote
        while (byte != EOF && byte != SINGLE_QUOTE) {
            stringBuffer[bufferIndex++] = byte.toByte()
            byte = input.read()
        }
        require(byte == SINGLE_QUOTE) { "Strings must be quoted" }
        byte = input.read() // Skip closing quote
        return String(stringBuffer, 0, bufferIndex)
    }

    private fun bareKey(): String {
        var bufferIndex = 0
        while (byte != EOF && byte != SPACE && byte != TAB && byte != EQUALS && byte != RETURN && byte != NEWLINE) {
            stringBuffer[bufferIndex++] = byte.toByte()
            byte = input.read()
        }
        require(bufferIndex > 0) { "No key found. ${exception()}" }
        return String(stringBuffer, 0, bufferIndex)
    }

    /**
     * Read a Double or Long number
     */
    fun number(): Number {
        val number: Number = when (byte) {
            MINUS -> {
                val value = readLong(0)
                if (byte == DOT) -readDecimal(value) else -value
            }
            PLUS -> {
                val value = readLong(0)
                if (byte == DOT) readDecimal(value) else value
            }
            else -> {
                val value = readLong(byte - ZERO.toLong())
                if (byte == DOT) readDecimal(value) else value
            }
        }
        nextLine()
        return number
    }

    /**
     * Read an Int value
     */
    fun int(): Int {
        val int = when (byte) {
            MINUS -> -readInt(0)
            PLUS -> readInt(0)
            else -> readInt(byte - ZERO)
        }
        nextLine()
        return int
    }

    private fun readInt(int: Int): Int {
        var value = int
        byte = input.read()
        while (isDigit() || byte == UNDERSCORE) {
            if (byte != UNDERSCORE) {
                val digit = byte - ZERO
                value = value * 10 + digit
            }
            byte = input.read()
        }
        return value
    }

    /**
     * Read a Long value
     */
    fun long(): Long {
        val long = when (byte) {
            MINUS -> -readLong(0)
            PLUS -> readLong(0)
            else -> readLong(byte - ZERO.toLong())
        }
        nextLine()
        return long
    }

    private fun readLong(long: Long): Long {
        var value = long
        byte = input.read()
        while (isDigit() || byte == UNDERSCORE) {
            if (byte != UNDERSCORE) {
                val digit = byte - ZERO
                value = value * 10 + digit
            }
            byte = input.read()
        }
        return value
    }

    /**
     * Read a Double value
     */
    fun double(): Double {
        val double = when (byte) {
            MINUS -> {
                val value = readLong(0)
                require(byte == DOT) { "Expecting decimal point. ${exception()}" }
                -readDecimal(value)
            }
            PLUS -> {
                val value = readLong(0)
                require(byte == DOT) { "Expecting decimal point. ${exception()}" }
                readDecimal(value)
            }
            else -> {
                val value = readLong(byte - ZERO.toLong())
                require(byte == DOT) { "Expecting decimal point. ${exception()}" }
                readDecimal(value)
            }
        }
        nextLine()
        return double
    }

    private fun readDecimal(long: Long): Double {
        var double = long.toDouble()
        byte = input.read() // Skip decimal point
        var decimalFactor = 1.0
        require(isDigit()) { "Expecting a digit after decimal point. ${exception()}" }
        while (isDigit() || byte == UNDERSCORE) {
            if (byte != UNDERSCORE) {
                val digit = byte - ZERO
                decimalFactor /= 10
                double += digit * decimalFactor
            }
            byte = input.read()
        }
//        if (byte == 'E'.code || byte == 'e'.code) {
//            double = readExponent(double)
//        }
        return double
    }

    private fun readExponent(double: Double): Double {
        byte = input.read() // Skip 'e'
        val sign = when (byte) {
            MINUS -> {
                byte = input.read() // Skip sign
                require(isDigit()) { "Expecting digits after exponent sign. ${exception()}" }
                -1
            }
            PLUS -> {
                byte = input.read() // Skip sign
                require(isDigit()) { "Expecting digits after exponent sign. ${exception()}" }
                1
            }
            ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE -> 1
            else -> throw IllegalArgumentException("Expecting a digit or sign after 'E'. ${exception()}")
        }

        // Read the exponent
        var exponent = 0
        while (isDigit() || byte == UNDERSCORE) {
            if (byte != UNDERSCORE) {
                exponent = exponent * 10 + (byte - ZERO)
            }
            byte = input.read()
        }
        val base = BigDecimal(double, MathContext.DECIMAL64)
        val scaleFactor = BigDecimal.TEN.pow(sign * exponent, MathContext.DECIMAL64)
        return base.multiply(scaleFactor).toDouble()
    }

    private fun isDigit() = byte == ZERO || byte == ONE || byte == TWO || byte == THREE || byte == FOUR || byte == FIVE || byte == SIX || byte == SEVEN || byte == EIGHT || byte == NINE

    /**
     * Read a case-sensitive boolean value
     */
    fun boolean(): Boolean {
        val boolean = when (byte) {
            T -> booleanTrue()
            F -> booleanFalse()
            else -> throw IllegalArgumentException("Expecting boolean. ${exception()}")
        }
        nextLine()
        return boolean
    }

    private fun booleanFalse(): Boolean {
        if (input.read() == A && input.read() == L && input.read() == S && input.read() == E) {
            byte = input.read()
            return false
        }
        throw IllegalArgumentException("Expecting boolean 'false'. ${exception()}")
    }

    private fun booleanTrue(): Boolean {
        if (input.read() == R && input.read() == U && input.read() == E) {
            byte = input.read()
            return true
        }
        throw IllegalArgumentException("Expecting boolean 'true'. ${exception()}")
    }

    /**
     * Check if there are anymore list elements remaining
     */
    fun nextElement(): Boolean = when (byte) {
        OPEN_BRACKET, COMMA -> {
            byte = input.read() // Skip [ or ,
            nextLine()
            when (byte) {
                EOF -> throw IllegalArgumentException("Expecting list closing bracket. ${exception()}")
                CLOSE_BRACKET -> {
                    byte = input.read() // Skip ]
                    nextLine()
                    false
                }
                else -> true
            }
        }
        CLOSE_BRACKET -> {
            byte = input.read() // Skip ]
            nextLine()
            false
        }
        else -> false
    }

    /**
     * Check if there are any more map entries remaining
     */
    fun nextEntry() = when (byte) {
        OPEN_BRACE, COMMA -> {
            byte = input.read() // Skip { or ,
            nextLine()
            when (byte) {
                EOF -> throw IllegalArgumentException("Expecting map closing brace. ${exception()}")
                CLOSE_BRACE -> {
                    byte = input.read() // Skip }
                    nextLine()
                    false
                }
                else -> true
            }
        }
        CLOSE_BRACE -> {
            byte = input.read() // Skip }
            nextLine()
            false
        }
        else -> false
    }

    /**
     * Read a mixed type list, if the types are known you should call [nextElement] with the relevant method(s) directly for better performance.
     */
    fun list(expectedSize: Int = 2): List<Any> {
        require(byte == OPEN_BRACKET) { "Lists must start with an opening bracket. ${exception()}" }
        val list = ObjectArrayList<Any>(expectedSize)
        while (nextElement()) {
            list.add(value())
        }
        return list
    }

    /**
     * Read a mixed type map, if the types are known you should call [nextEntry] with the relevant method directly for better performance.
     */
    fun map(expectedSize: Int = 8, loadFactor: Float = Hash.VERY_FAST_LOAD_FACTOR): Map<String, Any> {
        require(byte == OPEN_BRACE) { "Maps must start with an opening brace. ${exception()}" }
        val map = Object2ObjectOpenHashMap<String, Any>(expectedSize, loadFactor)
        while (nextEntry()) {
            map[key()] = value()
        }
        return map
    }

    /**
     * Read a generic value, if the type is known you should call the relevant method directly for better performance.
     */
    fun value(): Any {
        val value: Any = when (byte) {
            DOUBLE_QUOTE -> quotedString()
            SINGLE_QUOTE -> literalString()
            OPEN_BRACKET -> list()
            OPEN_BRACE -> map()
            T -> booleanTrue()
            F -> booleanFalse()
            MINUS -> {
                val value = readLong(0)
                if (byte == DOT) {
                    -readDecimal(value)
                } else if (value > Int.MIN_VALUE && value < Int.MAX_VALUE) {
                    -value.toInt()
                } else {
                    -value
                }
            }
            PLUS -> {
                val value = readLong(0)
                if (byte == DOT) {
                    readDecimal(value)
                } else if (value > Int.MIN_VALUE && value < Int.MAX_VALUE) {
                    value.toInt()
                } else {
                    value
                }
            }
            ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE -> {
                val value = readLong(byte - ZERO.toLong())
                if (byte == DOT) {
                    readDecimal(value)
                } else if (value > Int.MIN_VALUE && value < Int.MAX_VALUE) {
                    value.toInt()
                } else {
                    value
                }
            }
            else -> throw IllegalArgumentException("Unexpected character. ${exception()}")
        }
        nextLine()
        return value
    }

    private fun nextLine() {
        // Skip whitespace, new lines and comments
        while (byte == SPACE || byte == TAB || byte == RETURN || byte == NEWLINE || byte == HASH) {
            when (byte) {
                HASH -> {
                    while (byte != EOF && byte != RETURN && byte != NEWLINE) {
                        byte = input.read()
                    }
                }
                NEWLINE -> {
                    line++
                    byte = input.read()
                }
                else -> byte = input.read()
            }
        }
    }

    override fun close() {
        input.close()
    }

    fun exception(): String = "line=$line char='${charType(byte)}'${if (debug.isBlank()) "" else ", in=$debug"}"

    companion object {

        private fun charType(byte: Int): String = when (byte) {
            NEWLINE -> "\\n"
            RETURN -> "\\r"
            TAB -> "\\t"
            BACKSLASH -> "\\"
            EOF -> "<end-of-file>"
            else -> byte.toChar().toString()
        }

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

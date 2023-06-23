package world.gregs.voidps.engine.data

interface YamlParserI {
    var mapModifier: (key: String, value: Any) -> Any
    var listModifier: (value: Any) -> Any

    fun parseVal(indentOffset: Int = 0, withinMap: Boolean = false): Any

    var input: CharArray
    var size: Int
    var index: Int
    var indentation: Int
    var lineCount: Int

    val char: Char
        get() = input[index]

    val next: Char
        get() = input[index + 1]


    fun isFalse(char: Char) = char == 'f' && index + 4 < size && input[index + 1] == 'a' && input[index + 2] == 'l' && input[index + 3] == 's' && input[index + 4] == 'e'

    fun isTrue(char: Char) = char == 't' && index + 3 < size && input[index + 1] == 'r' && input[index + 2] == 'u' && input[index + 3] == 'e'

    fun isNumber(char: Char) = char == '0' || char == '1' || char == '2' || char == '3' || char == '4' || char == '5' || char == '6' || char == '7' || char == '8' || char == '9'

    fun isTerminator(char: Char) = linebreak(char) || char == '#'

    fun isOpeningTerminator(char: Char) = linebreak(char) || char == '#' || char == '{' || char == '['

    fun isLineEnd() = isOpeningTerminator(input[index])

    fun nextCharEmpty() = index + 1 < size && input[index + 1] == ' '

    fun isListItem() = input[index] == '-' && nextCharEmpty()

    fun number(decimal: Boolean, start: Int, end: Int): Any {
        val string = substring(start, end)
        return if (decimal) {
            string.toDouble()
        } else {
            val long = string.toLong()
            if (long <= Int.MAX_VALUE) long.toInt() else long
        }
    }

    fun parseQuote(): String {
        index++ // skip opening quote
        val start = index
        while (index < size) {
            if (input[index] == '"') {
                break
            }
            index++
        }
        return substring(start, index++) // skip closing quote
    }

    val outBounds: Boolean
        get() = index >= size

    val inBounds: Boolean
        get() = index < size

    val charInLine: Int

    val line: String

    fun debug(length: Int): String

    val debug: String

    fun nextLine()

    fun set(charArray: CharArray, size: Int = charArray.size)

    fun substring(start: Int, end: Int): String

    fun skipSpaces()

    fun linebreak(char: Char): Boolean

    fun parse(charArray: CharArray, length: Int = charArray.size): Any
}
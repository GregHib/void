package world.gregs.voidps.engine.data

interface CharReader {
    var mapModifier: (key: String, value: Any) -> Any
    var listModifier: (value: Any) -> Any

    fun parseExplicitList(): List<Any>

    fun parseExplicitMap(): Map<String, Any>

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
}
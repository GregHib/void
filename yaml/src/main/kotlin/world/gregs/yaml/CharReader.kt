package world.gregs.yaml

/**
 * Reads a string of characters one at a time
 */
class CharReader(
    val anchors: MutableMap<String, Any>,
) {
    private lateinit var input: CharArray
    var size = 0
    var index = 0
        private set
    var indentation = 0
    private var lineCount = 0
    private var lastLine = 0

    val char: Char
        get() = input[index]

    val peekNext: Char
        get() = input[index + 1]

    val outBounds: Boolean
        get() = index >= size

    val inBounds: Boolean
        get() = index < size

    private val charInLine: Int
        get() = index - lastLine

    fun next(): Char = input[++index]

    fun inBounds(offset: Int) = this.index + offset < size

    fun nextCharEmpty() = index + 1 < size && input[index + 1] == ' '

    fun skip() {
        index++
    }

    fun skip(count: Int) {
        index += count
    }

    fun set(charArray: CharArray, size: Int) {
        this.input = charArray
        this.size = size
        index = 0
        indentation = 0
        lastLine = 0
        lineCount = 1
    }

    fun substring(start: Int, end: Int): String = String(input, start, end - start)

    /**
     * Skips over spaces, line breaks and comments to reach the next line
     * Records the start of the [lastLine] and the [indentation] of current line
     */
    fun nextLine() {
        var tabs = 0
        while (index < size) {
            when (input[index]) {
                ' ' -> {}
                '\t' -> {
                    tabs++
                }
                '\n', '\r' -> {
                    if (lastLine < index - 1) {
                        lineCount++
                    }
                    lastLine = index + 1
                    tabs = 0
                }
                '#' -> while (index < size) {
                    if (linebreak(input[index])) {
                        lastLine = index
                        break
                    }
                    index++
                }
                else -> {
                    indentation = ((index - lastLine) / 2) + tabs
                    break
                }
            }
            index++
        }
    }

    fun skipSpaces() {
        while (index < size && input[index] == ' ') {
            index++
        }
    }

    /**
     * Parses a string as a number
     */
    fun number(decimal: Boolean, start: Int, end: Int): Any {
        val string = substring(start, end)
        return if (decimal) {
            string.toDouble()
        } else if (end - start < 10) {
            string.toInt()
        } else {
            val long = string.toLong()
            if (long <= Int.MAX_VALUE) long.toInt() else long
        }
    }

    fun debug(length: Int) = substring(index, (index + length).coerceAtMost(size)).replace("\n", "\\n").replace("\r", "\\r")

    val debug: String
        get() = debug(20)

    val line: String
        get() {
            var end = lastLine
            while (end < size) {
                val char = input[end]
                if (linebreak(char)) {
                    break
                }
                end++
            }
            return substring(lastLine, end).replace("\n", "\\n").replace("\r", "\\r").trim()
        }

    val exception: String
        get() = "line=$lineCount char=$charInLine '$line'"

    companion object {
        private fun linebreak(char: Char) = char == '\r' || char == '\n'
    }
}

package world.gregs.voidps.engine.data.yaml

class CharReader {
    private var input = CharArray(0)
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

    fun next(): Char {
        return input[++index]
    }

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

    fun substring(start: Int, end: Int): String {
        if (end == -1) {
            throw IndexOutOfBoundsException("")
        }
        return String(input, start, end - start)
    }

    fun nextLine() {
        while (index < size) {
            when (input[index]) {
                ' ' -> {}
                '\n', '\r' -> {
                    lastLine = index + 1
                    lineCount++
                }
                '#' -> while (index < size) {
                    if (linebreak(input[index])) {
                        lastLine = index
                        break
                    }
                    index++
                }
                else -> {
                    indentation = (index - lastLine) / 2
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

    fun skipAnchorString() {
        while (inBounds) {
            val char = char
            if (char == ' ' || linebreak(char)) {
                break
            }
            skip()
        }
    }

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

    fun debug(length: Int) = substring(index, (index + length).coerceAtMost(size)).replace("\n", "\\n")

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
            return substring(lastLine, end).replace("\n", "\\n").trim()
        }

    val exception: String
        get() = "line=$lineCount char=$charInLine '$line'"

    companion object {
        private fun linebreak(char: Char) = char == '\r' || char == '\n'
    }
}
package world.gregs.voidps.engine.data

class CharArrayReader {
    var input = CharArray(0)
    var size = 0
    var index = 0
        private set
    var indentation = 0
    var lineCount = 0
    private var lastLine = 0

    val char: Char
        get() = input[index]

    val next: Char
        get() = input[index + 1]

    val outBounds: Boolean
        get() = index >= size

    val inBounds: Boolean
        get() = index < size

    val end: Boolean
        get() = index == size

    val charInLine: Int
        get() = index - lastLine

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

    fun skip() {
        index++
    }

    fun skip(count: Int) {
        index += count
    }


    val exception: String
        get() = "line=$lineCount char=$charInLine '$line'"

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

    fun skipAnchorString() {
        while (inBounds) {
            val char = char
            if (char == ' ') {
                break
            }
            if (linebreak(char)) {
                break
            }
            skip()
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

    fun debug(length: Int) = substring(index, (index + length).coerceAtMost(size)).replace("\n", "\\n")

    val debug: String
        get() = debug(20)

    fun nextLine() {
        while (index < size) {
            when (input[index]) {
                ' ' -> {}
                '#' -> while (index < size) {
                    if (linebreak(input[index])) {
                        lastLine = index
                        break
                    }
                    index++
                }
                '\n', '\r' -> {
                    lastLine = index + 1
                    lineCount++
                }
                else -> {
                    indentation = (index - lastLine) / 2
                    break
                }
            }
            index++
        }
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

    fun skipSpaces() {
        while (index < size && input[index] == ' ') {
            index++
        }
    }

    fun linebreak(char: Char) = char == '\r' || char == '\n'
}
package world.gregs.voidps.engine.data

open class CharArrayReader {
    var input = CharArray(0)
    var size = 0
    var index = 0
    var indentation = 0
    var lineCount = 0
    private var lastLine = 0

    val char: Int
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

    fun set(charArray: CharArray, size: Int = charArray.size) {
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
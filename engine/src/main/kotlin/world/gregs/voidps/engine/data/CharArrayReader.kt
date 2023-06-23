package world.gregs.voidps.engine.data

abstract class CharArrayReader : YamlParserI {
    override var input = CharArray(0)
    override var size = 0
    override var index = 0
    override var indentation = 0
    override var lineCount = 0
    private var lastLine = 0

    override val char: Char
        get() = input[index]

    override val next: Char
        get() = input[index + 1]

    override val outBounds: Boolean
        get() = index >= size

    override val inBounds: Boolean
        get() = index < size

    override val charInLine: Int
        get() = index - lastLine

    override val line: String
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
    override fun debug(length: Int) = substring(index, (index + length).coerceAtMost(size)).replace("\n", "\\n")

    override val debug: String
        get() = debug(20)

    override fun nextLine() {
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

    override fun set(charArray: CharArray, size: Int) {
        this.input = charArray
        this.size = size
        index = 0
        indentation = 0
        lastLine = 0
        lineCount = 1
    }

    override fun substring(start: Int, end: Int): String {
        if (end == -1) {
            throw IndexOutOfBoundsException("")
        }
        return String(input, start, end - start)
    }

    override fun skipSpaces() {
        while (index < size && input[index] == ' ') {
            index++
        }
    }

    override fun linebreak(char: Char) = char == '\r' || char == '\n'
}
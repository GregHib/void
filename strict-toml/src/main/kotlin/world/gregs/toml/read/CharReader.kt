package world.gregs.toml.read

class CharReader {

    private var input: CharArray = CharArray(0)
    var size = 0
        private set
    var index = 0
        private set
    private var lineCount = 1
    private var lastLine = 0

    val char: Char
        get() = input[index]

    val peek: Char
        get() = input[index + 1]

    val inBounds: Boolean
        get() = index < size

    fun peek(offset: Int): Char = input[index + offset]

    fun inBounds(amount: Int): Boolean {
        return index + amount <= size
    }

    fun matches(vararg chars: Char): Boolean {
        if (!inBounds(chars.size)) {
            return false
        }
        for (i in chars.indices) {
            if (input[index + i] != chars[i]) {
                return false
            }
        }
        return true
    }

    fun set(chars: CharArray, size: Int) {
        this.lineCount = 1
        this.lastLine = 0
        this.index = 0
        input = chars
        this.size = size
    }

    fun skip(count: Int) {
        index += count
    }

    fun skipSpaces() {
        while (inBounds && space(input[index])) {
            index++
        }
    }

    fun skipSpacesComment() {
        while (inBounds && space(input[index])) {
            index++
        }
        if (inBounds && input[index] == '#') {
            while (inBounds && !linebreak(input[index])) {
                index++
            }
        }
    }

    fun substring(start: Int, end: Int = index): String {
        return String(input, start, end - start)
    }

   fun nextLine() {
        while (inBounds) {
            when (input[index]) {
                ' ', '\t' -> {}
                '\r', '\n' -> {
                    markLine()
                    continue
                }
                '#' -> while (inBounds) {
                    if (linebreak(input[index])) {
                        lastLine = index
                        break
                    }
                    index++
                }
                else -> break
            }
            index++
        }
    }

    fun markLine() {
        while (inBounds) {
            if (!linebreak(char)) {
                break
            }
            index++
        }
        if (lastLine < index - 1) {
            lineCount++
        }
        lastLine = index + 1
    }

    fun expect(c: Char) {
        if (!inBounds || char != c) {
            throw IllegalArgumentException("Expected character '$c' at $exception")
        }
        index++
    }

    fun expectLineBreak() {
        if (!inBounds || !linebreak(char)) {
            throw IllegalArgumentException("Expected newline at $exception")
        }
        markLine()
    }

    val debug: String
        get() = debug(20)

    fun debug(length: Int) = substring(index, (index + length).coerceAtMost(size)).replace("\n", "\\n").replace("\r", "\\r")

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
            if (lastLine == end) {
                return ""
            }
            return substring(lastLine, end).replace("\n", "\\n").replace("\r", "\\r").trim()
        }

    val exception: String
        get() = "line=$lineCount char=${index - lastLine} '$line'"

    companion object {
        private fun linebreak(char: Char) = char == '\r' || char == '\n'
        private fun space(char: Char) = char == ' ' || char == '\t'
    }
}
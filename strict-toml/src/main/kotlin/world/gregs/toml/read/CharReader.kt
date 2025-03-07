package world.gregs.toml.read

class CharReader {

    private var input: CharArray = CharArray(0)
    var size = 0
        private set
    var index = 0
        private set

    val char: Char
        get() = input[index]

    val peek: Char
        get() = input[index + 1]

    inline val inBounds: Boolean
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

    val exception: String
        get() {
            var lineCount = 0
            var lastLine = 0
            for (index in 0 until index) {
                if (input[index] == '\n') {
                    lastLine = index
                    lineCount++
                }
            }

            var end = index
            for (index in lastLine + 1 until index) {
                if (input[index] == '\n') {
                    end = index
                }
            }
            if (lastLine == end) {
                return ""
            }
            val line = substring(lastLine, end).replace("\n", "\\n").replace("\r", "\\r").trim()
            return "line=$lineCount char=${index - lastLine} '$line'"
        }

    companion object {
        private fun linebreak(char: Char) = char == '\r' || char == '\n'
        private fun space(char: Char) = char == ' ' || char == '\t'
    }
}
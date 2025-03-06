package world.gregs.toml.read

import kotlin.math.pow

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

    fun set(chars: CharArray, size: Int) {
        this.lineCount = 1
        this.lastLine = 0
        this.index = 0
        input = chars
        this.size = size
    }

    fun skipSpaces() {
        while (index < size && (input[index] == ' ' || input[index] == '\t')) {
            index++
        }
    }

    fun skipSpacesComment() {
        while (index < size && input[index] == ' ') {
            index++
        }
        if (inBounds && input[index] == '#') {
            while (index < size && input[index] != '\n' && input[index] != '\r') {
                index++
            }
        }
    }

    fun substring(start: Int, end: Int = index): String {
        return String(input, start, end - start)
    }

    fun number(decimal: Boolean, negative: Boolean, power: Int, string: String): Number {
        return if (negative) {
            if (decimal) {
                -string.toDouble()
            } else if (string.length < 10) {
                if (power == 0) {
                    -string.toInt()
                } else {
                    -(string.toInt() + 10.0.pow(power.toDouble()).toLong())
                }
            } else {
                val long = string.toLong()
                -(long * 10.0.pow(power.toDouble()).toLong())
            }
        } else {
            if (decimal) {
                string.toDouble()
            } else if (string.length < 10) {
                if (power == 0) {
                    string.toInt()
                } else {
                    string.toInt() * 10.0.pow(power.toDouble()).toLong()
                }
            } else {
                val long = string.toLong()
                long * 10.0.pow(power.toDouble()).toLong()
            }
        }
    }

    fun skip(count: Int) {
        index += count
    }

    fun debug(length: Int) = substring(index, (index + length).coerceAtMost(size)).replace("\n", "\\n").replace("\r", "\\r")

    fun nextLine() {
        while (index < size) {
            when (input[index]) {
                ' ', '\t' -> {}
                '\n', '\r' -> {
                    markLine()
                    continue
                }
                '#' -> while (index < size) {
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
            if (char != '\n' && char != '\r') {
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
        if (!inBounds || char != '\r' && char != '\n') {
            throw IllegalArgumentException("Expected newline at $exception")
        }
        markLine()
    }

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
            if (lastLine == end) {
                return ""
            }
            return substring(lastLine, end).replace("\n", "\\n").replace("\r", "\\r").trim()
        }

    val exception: String
        get() = "line=$lineCount char=${index - lastLine} '$line'"

    companion object {
        private fun linebreak(char: Char) = char == '\r' || char == '\n'
    }
}
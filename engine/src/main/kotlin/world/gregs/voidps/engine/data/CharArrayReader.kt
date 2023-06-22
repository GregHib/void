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


    fun skipWhitespaceCommentColon(limit: Int) {
        while (index < limit) {
            if (input[index] == '#') {
                skipComment(limit)
            } else if (input[index] != ' ' && input[index] != ':' && !linebreak(input[index])) {
                break
            }
            index++
        }
    }

    fun skipWhitespaceComments(limit: Int) {
        while (index < limit) {
            if (input[index] == '#') {
                skipComment(limit)
            } else if (input[index] != ' ' && !linebreak(input[index])) {
                break
            }
            index++
        }
    }

    fun skipIfComment(limit: Int = size, breaks: Boolean = true) {
        if (index < limit && input[index] == '#') {
            while (index < limit) {
                val char = input[index]
                if (linebreak(char)) {
                    break
                }
                index++
            }
            if (breaks) {
                skipLineBreaks()
            }
        }
    }

    fun skipComment(limit: Int = size) {
        while (index < limit) {
            val char = input[index]
            if (linebreak(char)) {
                break
            }
            index++
        }
    }

    /**
     * Skip space or line breaks
     */
    fun skipWhitespace(limit: Int = size) {
        while (index < limit && (input[index] == ' ' || linebreak(input[index]))) {
            if (linebreak(input[index])) {
                lastLine = index + 1
            }
            index++
        }
    }

    fun skipSpaces(limit: Int = size) {
        while (index < limit && input[index] == ' ') {
            index++
        }
    }

    fun skipLineBreaks(limit: Int = size) {
        while (index < limit) {
            val char = input[index]
            if (!linebreak(char)) {
                return
            }
            index++
        }
    }

    fun skipExceptLineBreaks(limit: Int) {
        while (index < limit) {
            val char = input[index]
            if (linebreak(char)) {
                return
            }
            index++
        }
    }

    fun skipValueIndex(limit: Int): Int {
        var end = -1
        var previous = ' '
        while (index < limit) {
            val char = input[index]
            if (linebreak(char) || char == '#') {
                break
            } else if (char == ' ' && previous != ' ') {
                end = index
            }
            previous = char
            index++
        }
        if (previous != ' ') {
            return index
        }
        return end
    }

    fun linebreak(char: Char) = char == '\r' || char == '\n'
}
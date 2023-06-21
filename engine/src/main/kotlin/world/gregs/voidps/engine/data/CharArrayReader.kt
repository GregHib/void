package world.gregs.voidps.engine.data

abstract class CharArrayReader {
    var input = CharArray(0)
    var size = 0
    var index = 0

    fun set(charArray: CharArray, size: Int = charArray.size) {
        this.input = charArray
        this.size = size
        this.index = 0
    }

    fun substring(start: Int, end: Int) = String(input, start, end - start)

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
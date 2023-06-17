package world.gregs.voidps.engine.data

import java.util.*

class YamlParser {
    private lateinit var lines: Stack<String>

    private var input = ""
    private var currentIndex = 0

    fun parse(yaml: String): Any {
        val input = yaml.trim()
        lines = Stack()
        input.lines().reversed().forEach {
            if (!it.trim().startsWith('#')) {
                lines.add(it)
            }
        }
        return readCollection()
    }

    fun parse(lines: List<String>): Any {
        this.lines = Stack()
        lines.reversed().forEach {
            if (!it.trim().startsWith('#')) {
                this.lines.add(it)
            }
        }
        return readCollection()
    }

    private fun readCollection(): Any {
        input = lines.peek()
        currentIndex = 0
        val indent = skipWhitespaces()
        return if (currentIndex < input.length && input[currentIndex] == '-') {
            readList(indent)
        } else {
            readMap(null, indent)
        }
    }

    private fun readList(currentIndent: Int): MutableList<Any> {
        val list = mutableListOf<Any>()
        while (lines.isNotEmpty()) {
            input = lines.peek()
            currentIndex = 0
            val indent = skipWhitespaces()
            if (currentIndent == indent) {
                currentIndex++ // skip '-'
                skipWhitespaces()
                lines.pop()
                val listItem = input.substring(currentIndex, input.length).trim()
                list.add(parseValue(listItem, currentIndent + 1))
            } else if (indent > currentIndent) {
                if (indent != currentIndent + 1) {
                    throw IllegalArgumentException("Invalid indent on line '$input'")
                }
                list.add(readCollection())
            } else {
                break
            }
        }
        return list
    }

    private fun readMap(value: String?, currentIndent: Int): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        // read key
        var start = currentIndex
        while (currentIndex < input.length && input[currentIndex] != ':') {
            currentIndex++
        }
        val key = input.substring(start, currentIndex)
        currentIndex++// skip ':'
        skipWhitespaces()
        // continue till the end of the line
        start = currentIndex
        while (currentIndex < input.length && input[currentIndex] != '\n') {
            currentIndex++
        }
        if (start != currentIndex) {
            val entry = input.substring(start, currentIndex)
            map[key] = parseValue(entry, currentIndent + 1)
        }
        while (lines.isNotEmpty()) {
            input = lines.peek()
            currentIndex = 0
            val indent = skipWhitespaces()
            if (indent == currentIndent) {
                lines.pop()
                val k = readKey()
                currentIndex++ // skip ':'
                // TODO replace with input[currentIndex] == '\n'
                val nextIndent = if (lines.isEmpty()) -1 else getIndent(lines.peek())
                if (currentIndex == input.length && lines.isNotEmpty() && nextIndent != indent) { // value is blank
                    map[k] = readCollection()
                } else {
                    map[k] = parseValue(input.substring(currentIndex, input.length).trim(), currentIndent + 1)
                }
            } else if (indent > currentIndent) {
                if (indent != currentIndent + 1) {
                    throw IllegalArgumentException("Invalid indent on line '$input'")
                }
                map[key] = readCollection()
            } else {
                break
            }
        }
        return map
    }

    private fun skipWhitespaces(): Int {
        var count = 0
        while (currentIndex < input.length && input[currentIndex] == ' ') {
            currentIndex++
            count++
        }
        return count / 2
    }

    private fun getIndent(line: String): Int {
        val index = line.indexOfFirst { it != ' ' }
        if (index == -1) {
            return -1
        }
        return index / 2
    }

    private fun readKey(): String {
        val start = currentIndex
        while (currentIndex < input.length && input[currentIndex] != ':') {
            currentIndex++
        }
        return input.substring(start, currentIndex)
    }

    private fun parseMapEntry(line: String): Pair<String, String> {
        val split = split(line, ':')
        val key = split[0].trim()
        val value = if (split.size > 1) split[1].trim() else ""
        return key to value
    }

    private val intRegex = Regex("-?\\d+")
    private val longRegex = Regex("-?\\d+L")
    private val doubleRegex = Regex("-?\\d+\\.\\d+")

    private fun parseValue(text: String, indent: Int): Any {
        var value = text
        val hashIndex = value.indexOf('#')
        if (hashIndex != -1) {
            value = value.substring(0, hashIndex).trim()
        }
        return when {
            value == "true" -> true
            value == "false" -> false
            value.startsWith("&") -> {
                mapOf(value to readCollection())
            }
            value.startsWith('"') && value.endsWith('"') -> value.trim('"')
            value.startsWith('[') && value.endsWith(']') -> split(value.trim('[', ']'), ',').map { parseValue(it.trim(), indent) }
            value.startsWith('{') && value.endsWith('}') -> {
                split(value.trim('{', '}'), ',').associate {
                    val (key, s) = parseMapEntry(it.trim())
                    key to parseValue(s, indent)
                }
            }
            value.matches(intRegex) -> value.toInt()
            value.matches(longRegex) -> value.toLong()
            value.matches(doubleRegex) -> value.toDouble()
            value.contains(":") -> {
                val (key, s) = parseMapEntry(value)
                if (s.isBlank()) {
                    mapOf(key to readCollection())
                } else {
                    readMap(null, indent)
                }
            }
            else -> value

        }
    }

    fun split(text: String, delimiter: Char): List<String> {
        val text = text.trim()
        var depth = 0
        val list = mutableListOf<String>()
        val builder = StringBuilder()
        for (i in text.indices) {
            val char = text[i]
            when (char) {
                '[', '{' -> depth++
                ']', '}' -> depth--
                delimiter -> if (depth == 0) {
                    list.add(builder.toString().trim())
                    builder.clear()
                    continue
                }
            }
            builder.append(char)
        }
        if (builder.isNotEmpty()) {
            list.add(builder.toString().trim())
        }
        return list
    }
}
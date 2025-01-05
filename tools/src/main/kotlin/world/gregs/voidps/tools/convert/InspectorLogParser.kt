package world.gregs.voidps.tools.convert

import java.io.File

/**
 * Parses old inspector logs into a usable data model that can be filtered to get the data desired
 */
object InspectorLogParser {

    data class Log(
        val tick: Int,
        val timestamp: String,
        val entity: Data,
        val event: Data
    )

    data class Data(val name: String, val list: List<Any> = emptyList()) {
        operator fun get(name: String): Any? {
            val pair = list.firstOrNull { it is Pair<*, *> && it.first == name } ?: return null
            return (pair as Pair<*, *>).second
        }

        override fun toString(): String {
            return "$name(${list.map { if (it is Pair<*, *>) "${it.first}=${it.second}" else it }.joinToString(", ")})"
        }
    }

    private val parser = Parser()

    class Parser {
        private var position = 0
        private var input = ""

        fun parse(text: String): Any {
            input = text
            position = 0
            return parseToken()
        }

        private fun parseToken(): Any {
            skipWhitespace()
            return when {
                peek() == '"' -> parseQuotedString()
                peek() == '[' -> parseArray()
                peek() == '{' -> parseMap()
                else -> {
                    val word = parseWord()
                    if (peek() == '=' || peek() == ':') {
                        position++
                        val value = parseToken()
                        Pair(word, value)
                    } else if (peek() == '(') {
                        parseObject(word)
                    } else {
                        word
                    }
                }
            }
        }

        private fun parseObject(name: String): Any {
            val params = mutableListOf<Any>()
            expect('(')
            while (true) {
                skipWhitespace()
                if (peek() == ')') break
                if (peek() != ',') {
                    val element = parseToken()
                    params.add(element)
                }

                skipWhitespace()
                if (peek() == ',') {
                    position++
                    continue
                }
                if (peek() == ')') break
                if (position == input.length) {
                    return Data(name, params)
                }
                throw IllegalArgumentException("Expected ',' or ')' at position $position")
            }
            expect(')')
            return Data(name, params)
        }

        private fun parseArray(): List<Any> {
            val values = mutableListOf<Any>()
            expect('[')
            while (true) {
                skipWhitespace()
                if (peek() == ']') break

                values.add(parseToken())

                skipWhitespace()
                if (peek() == ',') {
                    position++
                    continue
                }
                if (peek() == ']') break
                throw IllegalArgumentException("Expected ',' or ']' at position $position")
            }
            expect(']')
            return values
        }

        private fun parseMap(): List<Any> {
            val values = mutableListOf<Any>()
            expect('{')
            while (true) {
                skipWhitespace()
                if (peek() == '}') break

                values.add(parseToken())

                skipWhitespace()
                if (peek() == ',') {
                    position++
                    continue
                }
                if (peek() == '}') break
                throw IllegalArgumentException("Expected ',' or '}' at position $position")
            }
            expect('}')
            return values
        }

        private fun parseQuotedString(): String {
            expect('"')
            val sb = StringBuilder()
            while (position < input.length) {
                when (val c = input[position]) {
                    '"' -> {
                        position++
                        return sb.toString()
                    }
                    '\\' -> {
                        position++
                        if (position < input.length) {
                            sb.append(input[position])
                            position++
                        }
                    }
                    else -> {
                        sb.append(c)
                        position++
                    }
                }
            }
            throw IllegalArgumentException("Unterminated string at position $position")
        }

        private fun parseWord(): String {
            val sb = StringBuilder()
            while (position < input.length) {
                when (val c = input[position]) {
                    ',', ')', ':', '=', ']', '}', '(' -> {
                        return sb.toString().trim()
                    }
                    '\\' -> {
                        position++
                        if (position < input.length) {
                            sb.append(input[position])
                            position++
                        }
                    }
                    else -> {
                        sb.append(c)
                        position++
                    }
                }
            }
            return sb.toString().trim()
        }

        private fun skipWhitespace() {
            while (position < input.length && input[position].isWhitespace()) {
                position++
            }
        }

        private fun peek(): Char = if (position < input.length) input[position] else '\u0000'

        private fun expect(c: Char) {
            skipWhitespace()
            if (position >= input.length || input[position] != c) {
                throw IllegalArgumentException("Expected '$c' at position $position")
            }
            position++
        }
    }

    private fun parseLogEntry(line: String): Log? {
        val regex = Regex("""\[(\d+)]\s+(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})\s+(.+)""")
        val match = regex.matchEntire(line) ?: return null

        val (tick, timestamp, remainder) = match.destructured
        val regex2 = Regex("""(.+?)(?=\s{2,}|${'$'}|\)[A-Z])(.+)""")
        val match2 = regex2.matchEntire(remainder) ?: return null
        val (entityRaw, eventRaw) = match2.destructured
        return Log(
            tick.toInt(),
            timestamp,
            parseEntityOrEvent(if (eventRaw.startsWith(")")) "$entityRaw)" else entityRaw),
            parseEntityOrEvent(eventRaw.trim().removePrefix(")"))
        )
    }

    private fun parseEntityOrEvent(input: String): Data {
        return if (input.contains("(")) {
            parseKeyValuePairs(input)
        } else {
            Data(input.trim())
        }
    }

    private fun parseKeyValuePairs(input: String): Data {
        if (input.startsWith("Area sound, ")) {
            val parse = parser.parse(input.removePrefix("Area sound, "))
            val data = parse as Data
            return Data("Area sound", listOf(data))
        } else {
            val parse = parser.parse(input)
            if (parse is String) {
                return Data(parse)
            } else if (parse is Pair<*, *>) {
                return Data(parse.toString())
            }
            return parse as Data
        }
    }


    fun isEnd(log: Log): Boolean {
        return when (log.event.name) {
//            "Animation" -> log.event.list.any {
//                it is Pair<*, *> && it.first == "id" && it.second == "-1"
//            }
            "Teleport" -> true
            "Experience" -> true
            "Menu Interaction" -> true
            else -> false
        }
    }

    private fun find(logs: List<Log?>, index: Int, isEnd: (Log) -> Boolean) = (index..(index + 75).coerceAtMost(logs.lastIndex)).firstOrNull { logs[it] != null && isEnd(logs[it]!!) }

    @JvmStatic
    fun main(args: Array<String>) {
        val folder = File("${System.getProperty("user.home")}/Downloads/event-inspector-logs/")
        for (file in folder.listFiles()) {
            if (!file.name.startsWith("agility-pyramid")) {
                continue
            }
            val logs = file.readLines().map { parseLogEntry(it) }
            for ((index, log) in logs.withIndex()) {
                if (log == null) {
                    continue
                }
                if (log.event.name == "MenuClick" && log.event["type"] == "OpLoc1") {
                    println()
                    val end = find(logs, index) { it.event.name == "Experience" }
                        ?: find(logs, index + 1) { it.event.name == "MenuClick" }
                        ?: -1

                    if (end != -1) {
                        val startTick = log.tick
                        for (i in index..(end + 5).coerceAtMost(logs.lastIndex)) {
                            val l = logs[i] ?: continue
                            if (l.event.name == "MenuClick" && i != index) {
                                break
                            }
                            when (l.event.name) {
                                "MenuClick" -> println("[${l.tick - startTick}] ${l.event} // Line ${i + 1}")
                                "FaceCoordinate", "SoundEffect", "Animation", "Teleport", "Movement", "ExactMove", "Experience", "Message",
                                "CamMoveTo", "CamLookAt" -> println("[${l.tick - startTick}] ${l.event}")
                            }
                        }
                    }
                }
            }
        }
    }

}
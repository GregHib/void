package world.gregs.toml

import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal class TomlTest {

    @Test
    fun `Benchmark test`() {
        val text = File("C:\\Users\\Greg\\AppData\\Roaming\\JetBrains\\IntelliJIdea2024.3\\scratches\\scratch.toml").readText().toCharArray()
        var result: Any = ""
        val toml = Toml
        val count = 10
        val start = System.nanoTime()
        for (i in 0 until count) {
            result = toml.decodeFromCharArray(text)
        }
        val end = System.nanoTime()
        println(result)
        println("Took ${(end-start)/count}ns")
    }

    @TestFactory
    fun `Toml examples`() = File(TomlTest::class.java.getResource("valid/")!!.file).listFiles()!!.mapNotNull { file ->
        if (file.isDirectory) {
            return@mapNotNull null
        }
        dynamicTest(file.nameWithoutExtension.toSentenceCase()) {
            val toml = file.readText()
            val result = parse(toml)
            val actual = StringBuilder()
            printJson(actual, result)
            val expected = TomlTest::class.java.getResourceAsStream("valid/expected/${file.nameWithoutExtension}.json")!!.readBytes().toString(Charsets.UTF_8)
            assertEquals(expected, actual.toString())
        }
    }

    @TestFactory
    fun `Invalid toml examples`() = File(TomlTest::class.java.getResource("invalid/")!!.file).listFiles()!!.mapNotNull { file ->
        if (file.isDirectory || file.nameWithoutExtension == "invalid-inline-table") {
//            Maps are only shallow immutable so no way to check if it's an inline map to prevent modification.
            return@mapNotNull null
        }
        dynamicTest(file.nameWithoutExtension.toSentenceCase()) {
            val toml = file.readText()
            assertThrows<IllegalArgumentException> {
                parse(toml)
            }
        }
    }

    private fun parse(string: String): Map<String, Any> {
        return Toml.decodeFromString(string)
    }

    private fun printJson(builder: StringBuilder, any: Any) {
        when (any) {
            is Map<*, *> -> {
                builder.append('{')
                var first = true
                for ((key, value) in (any as Map<String, Any>).toSortedMap()) {
                    if (first) {
                        first = false
                    } else {
                        builder.append(',')
                    }
                    builder.append("\"${escapeString(key)}\":")
                    printJson(builder, value!!)
                }
                builder.append('}')
            }
            is List<*> -> {
                builder.append('[')
                var first = true
                for (value in any) {
                    if (first) {
                        first = false
                    } else {
                        builder.append(',')
                    }
                    printJson(builder, value!!)
                }
                builder.append(']')
            }
            is Array<*> -> {
                builder.append('[')
                var first = true
                for (value in any) {
                    if (first) {
                        first = false
                    } else {
                        builder.append(',')
                    }
                    printJson(builder, value!!)
                }
                builder.append(']')
            }
            is String -> {
                builder.append('\"')
                builder.append(escapeString(any))
                builder.append('\"')
            }
            is Instant, is LocalDateTime, is LocalDate, is LocalTime -> {
                builder.append('\"')
                builder.append(any)
                builder.append('\"')
            }
            else -> builder.append(any)
        }
    }

    private fun escapeString(input: String): String {
        var previous = ' '
        for (char in input) {
            if (char == '"' && previous != '\\') {
                return input.replace("\"", "\\\"")
            }
            previous = char
        }
        return input
    }
}
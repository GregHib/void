package world.gregs.config

import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.BufferedWriter
import java.io.StringWriter

class ConfigWriterTest {
    private val input = mapOf(
        "key value pair" to mapOf("key" to "value"),
        "number value" to mapOf("key" to 1234),
        "double value" to mapOf("key" to 12.34),
        "boolean value" to mapOf("key" to true),
        "multiple values in a section" to mapOf("dogs" to mapOf("name" to "Fido", "breed" to "pug")),
        "multiple sections" to mapOf("server" to mapOf("ip" to "127.0.0.1"), "database" to mapOf("port" to 8000)),
        "inline array" to mapOf("list" to listOf("one", "two", "three")),
        "inline map" to mapOf("map" to mapOf("one" to 1, "two" to 2, "three" to 3, "four" to 4)),
        "nested lists" to mapOf("lists" to listOf(listOf(1, 2, 3), listOf("one", "two", "three"))),
        "nested maps" to mapOf("lists" to mapOf("one" to mapOf("name" to "one", "number" to 1), "two" to mapOf("name" to "two", "number" to 2))),
        "nested list of maps" to mapOf("lists" to listOf(mapOf("host" to "alpha", "enabled" to true), mapOf("host" to "beta", "enabled" to false))),
        "nested map of lists" to mapOf("lists" to mapOf("one" to listOf(1, "un", "eins"), "two" to listOf(2, "duex", "zwei"))),
    )

    @TestFactory
    fun `Writable configs`() = input.map { (name, input) ->
        dynamicTest(name.toSentenceCase()) {
            val writer = ConfigWriter()
            val stringWriter = StringWriter()
            BufferedWriter(stringWriter).use { output ->
                writer.encode(output, input)
            }
            val expected = ConfigWriterTest::class.java.getResourceAsStream("write/valid/${name.toKebabCase()}.toml")!!
                .readBytes()
                .toString(Charsets.UTF_8)
                .replace("\r\n", "\n")
                .trim('\n')
            assertEquals(expected, stringWriter.buffer.toString().trim('\n'))
        }
    }

}
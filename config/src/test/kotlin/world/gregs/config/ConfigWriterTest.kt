package world.gregs.config

import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.BufferedWriter
import java.io.StringWriter

class ConfigWriterTest {
    private val input = mapOf<String, Map<String, Any>>(
        "key value pair" to mapOf("key" to "value"),
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
            assertEquals(expected, stringWriter.buffer.toString())
        }
    }

}
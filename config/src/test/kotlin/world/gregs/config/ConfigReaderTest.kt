package world.gregs.config

import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class ConfigReaderTest {
    @TestFactory
    fun `Readable configs`() = File(ConfigReaderTest::class.java.getResource("read/valid/")!!.file)
        .listFiles { f -> f.isFile && f.extension == "toml" }!!
        .map { file ->
            dynamicTest(file.nameWithoutExtension.toSentenceCase()) {
                val string = parse(file)
                val expected = ConfigReaderTest::class.java.getResourceAsStream("read/valid/${file.nameWithoutExtension}.txt")!!
                    .readBytes()
                    .toString(Charsets.UTF_8)
                    .replace("\r\n", "\n")
                assertEquals(expected, string)
            }
        }

    @TestFactory
    fun `Invalid configs`() = File(ConfigReaderTest::class.java.getResource("read/invalid/")!!.file)
        .listFiles { f -> f.isFile && f.extension == "toml" }!!
        .map { file ->
            dynamicTest(file.nameWithoutExtension.toSentenceCase()) {
                assertThrows<IllegalArgumentException> {
                    parse(file)
                }
            }
        }

    private fun parse(file: File): String {
        val reader = ConfigReader(file.inputStream().buffered())
        val builder = StringBuilder()
        while (reader.nextSection()) {
            val section = reader.section()
            while (reader.nextPair()) {
                val key = reader.key()
                parseValue(builder, reader, section, key)
            }
        }
        return builder.toString()
    }

    private fun parseValue(builder: StringBuilder, reader: ConfigReader, section: String, key: String, collection: String = "") {
        when (reader.byte) {
            '"'.code, '\''.code -> builder.appendLine("[$section] $key$collection = \"${reader.string()}\"")
            '['.code -> {
                var index = 0
                while (reader.nextElement()) {
                    parseValue(builder, reader, section, key, collection = "${collection}[${index++}]")
                }
            }
            '{'.code -> {
                while (reader.nextEntry()) {
                    val k = reader.key()
                    parseValue(builder, reader, section, key, collection = "${collection}[\"${k}\"]")
                }
            }
            't'.code, 'f'.code -> builder.appendLine("[$section] $key$collection = ${reader.boolean()}")
            '-'.code, '+'.code, '0'.code, '1'.code, '2'.code, '3'.code, '4'.code, '5'.code, '6'.code, '7'.code, '8'.code, '9'.code ->
                builder.appendLine("[$section] $key$collection = ${reader.number()}")
            else -> throw IllegalArgumentException("Unexpected character section=${section} char=${reader.byte.toChar()}")
        }
    }

    @Test
    fun `Ignore comments and lines`() {
        resource("next-line")
            .replaceFirst("\r\n", "\n")
            .byteInputStream().use { input ->
                val parser = ConfigReader(input)
                assertFalse(parser.nextSection())
                assertEquals(-1, input.read())
            }
    }

    @Test
    fun `Read section`() {
        "[section]".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertFalse(parser.nextPair())
            assertTrue(parser.nextSection())
            assertTrue(parser.nextSection())
            assertEquals("section", parser.section())
            assertFalse(parser.nextSection())
        }
    }

    @Test
    fun `Read key`() {
        "bare-key".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertTrue(parser.nextPair())
            assertEquals("bare-key", parser.key())
        }
        "\"quoted \\n key\"".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertTrue(parser.nextPair())
            assertEquals("quoted \\n key", parser.key())
        }
        "'literal \n key'".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertTrue(parser.nextPair())
            assertEquals("literal \n key", parser.key())
        }
    }

    @Test
    fun `Read string`() {
        "\"quoted \\n string\"".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals("quoted \\n string", parser.string())
        }
        "'literal \n string'".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertTrue(parser.nextPair())
            assertEquals("literal \n string", parser.string())
        }
    }

    @Test
    fun `Read number`() {
        "1234".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(1234L, parser.number())
        }
        "-1234".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(-1234L, parser.number())
        }
        "+1234".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(1234L, parser.number())
        }
        "12.34".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(12.34, parser.number())
        }
        "-12.34".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(-12.34, parser.number())
        }
        "+12.34".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(12.34, parser.number())
        }
    }

    @Test
    fun `Read long`() {
        "1234".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(1234L, parser.long())
        }
        "-1234".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(-1234L, parser.long())
        }
        "+1234".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(1234L, parser.long())
        }
    }

    @Test
    fun `Read double`() {
        "12.34".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(12.34, parser.double())
        }
        "-12.34".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(-12.34, parser.double())
        }
        "+12.34".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(12.34, parser.double())
        }
    }

    @Test
    fun `Read boolean`() {
        "true".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertTrue(parser.boolean())
        }
        "false".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertFalse(parser.boolean())
        }
    }

    @Test
    fun `Read list`() {
        "[ 1, 2, 3, 4 ]".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(listOf(1L, 2L, 3L, 4L), parser.list())
        }
        "[ \"1\", \"two\", 3 ]".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(listOf("1", "two", 3L), parser.list())
        }
        "[1 , # comment \n \"two\" # c\n,3]".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(listOf(1L, "two", 3L), parser.list())
        }
        "[]".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(emptyList<Any>(), parser.list())
        }
        "[  ]".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(emptyList<Any>(), parser.list())
        }
        "[#comment\n]".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(emptyList<Any>(), parser.list())
        }
        "[\n#comment\n ]".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(emptyList<Any>(), parser.list())
        }
    }

    @Test
    fun `Read map`() {
        "{ one = 1, two = 2, three = 3 }".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(mapOf("one" to 1L, "two" to 2L, "three" to 3L), parser.map())
        }
        "{ one = \"1\", two = \"two\", three = 3 }".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(mapOf("one" to "1", "two" to "two", "three" to 3L), parser.map())
        }
        "{one=1 , # comment \n two =\"two\" # c\n,three= 3}".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(mapOf("one" to 1L, "two" to "two", "three" to 3L), parser.map())
        }
        "{}".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(emptyMap<String, Any>(), parser.map())
        }
        "{  }".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(emptyMap<String, Any>(), parser.map())
        }
        "{#comment\n }".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(emptyMap<String, Any>(), parser.map())
        }
        "{\n#comment \n}".byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertEquals(emptyMap<String, Any>(), parser.map())
        }
    }

    @Test
    fun `Read multiple section`() {
        resource("multi-sections").byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertTrue(parser.nextSection())
            assertEquals("", parser.section())
            assertTrue(parser.nextPair())
            assertEquals("one", parser.key())
            assertEquals(1, parser.int())

            assertTrue(parser.nextSection())
            assertEquals("section-1", parser.section())
            assertTrue(parser.nextPair())
            assertEquals("two", parser.key())
            assertEquals(2, parser.int())
            assertEquals("three", parser.key())
            assertEquals(3, parser.int())

            assertTrue(parser.nextSection())
            assertTrue(parser.nextSection()) // Multiple calls don't change anything
            assertEquals("section-2", parser.section())
            assertTrue(parser.nextPair())
            assertEquals("four", parser.key())
            assertEquals("four", parser.string())
            assertFalse(parser.nextPair())
            assertFalse(parser.nextSection())
        }
    }

    @Test
    fun `Read root section`() {
        resource("root-pairs").byteInputStream().use { input ->
            val parser = ConfigReader(input)
            assertTrue(parser.nextSection())
            assertTrue(parser.nextPair())
            assertEquals("", parser.section())
            assertEquals("key", parser.key())
            assertEquals("value", parser.string())
            assertTrue(parser.nextPair())
            assertEquals("number", parser.key())
            assertEquals(3.14, parser.double())
            assertFalse(parser.nextPair())
            assertFalse(parser.nextSection())
        }
    }

    private fun resource(file: String) = ConfigReaderTest::class.java.getResourceAsStream("read/$file.toml")!!
        .readBytes()
        .toString(Charsets.UTF_8)
}
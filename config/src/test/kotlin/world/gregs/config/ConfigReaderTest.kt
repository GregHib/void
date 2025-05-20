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
        val builder = StringBuilder()
        Config.fileReader(file) {
            while (nextSection()) {
                val section = section()
                while (nextPair()) {
                    val key = key()
                    parseValue(builder, this, section, key)
                }
            }
        }
        return builder.toString()
    }

    private fun parseValue(builder: StringBuilder, reader: ConfigReader, section: String, key: String, collection: String = "") {
        when (reader.peek) {
            '"', '\'' -> builder.appendLine("[$section] $key$collection = \"${reader.string()}\"")
            '[' -> {
                var index = 0
                while (reader.nextElement()) {
                    parseValue(builder, reader, section, key, collection = "${collection}[${index++}]")
                }
            }
            '{' -> {
                while (reader.nextEntry()) {
                    val k = reader.key()
                    parseValue(builder, reader, section, key, collection = "${collection}[\"${k}\"]")
                }
            }
            't', 'f' -> builder.appendLine("[$section] $key$collection = ${reader.boolean()}")
            '-', '+', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                builder.appendLine("[$section] $key$collection = ${reader.number()}")
            else -> throw IllegalArgumentException("Unexpected character section=${section} char=${reader.peek}")
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
        Config.stringReader("[section]") {
            assertFalse(nextPair())
            assertTrue(nextSection())
            assertTrue(nextSection())
            assertEquals("section", section())
            assertFalse(nextSection())
        }
    }

    @Test
    fun `Read key`() {
        Config.stringReader("bare_key ") {
            assertTrue(nextPair())
            val exception = assertThrows<IllegalArgumentException> {
                key()
            }
            assertEquals("Expected equals after key. line=1 char='<end-of-file>'", exception.message)
        }
        Config.stringReader("bare_key = 0") {
            assertTrue(nextPair())
            assertEquals("bare_key", key())
        }
        Config.stringReader("\"quoted \\n key\" = 0") {
            assertTrue(nextPair())
            assertEquals("quoted \\n key", key())
        }
        Config.stringReader("'literal \n key' = 0") {
            assertTrue(nextPair())
            assertEquals("literal \n key", key())
        }
    }

    @Test
    fun `Read string`() {
        Config.stringReader("\"quoted \\n string\"") {
            assertEquals("quoted \\n string", string())
        }
        Config.stringReader("'literal \n string'") {
            assertTrue(nextPair())
            assertEquals("literal \n string", string())
        }
    }

    @Test
    fun `Read number`() {
        Config.stringReader("1234") {
            assertEquals(1234L, number())
        }
        Config.stringReader("-1234") {
            assertEquals(-1234L, number())
        }
        Config.stringReader("+1234") {
            assertEquals(1234L, number())
        }
        Config.stringReader("12.34") {
            assertEquals(12.34, number())
        }
        Config.stringReader("-12.34") {
            assertEquals(-12.34, number())
        }
        Config.stringReader("+12.34") {
            assertEquals(12.34, number())
        }
    }

    @Test
    fun `Read int`() {
        Config.stringReader("1234") {
            assertEquals(1234, int())
        }
        Config.stringReader("-1234") {
            assertEquals(-1234, int())
        }
        Config.stringReader("+1234") {
            assertEquals(1234, int())
        }
    }

    @Test
    fun `Read long`() {
        Config.stringReader("1234") {
            assertEquals(1234L, long())
        }
        Config.stringReader("-1234") {
            assertEquals(-1234L, long())
        }
        Config.stringReader("+1234") {
            assertEquals(1234L, long())
        }
    }

    @Test
    fun `Read double`() {
        Config.stringReader("12.34") {
            assertEquals(12.34, double())
        }
        Config.stringReader("-12.34") {
            assertEquals(-12.34, double())
        }
        Config.stringReader("+12.34") {
            assertEquals(12.34, double())
        }
//        Config.stringReader("1.4E7") {
//            assertEquals(14_000_000.0, double())
//        }
//        Config.stringReader("1.4E+7") {
//            assertEquals(14_000_000.0, double())
//        }
//        Config.stringReader("1.4E-7") {
//            assertEquals(0.00000014, double())
//        }
    }

    @Test
    fun `Read boolean`() {
        Config.stringReader("true") {
            assertTrue(boolean())
        }
        Config.stringReader("false") {
            assertFalse(boolean())
        }
    }

    @Test
    fun `Read list`() {
        Config.stringReader("[ 1, 2, 3, 4 ]") {
            assertEquals(listOf(1, 2, 3, 4), list())
        }
        Config.stringReader("[ \"1\", \"two\", 3 ]") {
            assertEquals(listOf("1", "two", 3), list())
        }
        Config.stringReader("[1 , # comment \n \"two\" # c\n,3]") {
            assertEquals(listOf(1, "two", 3), list())
        }
        Config.stringReader("[]") {
            assertEquals(emptyList<Any>(), list())
        }
        Config.stringReader("[  ]") {
            assertEquals(emptyList<Any>(), list())
        }
        Config.stringReader("[#comment\n]") {
            assertEquals(emptyList<Any>(), list())
        }
        Config.stringReader("[\n#comment\n ]") {
            assertEquals(emptyList<Any>(), list())
        }
    }

    @Test
    fun `Read map`() {
        Config.stringReader("{ one = 1, two = 2, three = 3 }") {
            assertEquals(mapOf("one" to 1, "two" to 2, "three" to 3), map())
        }
        Config.stringReader("{ one = \"1\", two = \"two\", three = 3 }") {
            assertEquals(mapOf("one" to "1", "two" to "two", "three" to 3), map())
        }
        Config.stringReader("{one=1 , # comment \n two =\"two\" # c\n,three= 3}") {
            assertEquals(mapOf("one" to 1, "two" to "two", "three" to 3), map())
        }
        Config.stringReader("{}") {
            assertEquals(emptyMap<String, Any>(), map())
        }
        Config.stringReader("{  }") {
            assertEquals(emptyMap<String, Any>(), map())
        }
        Config.stringReader("{#comment\n }") {
            assertEquals(emptyMap<String, Any>(), map())
        }
        Config.stringReader("{\n#comment \n}") {
            assertEquals(emptyMap<String, Any>(), map())
        }
    }

    @Test
    fun `Read value`() {
        Config.stringReader("false") {
            assertEquals(false, value())
        }
        Config.stringReader("\"value\"") {
            assertEquals("value", value())
        }
        Config.stringReader("'literal'") {
            assertEquals("literal", value())
        }
        Config.stringReader("+123") {
            assertEquals(123, value())
        }
        Config.stringReader("123") {
            assertEquals(123, value())
        }
        Config.stringReader("-12.3") {
            assertEquals(-12.3, value())
        }
        Config.stringReader("[ 1, 2 ]") {
            assertEquals(listOf(1, 2), value())
        }
        Config.stringReader("{ one = 1 }") {
            assertEquals(mapOf("one" to 1), value())
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

        resource("multi-sections").byteInputStream().use { input ->
            val parser = ConfigReader(input)
            val sections = parser.sections()
            assertEquals(3, sections.size)
            assertTrue(sections.contains(""))
            assertTrue(sections.contains("section-1"))
            assertTrue(sections.contains("section-2"))
            val root = sections[""]
            assertNotNull(root)
            assertEquals(1, root!!.size)
            assertTrue(root.contains("one"))
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
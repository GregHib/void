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
        Config.fileReader(file).use { reader ->
            while (reader.nextSection()) {
                val section = reader.section()
                while (reader.nextPair()) {
                    val key = reader.key()
                    parseValue(builder, reader, section, key)
                }
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
        Config.stringReader("[section]").use { reader ->
            assertFalse(reader.nextPair())
            assertTrue(reader.nextSection())
            assertTrue(reader.nextSection())
            assertEquals("section", reader.section())
            assertFalse(reader.nextSection())
        }
    }

    @Test
    fun `Read key`() {
        Config.stringReader("[section]")
        Config.stringReader("bare-key").use { reader ->
            assertTrue(reader.nextPair())
            assertEquals("bare-key", reader.key())
        }
        Config.stringReader("\"quoted \\n key\"").use { reader ->
            assertTrue(reader.nextPair())
            assertEquals("quoted \\n key", reader.key())
        }
        Config.stringReader("'literal \n key'").use { reader ->
            assertTrue(reader.nextPair())
            assertEquals("literal \n key", reader.key())
        }
    }

    @Test
    fun `Read string`() {
        Config.stringReader("\"quoted \\n string\"").use { reader ->
            assertEquals("quoted \\n string", reader.string())
        }
        Config.stringReader("'literal \n string'").use { reader ->
            assertTrue(reader.nextPair())
            assertEquals("literal \n string", reader.string())
        }
    }

    @Test
    fun `Read number`() {
        Config.stringReader("1234").use { reader ->
            assertEquals(1234L, reader.number())
        }
        Config.stringReader("-1234").use { reader ->
            assertEquals(-1234L, reader.number())
        }
        Config.stringReader("+1234").use { reader ->
            assertEquals(1234L, reader.number())
        }
        Config.stringReader("12.34").use { reader ->
            assertEquals(12.34, reader.number())
        }
        Config.stringReader("-12.34").use { reader ->
            assertEquals(-12.34, reader.number())
        }
        Config.stringReader("+12.34").use { reader ->
            assertEquals(12.34, reader.number())
        }
    }

    @Test
    fun `Read int`() {
        Config.stringReader("1234").use { reader ->
            assertEquals(1234, reader.int())
        }
        Config.stringReader("-1234").use { reader ->
            assertEquals(-1234, reader.int())
        }
        Config.stringReader("+1234").use { reader ->
            assertEquals(1234, reader.int())
        }
    }

    @Test
    fun `Read long`() {
        Config.stringReader("1234").use { reader ->
            assertEquals(1234L, reader.long())
        }
        Config.stringReader("-1234").use { reader ->
            assertEquals(-1234L, reader.long())
        }
        Config.stringReader("+1234").use { reader ->
            assertEquals(1234L, reader.long())
        }
    }

    @Test
    fun `Read double`() {
        Config.stringReader("12.34").use { reader ->
            assertEquals(12.34, reader.double())
        }
        Config.stringReader("-12.34").use { reader ->
            assertEquals(-12.34, reader.double())
        }
        Config.stringReader("+12.34").use { reader ->
            assertEquals(12.34, reader.double())
        }
    }

    @Test
    fun `Read boolean`() {
        Config.stringReader("true").use { reader ->
            assertTrue(reader.boolean())
        }
        Config.stringReader("false").use { reader ->
            assertFalse(reader.boolean())
        }
    }

    @Test
    fun `Read list`() {
        Config.stringReader("[ 1, 2, 3, 4 ]").use { reader ->
            assertEquals(listOf(1L, 2L, 3L, 4L), reader.list())
        }
        Config.stringReader("[ \"1\", \"two\", 3 ]").use { reader ->
            assertEquals(listOf("1", "two", 3L), reader.list())
        }
        Config.stringReader("[1 , # comment \n \"two\" # c\n,3]").use { reader ->
            assertEquals(listOf(1L, "two", 3L), reader.list())
        }
        Config.stringReader("[]").use { reader ->
            assertEquals(emptyList<Any>(), reader.list())
        }
        Config.stringReader("[  ]").use { reader ->
            assertEquals(emptyList<Any>(), reader.list())
        }
        Config.stringReader("[#comment\n]").use { reader ->
            assertEquals(emptyList<Any>(), reader.list())
        }
        Config.stringReader("[\n#comment\n ]").use { reader ->
            assertEquals(emptyList<Any>(), reader.list())
        }
    }

    @Test
    fun `Read map`() {
        Config.stringReader("{ one = 1, two = 2, three = 3 }").use { reader ->
            assertEquals(mapOf("one" to 1L, "two" to 2L, "three" to 3L), reader.map())
        }
        Config.stringReader("{ one = \"1\", two = \"two\", three = 3 }").use { reader ->
            assertEquals(mapOf("one" to "1", "two" to "two", "three" to 3L), reader.map())
        }
        Config.stringReader("{one=1 , # comment \n two =\"two\" # c\n,three= 3}").use { reader ->
            assertEquals(mapOf("one" to 1L, "two" to "two", "three" to 3L), reader.map())
        }
        Config.stringReader("{}").use { reader ->
            assertEquals(emptyMap<String, Any>(), reader.map())
        }
        Config.stringReader("{  }").use { reader ->
            assertEquals(emptyMap<String, Any>(), reader.map())
        }
        Config.stringReader("{#comment\n }").use { reader ->
            assertEquals(emptyMap<String, Any>(), reader.map())
        }
        Config.stringReader("{\n#comment \n}").use { reader ->
            assertEquals(emptyMap<String, Any>(), reader.map())
        }
    }

    @Test
    fun `Read value`() {
        Config.stringReader("false").use { reader ->
            assertEquals(false, reader.value())
        }
        Config.stringReader("\"value\"").use { reader ->
            assertEquals("value", reader.value())
        }
        Config.stringReader("'literal'").use { reader ->
            assertEquals("literal", reader.value())
        }
        Config.stringReader("+123").use { reader ->
            assertEquals(123L, reader.value())
        }
        Config.stringReader("123").use { reader ->
            assertEquals(123L, reader.value())
        }
        Config.stringReader("-12.3").use { reader ->
            assertEquals(-12.3, reader.value())
        }
        Config.stringReader("[ 1, 2 ]").use { reader ->
            assertEquals(listOf(1L, 2L), reader.value())
        }
        Config.stringReader("{ one = 1 }").use { reader ->
            assertEquals(mapOf("one" to 1L), reader.value())
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
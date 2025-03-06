package world.gregs.toml

import net.pearx.kasechange.toSentenceCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.charset.Charset

internal class TomlTest {

    @Test
    fun `Test comments`() {
        val toml = """
            # This is a full-line comment
            key = "value"  # This is a comment at the end of a line
            another = "# This is not a comment"
        """.trimIndent()
        val expected = mapOf(
            "key" to "value",
            "another" to "# This is not a comment"
        )
        assertEquals(expected, parse(toml))
        assertEquals(emptyMap<String, Any>(), parse(""))
        assertEquals(emptyMap<String, Any>(), parse("   # spaces before comments are fine"))
        assertThrows<IllegalArgumentException> {
            parse("// backslashes are invalid")
        }
    }

    @Test
    fun `Test key value pairs`() {
        val keys = """
            key = "value"
            bare_key = "value"
            bare-key = "value"
            1234 = "value"
            3.14159 = "pi"
        """.trimIndent()

        val expected = mapOf(
            "key" to "value",
            "bare_key" to "value",
            "bare-key" to "value",
            "1234" to "value",
            "3" to mapOf("14159" to "pi")
        )
        assertEquals(expected, parse(keys))
        assertThrows<IllegalArgumentException> {
            parse("key = # Invalid")
        }
        assertThrows<IllegalArgumentException> {
            parse("first = \"Value\" last = \"invalid\"")
        }
    }

    @Test
    fun `Test quoted keys`() {
        val toml = """
            "127.0.0.1" = "value"
            "character encoding" = "value"
            "ʎǝʞ" = "value"
            'key2' = "value"
            'quoted "value"' = "value"
            "quoted 'value'" = "value"
        """.trimIndent()
        val expected = mapOf(
            "127.0.0.1" to "value",
            "character encoding" to "value",
            "ʎǝʞ" to "value",
            "key2" to "value",
            "quoted \"value\"" to "value",
            "quoted 'value'" to "value",
        )
        assertEquals(expected, parse(toml))
        assertThrows<IllegalArgumentException> {
            parse("= \"value\"")
        }
        assertThrows<IllegalArgumentException> {
            parse(" = \"value\"")
        }
        assertEquals(mapOf("" to "value"), parse("\"\" = \"value\""))
        assertEquals(mapOf("" to "value"), parse("'' = \"value\""))
    }

    @Test
    fun `Test dotted keys`() {
        val keys = """
            name = "Orange"
            physical.colour = "orange"
            physical.shape = "round"
            site."google.com" = true
        """.trimIndent()
        val expected = mapOf(
            "name" to "Orange",
            "physical" to mapOf(
                "colour" to "orange",
                "shape" to "round"
            ),
            "site" to mapOf("google.com" to true)
        )
        assertEquals(expected, parse(keys))
    }

    @Test
    fun `Test spaced dotted keys`() {
        val keys = """
            fruit.name = "banana"
            fruit. color = "yellow"
            fruit . flavour = "banana"
        """.trimIndent()
        val expected = mapOf(
            "fruit" to mapOf(
                "name" to "banana",
                "color" to "yellow",
                "flavour" to "banana",
            )
        )
        assertEquals(expected, parse(keys))
    }

    @Test
    fun `Invalid repeated key`() {
        val repeat = """
            name = "Value"
            name = "value"
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            parse(repeat)
        }
    }

    @Test
    fun `Invalid repeated quoted key`() {
        val repeat = """
            spelling = "favourite"
            "spelling" = "favourite"
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            parse(repeat)
        }
    }

    @Test
    fun `Append key`() {
        val toml = """
            # This makes the key "fruit" into a table.
            fruit.apple.smooth = true
            
            # So then you can add to the table "fruit" like so:
            fruit.orange = 2
        """.trimIndent()

        val expected = mapOf(
            "fruit" to mapOf("apple" to mapOf("smooth" to true), "orange" to 2),
        )
        assertEquals(expected, parse(toml))
    }

    @Test
    fun `Append invalid key order`() {
        val toml = """
            # THE FOLLOWING IS INVALID

            # This defines the value of fruit.apple to be an integer.
            fruit.apple = 1
            
            # But then this treats fruit.apple like it's a table.
            # You can't turn an integer into a table.
            fruit.apple.smooth = true
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            parse(toml)
        }
    }

    @Test
    fun `Out of order keys`() {
        val toml = """
            # VALID BUT DISCOURAGED

            apple.type = "fruit"
            orange.type = "fruit"
            
            apple.skin = "thin"
            orange.skin = "thick"
            
            apple.color = "red"
            orange.color = "orange"
        """.trimIndent()
        val expected = mapOf(
            "apple" to mapOf("type" to "fruit", "skin" to "thin", "color" to "red"),
            "orange" to mapOf("type" to "fruit", "skin" to "thick", "color" to "orange"),
        )
        assertEquals(expected, parse(toml))
    }

    @Test
    fun `Ordered keys`() {
        val toml = """
            # RECOMMENDED

            apple.type = "fruit"
            apple.skin = "thin"
            apple.color = "red"
            
            orange.type = "fruit"
            orange.skin = "thick"
            orange.color = "orange"
        """.trimIndent()
        val expected = mapOf(
            "apple" to mapOf("type" to "fruit", "skin" to "thin", "color" to "red"),
            "orange" to mapOf("type" to "fruit", "skin" to "thick", "color" to "orange"),
        )
        assertEquals(expected, parse(toml))
    }

    @Test
    fun `String are escaped`() {
        val toml = """
            str = "I'm a string. \"You can quote me\". Name\tJos\u00E9\nLocation\tSF."
        """.trimIndent()
        val expected = mapOf(
            "str" to "I'm a string. \\\"You can quote me\\\". Name\\tJos\\u00E9\\nLocation\\tSF.",
        )
        assertEquals(expected, parse(toml))
    }

    @Test
    fun `Multi-line strings`() {
        val toml = """
            str1 = ""${'"'}
            Roses are red
            Violets are blue""${'"'}
        """.trimIndent()
        val expected = mapOf(
            "str1" to "Roses are red\nViolets are blue",
        )
        assertEquals(expected, parse(toml))
    }

    @Test
    fun `Line ending backslash`() {
        val toml = TomlTest::class.java.getResourceAsStream("/line-ending-backslash.toml")!!.readAllBytes().toString(Charset.defaultCharset())
        val expected = mapOf(
            "str1" to "The quick brown fox jumps over the lazy dog.",
            "str2" to "The quick brown fox jumps over the lazy dog.",
            "str3" to "The quick brown fox jumps over the lazy dog.",
        )
        assertEquals(expected, parse(toml))
    }

    @Test
    fun `String literals`() {
        val toml = TomlTest::class.java.getResourceAsStream("/string-literals.toml")!!.readAllBytes().toString(Charset.defaultCharset())
        val expected = mapOf(
            "winpath" to "C:\\Users\\nodejs\\templates",
            "winpath2" to "\\\\ServerX\\admin\$\\system32\\",
            "quoted" to "Tom \"Dubs\" Preston-Werner",
            "regex" to "<\\i\\c*\\s*>",
        )
        assertEquals(expected, parse(toml))
    }

    @Test
    fun `Multi-line string literals`() {
        val toml = TomlTest::class.java.getResourceAsStream("/multi-line-string-literals.toml")!!.readAllBytes().toString(Charset.defaultCharset())
        val expected = mapOf(
            "regex2" to "I [dw]on't need \\d{2} apples",
            "lines" to "The first newline is\r\ntrimmed in raw strings.\r\n   All other whitespace\r\n   is preserved.\r\n",
        )
        val actual = parse(toml)
        assertEquals(expected, actual)
    }

    @TestFactory
    fun `Integration test`(): List<DynamicTest> {
        val files = File("./src/test/resources/").listFiles()!!
        return files.mapNotNull { file ->
            if (file.nameWithoutExtension == "invalid-inline-table") {
                // Maps are only shallow immutable so no way to check if it's an inline map or not to prevent modification.
                return@mapNotNull null
            }
            dynamicTest(file.nameWithoutExtension.toSentenceCase()) {
                val toml = file.readText()
                if (file.name.startsWith("invalid-")) {
                    assertThrows<IllegalArgumentException> {
                        parse(toml)
                    }
                } else {
                    println(parse(toml))
                }
            }
        }
    }

    private fun parse(string: String): Map<String, Any> {
        return Toml.decodeFromString(string)
    }
}
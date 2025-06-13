package world.gregs.yaml

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CharReaderTest {

    private val reader = CharReader(mutableMapOf())

    @Test
    fun `Read initial line`() {
        reader.set(
            """
            a line
            item
            """.trimIndent(),
        )
        reader.nextLine()
        assertEquals(0, reader.indentation)
        assertEquals(0, reader.index)
    }

    @Test
    fun `Read line with trailing space and comment`() {
        reader.set(
            """
            a line # with comment
            item
            """.trimIndent(),
        )

        var index = skip("a line")
        index += " # with comment\n".length
        reader.nextLine()
        assertEquals(0, reader.indentation)
        assertEquals(index, reader.index)
    }

    @Test
    fun `Read line with comment`() {
        reader.set(
            """
            a line# with comment
            item
            """.trimIndent(),
        )

        var index = skip("a line")
        index += "# with comment\n".length
        reader.nextLine()
        assertEquals(0, reader.indentation)
        assertEquals(index, reader.index)
    }

    @Test
    fun `Read line with excess spaces`() {
        reader.set(
            """
            a line   
            item
            """.trimIndent(),
        )

        var index = skip("a line")
        index += "   \n".length
        reader.nextLine()
        assertEquals(0, reader.indentation)
        assertEquals(index, reader.index)
    }

    @Test
    fun `Read line`() {
        reader.set(
            """
            a line
            item
            """.trimIndent(),
        )

        var index = skip("a line")
        index += "\n".length
        reader.nextLine()
        assertEquals(0, reader.indentation)
        assertEquals(index, reader.index)
    }

    @Test
    fun `Read multi-line indent`() {
        reader.set(
            """
            a line
              item
            """.trimIndent(),
        )

        var index = skip("a line")
        index += "\n  ".length
        reader.nextLine()
        assertEquals(1, reader.indentation)
        assertEquals(index, reader.index)
    }

    @Test
    fun `Read large indent`() {
        reader.set(
            """
            a line
                  item
            """.trimIndent(),
        )

        var index = skip("a line")
        index += "\n      ".length
        reader.nextLine()
        assertEquals(3, reader.indentation)
        assertEquals(index, reader.index)
    }

    @Test
    fun `Read line with indent`() {
        reader.set("  a line")

        reader.nextLine()
        assertEquals(1, reader.indentation)
        assertEquals(2, reader.index)
    }

    @Test
    fun `Read lower indent`() {
        reader.set("    a line\n  item")

        skip("    a line")
        reader.nextLine()
        assertEquals(1, reader.indentation)
        assertEquals(13, reader.index)
    }

    @Test
    fun `Read line with tabs`() {
        reader.set("a line\t\n\titem")

        var index = skip("a line")
        index += " \n ".length
        reader.nextLine()
        assertEquals(1, reader.indentation)
        assertEquals(index, reader.index)
    }

    private fun skip(string: String): Int {
        reader.skip(string.length)
        return string.length
    }

    private fun CharReader.set(text: String) = set(text.toCharArray(), text.length)
}

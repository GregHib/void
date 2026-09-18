package world.gregs.voidps.engine.client.command

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConsoleLineTest {

    private val written = StringBuilder()
    private var width = 80
    private val line = ConsoleLine("› ", written::append) { width }

    @BeforeEach
    fun setup() {
        ConsoleCommands.clear()
    }

    @AfterEach
    fun teardown() {
        ConsoleCommands.clear()
    }

    @Test
    fun `Typed keys are drawn back out`() {
        type("hi")

        assertEquals("\r[J› h\r[3C\r[J› hi\r[4C", written.toString())
    }

    @Test
    fun `Enter submits what was typed`() {
        type("players")

        val key = line.key(ENTER)

        assertEquals(ConsoleLine.Key.Submit("players"), key)
        assertTrue(written.endsWith("› players\r\n"))
    }

    @Test
    fun `Backspace removes the character before the cursor`() {
        type("hit")

        line.key(BACKSPACE)

        assertEquals(ConsoleLine.Key.Submit("hi"), line.key(ENTER))
    }

    @Test
    fun `Backspace at the start does nothing`() {
        line.key(BACKSPACE)

        assertEquals(ConsoleLine.Key.Submit(""), line.key(ENTER))
    }

    @Test
    fun `Left moves the cursor so typing inserts`() {
        type("ab")
        left()

        type("c")

        assertEquals(ConsoleLine.Key.Submit("acb"), line.key(ENTER))
    }

    @Test
    fun `Delete removes the character under the cursor`() {
        type("abc")
        left()

        delete()

        assertEquals(ConsoleLine.Key.Submit("ab"), line.key(ENTER))
    }

    @Test
    fun `Home and end jump to either side of the line`() {
        type("bc")
        home()
        type("a")
        end()

        type("d")

        assertEquals(ConsoleLine.Key.Submit("abcd"), line.key(ENTER))
    }

    @Test
    fun `Up recalls the last line entered`() {
        type("players")
        line.key(ENTER)

        up()

        assertEquals(ConsoleLine.Key.Submit("players"), line.key(ENTER))
    }

    @Test
    fun `Up walks back through history and down returns`() {
        type("players")
        line.key(ENTER)
        type("save")
        line.key(ENTER)

        up()
        up()
        down()

        assertEquals(ConsoleLine.Key.Submit("save"), line.key(ENTER))
    }

    @Test
    fun `Down past the newest line clears it`() {
        type("players")
        line.key(ENTER)
        up()

        down()

        assertEquals(ConsoleLine.Key.Submit(""), line.key(ENTER))
    }

    @Test
    fun `Repeating a line doesn't repeat it in history`() {
        type("players")
        line.key(ENTER)
        type("players")
        line.key(ENTER)

        up()
        up()

        assertEquals(ConsoleLine.Key.Submit("players"), line.key(ENTER))
    }

    @Test
    fun `Blank lines aren't kept in history`() {
        line.key(ENTER)

        up()

        assertEquals(ConsoleLine.Key.Submit(""), line.key(ENTER))
    }

    @Test
    fun `History is capped so it can't grow forever`() {
        for (index in 0..120) {
            type("command$index")
            line.key(ENTER)
        }

        // Walking further back than the cap stops at the oldest kept line
        repeat(200) { up() }

        assertEquals(ConsoleLine.Key.Submit("command21"), line.key(ENTER))
    }

    @Test
    fun `Ctrl D on an empty line is the end of the input`() {
        assertEquals(ConsoleLine.Key.EndOfFile, line.key(END_OF_FILE))
    }

    @Test
    fun `Ctrl D with something typed isn't`() {
        type("players")

        assertEquals(ConsoleLine.Key.None, line.key(END_OF_FILE))
    }

    @Test
    fun `Ctrl L asks for a clear`() {
        assertEquals(ConsoleLine.Key.Clear, line.key(FORM_FEED))
    }

    @Test
    fun `Multi byte characters are held until complete`() {
        // 'ø' is two bytes
        line.key(0xc3)
        line.key(0xb8)

        assertEquals(ConsoleLine.Key.Submit("ø"), line.key(ENTER))
    }

    @Test
    fun `Log lines are printed above what's been typed`() {
        type("play")
        written.clear()

        line.printAbove("INFO Tick 54 took 103ms")

        assertEquals("\r[JINFO Tick 54 took 103ms\r\n› play\r[6C", written.toString())
    }

    @Test
    fun `A wrapped input line is erased from its first row`() {
        width = 8
        type("abcdefgh")
        written.clear()

        line.printAbove("log")

        // Prompt plus input fills more than one row so the erase starts a row above
        assertTrue(written.startsWith("[1A\r[Jlog\r\n"))
    }

    @Test
    fun `Tab completes a command name`() {
        ConsoleCommands.register("players") { emptyList() }
        type("pl")

        line.key(TAB)

        assertEquals(ConsoleLine.Key.Submit("players "), line.key(ENTER))
    }

    @Test
    fun `Tab completes as far as the matches agree`() {
        ConsoleCommands.register("save") { emptyList() }
        ConsoleCommands.register("say") { emptyList() }
        type("s")

        line.key(TAB)

        assertEquals(ConsoleLine.Key.Submit("sa"), line.key(ENTER))
        assertTrue(written.contains("  save"))
        assertTrue(written.contains("  say"))
    }

    @Test
    fun `Tab completes an argument and quotes spaces`() {
        ConsoleCommands.register("kick", stringArg("player-name", autofill = setOf("harley gilpin"))) { emptyList() }
        type("kick har")

        line.key(TAB)

        assertEquals(ConsoleLine.Key.Submit("kick \"harley gilpin\" "), line.key(ENTER))
    }

    @Test
    fun `Tab with nothing to complete leaves the line alone`() {
        type("bogus ")

        line.key(TAB)

        assertEquals(ConsoleLine.Key.Submit("bogus "), line.key(ENTER))
    }

    private fun type(text: String) {
        for (character in text) {
            line.key(character.code)
        }
    }

    private fun left() = escape('D')

    private fun up() = escape('A')

    private fun down() = escape('B')

    private fun home() = escape('H')

    private fun end() = escape('F')

    private fun delete() {
        line.key(ESCAPE)
        line.key('['.code)
        line.key('3'.code)
        line.key('~'.code)
    }

    private fun escape(final: Char) {
        line.key(ESCAPE)
        line.key('['.code)
        line.key(final.code)
    }

    private companion object {
        private const val ENTER = 13
        private const val BACKSPACE = 127
        private const val TAB = 9
        private const val END_OF_FILE = 4
        private const val FORM_FEED = 12
        private const val ESCAPE = 27
    }
}

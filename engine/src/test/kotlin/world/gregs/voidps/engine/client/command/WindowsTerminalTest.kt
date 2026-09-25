package world.gregs.voidps.engine.client.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * The console api itself is native and windows only, so what's tested here is everything either
 * side of it: which mode gets asked for, what happens when it's refused, and that nothing throws
 * where there's no api to call at all.
 */
class WindowsTerminalTest {

    private val output = ByteArrayOutputStream()

    @Test
    fun `Keys are asked for a at a time without giving up interrupts`() {
        // Everything windows turns on for a console by default
        val mode = WindowsTerminal.keyMode(PROCESSED or LINE or ECHO or MOUSE or QUICK_EDIT or WINDOW)

        assertEquals(PROCESSED, mode and PROCESSED, "Ctrl + C has to keep stopping the server")
        assertEquals(VIRTUAL_TERMINAL_INPUT, mode and VIRTUAL_TERMINAL_INPUT)
        assertEquals(EXTENDED, mode and EXTENDED, "quick edit is only taken notice of with this set")
        assertEquals(0, mode and LINE)
        assertEquals(0, mode and ECHO)
        assertEquals(0, mode and QUICK_EDIT, "a click would otherwise block everything written")
        assertEquals(0, mode and MOUSE)
        assertEquals(0, mode and WINDOW)
    }

    @Test
    fun `Anything else the console was set to is left alone`() {
        val insert = 0x0020

        assertEquals(insert, WindowsTerminal.keyMode(insert) and insert)
    }

    @Test
    fun `Escapes are drawn rather than printed, and lines still wrap`() {
        val mode = WindowsTerminal.drawMode(PROCESSED)

        assertEquals(VIRTUAL_TERMINAL_PROCESSING, mode and VIRTUAL_TERMINAL_PROCESSING)
        assertEquals(WRAP_AT_END, mode and WRAP_AT_END)
        assertEquals(0, mode and NEWLINE_AUTO_RETURN, "the input line wraps on the cursor moving on")
    }

    @Test
    fun `Taking the console asks for both modes and switches to utf-8`() {
        val console = FakeConsoleApi()
        val terminal = terminal(console)

        assertTrue(terminal.start())
        assertTrue(terminal.interactive)
        assertEquals(WindowsTerminal.keyMode(0x00F7), console.left(1L))
        assertEquals(WindowsTerminal.drawMode(0x0003), console.left(2L))
        assertTrue(console.calls.contains("page 65001"), "the prompt and gutter are utf-8")
    }

    @Test
    fun `A console which won't be drawn into is handed back as it was`() {
        val console = FakeConsoleApi(refuse = setOf(2L))
        val terminal = terminal(console)

        assertFalse(terminal.start())
        assertFalse(terminal.interactive)
        assertEquals(0x00F7, console.left(1L), "the keys it was reading with are put back")
    }

    @Test
    fun `A console which won't give up its keys isn't taken`() {
        val console = FakeConsoleApi(refuse = setOf(1L))

        assertFalse(terminal(console).start())
    }

    @Test
    fun `Everything is put back once, however many times it's asked for`() {
        val console = FakeConsoleApi()
        val terminal = terminal(console)
        terminal.start()
        console.calls.clear()

        terminal.restore()
        terminal.restore()

        assertEquals(listOf("mode 2 3", "mode 1 247", "page 437"), console.calls)
        assertFalse(terminal.interactive)
    }

    @Test
    fun `Restoring a console which was never taken does nothing`() {
        val console = FakeConsoleApi()

        terminal(console).restore()

        assertTrue(console.calls.isEmpty())
        assertEquals("", output.toString())
    }

    @Test
    fun `Width comes from the buffer the line wraps in`() {
        val terminal = terminal(FakeConsoleApi())

        terminal.refreshWidth()

        assertEquals(120, terminal.width)
    }

    @Test
    fun `A width which can't be read is left as it was`() {
        val terminal = terminal(FakeConsoleApi(columns = 0))

        terminal.refreshWidth()

        assertEquals(80, terminal.width)
    }

    @Test
    fun `Nothing is attached without a console on both ends`() {
        val console = FakeConsoleApi(handles = mapOf(FakeConsoleApi.INPUT to 1L, FakeConsoleApi.OUTPUT to null))

        assertFalse(terminal(console).attached)
    }

    @Test
    fun `Resizes aren't watched, so the width is asked for as commands are entered`() {
        assertFalse(terminal(FakeConsoleApi()).watching)
    }

    @Test
    fun `Without the library to call there's nothing to take`() {
        // Which is every run of this test, and every windows jvm which can't load it
        val terminal = WindowsTerminal(ByteArrayInputStream(ByteArray(0)), PrintStream(output))

        assertFalse(terminal.start())
        assertFalse(terminal.attached, "no console under a test runner either way")
        assertFalse(terminal.interactive)
    }

    @Test
    fun `Windows is driven through the console api and anything else with stty`() {
        assertInstanceOf(WindowsTerminal::class.java, consoleTerminal("Windows 11"))
        assertInstanceOf(SystemTerminal::class.java, consoleTerminal("Linux"))
    }

    private fun terminal(console: ConsoleApi) = WindowsTerminal(ByteArrayInputStream(ByteArray(0)), PrintStream(output), console)

    private companion object {
        private const val PROCESSED = 0x0001
        private const val LINE = 0x0002
        private const val ECHO = 0x0004
        private const val WINDOW = 0x0008
        private const val MOUSE = 0x0010
        private const val QUICK_EDIT = 0x0040
        private const val EXTENDED = 0x0080
        private const val VIRTUAL_TERMINAL_INPUT = 0x0200
        private const val WRAP_AT_END = 0x0002
        private const val VIRTUAL_TERMINAL_PROCESSING = 0x0004
        private const val NEWLINE_AUTO_RETURN = 0x0008
    }
}

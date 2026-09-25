package world.gregs.voidps.engine.client.command

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration

class ConsoleReaderTest {

    private val submitted = mutableListOf<String>()

    @AfterEach
    fun teardown() {
        ConsoleCommands.clear()
    }

    @Test
    fun `Lines are submitted to be run on the game thread`() {
        ConsoleReader(FakeTerminal("players\nkick bob\n"), submitted::add).run()

        assertEquals(listOf("players", "kick bob"), submitted)
    }

    @Test
    fun `Blank lines are ignored`() {
        ConsoleReader(FakeTerminal("\n   \nsave\n"), submitted::add).run()

        assertEquals(listOf("save"), submitted)
    }

    @Test
    fun `Typed keys are submitted once entered`() {
        val terminal = FakeTerminal("players\r", interactive = true)

        ConsoleReader(terminal, submitted::add).run()

        assertEquals(listOf("players"), submitted)
        assertTrue(terminal.written.contains("› players"))
    }

    @Test
    fun `Clear is handled by the reader rather than queued`() {
        val replies = mutableListOf<String>()
        ConsoleCommands.output = replies::add

        ConsoleReader(FakeTerminal("clear\nsave\n"), submitted::add).run()

        assertEquals(listOf("save"), submitted)
        assertEquals(listOf("Unable to clear without a terminal."), replies)
    }

    @Test
    fun `Clear wipes the screen and scrollback in a terminal`() {
        val terminal = FakeTerminal("clear\r", interactive = true)

        ConsoleReader(terminal, submitted::add).run()

        assertTrue(submitted.isEmpty())
        assertTrue(terminal.written.contains("[H[2J[3J"))
    }

    @Test
    fun `A tab in an entered line completes it instead of running it`() {
        val replies = mutableListOf<String>()
        ConsoleCommands.output = replies::add
        ConsoleCommands.register("players") { emptyList() }

        ConsoleReader(FakeTerminal("play\t\n"), submitted::add).run()

        assertEquals(listOf("players"), replies)
        assertTrue(submitted.isEmpty())
    }

    @Test
    fun `A tab completes an argument mid line`() {
        val replies = mutableListOf<String>()
        ConsoleCommands.output = replies::add
        ConsoleCommands.register("kick", stringArg("player-name", autofill = setOf("harley gilpin"))) { emptyList() }

        ConsoleReader(FakeTerminal("kick har\t\n"), submitted::add).run()

        assertEquals(listOf("kick harley gilpin"), replies)
    }

    @Test
    fun `A tab with several matches lists them`() {
        val replies = mutableListOf<String>()
        ConsoleCommands.output = replies::add
        ConsoleCommands.register("save") { emptyList() }
        ConsoleCommands.register("say") { emptyList() }

        ConsoleReader(FakeTerminal("sa\t\n"), submitted::add).run()

        assertEquals(listOf("Matches:", "  save", "  say"), replies)
    }

    @Test
    fun `Typing on past a tab runs the line without it`() {
        ConsoleReader(FakeTerminal("play\ters\n"), submitted::add).run()

        assertEquals(listOf("players"), submitted)
    }

    @Test
    fun `A tab with nothing to complete says so`() {
        val replies = mutableListOf<String>()
        ConsoleCommands.output = replies::add
        ConsoleCommands.register("players") { emptyList() }

        ConsoleReader(FakeTerminal("zzz\t\n"), submitted::add).run()

        assertEquals(listOf("No completions for 'zzz'."), replies)
    }

    @Test
    fun `Closed input stops the reader instead of spinning`() {
        // Reading returns -1 when there's no stdin e.g. `docker run` without -i
        assertTimeoutPreemptively(Duration.ofSeconds(5)) {
            ConsoleReader(FakeTerminal(""), submitted::add).run()
        }

        assertTrue(submitted.isEmpty())
    }

    @Test
    fun `Closed input stops an interactive reader instead of spinning`() {
        assertTimeoutPreemptively(Duration.ofSeconds(5)) {
            ConsoleReader(FakeTerminal("", interactive = true), submitted::add).run()
        }

        assertTrue(submitted.isEmpty())
    }

    @Test
    fun `Input ending without a new line still runs the last command`() {
        assertTimeoutPreemptively(Duration.ofSeconds(5)) {
            ConsoleReader(FakeTerminal("players"), submitted::add).run()
        }

        assertEquals(listOf("players"), submitted)
    }

    @Test
    fun `End of file key stops an interactive reader`() {
        // Ctrl + D on an empty line
        ConsoleReader(FakeTerminal("", interactive = true), submitted::add).run()

        assertTrue(submitted.isEmpty())
    }

    @Test
    fun `Terminal is handed back however the reader stops`() {
        val terminal = FakeTerminal("players\r", interactive = true)

        ConsoleReader(terminal, submitted::add).run()

        assertTrue(terminal.restored)
    }

    @Test
    fun `The console doesn't run when there's no terminal to run it in`() {
        val out = System.out

        val thread = ConsoleReader(FakeTerminal("players\n"), submitted::add).start()

        assertNull(thread)
        assertTrue(submitted.isEmpty())
        assertEquals(out, System.out)
    }

    @Test
    fun `A terminal which can't be taken over is still read a line at a time`() {
        // Windows has a terminal but no stty to put it into character mode
        val terminal = FakeTerminal("players\n", interactive = false, attached = true)

        ConsoleReader(terminal, submitted::add).start()?.join()

        assertEquals(listOf("players"), submitted)
    }

    @Test
    fun `Piped input is read when it's asked for`() {
        ConsoleReader(FakeTerminal("players\n"), submitted::add, setting = "true").start()?.join()

        assertEquals(listOf("players"), submitted)
    }

    @Test
    fun `Output isn't redirected for a terminal which can't be drawn into`() {
        val out = System.out

        ConsoleReader(FakeTerminal("players\n"), submitted::add, setting = "true").start()?.join()

        assertEquals(out, System.out)
    }

    @Test
    fun `The console can be turned off entirely`() {
        val thread = ConsoleReader(FakeTerminal("players\n", interactive = true), submitted::add, setting = "false").start()

        assertNull(thread)
        assertTrue(submitted.isEmpty())
    }

    @Test
    fun `Width is refreshed when resizes aren't watched`() {
        val terminal = FakeTerminal("players\r", interactive = true)

        ConsoleReader(terminal, submitted::add).run()

        assertFalse(terminal.watching)
        assertEquals(1, terminal.refreshes)
    }
}

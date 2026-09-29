package world.gregs.voidps.engine.client.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.PrintStream

class ConsoleOutputTest {

    private val printed = mutableListOf<String>()
    private val output = PrintStream(ConsoleOutput(printed::add), true)

    @Test
    fun `Each line logged is printed above the input line`() {
        output.print("Tick 54 took 103ms\n")

        assertEquals(listOf("Tick 54 took 103ms"), printed)
    }

    @Test
    fun `Partial lines are buffered until complete`() {
        output.print("Tick 54 ")

        assertEquals(emptyList<String>(), printed)

        output.print("took 103ms\n")

        assertEquals(listOf("Tick 54 took 103ms"), printed)
    }

    @Test
    fun `Multiple lines in one write are printed separately`() {
        output.print("First\nSecond\n")

        assertEquals(listOf("First", "Second"), printed)
    }

    @Test
    fun `Installing redirects both output streams and uninstalling restores them`() {
        val out = System.out
        val error = System.err
        val printed = mutableListOf<String>()

        ConsoleOutput.install(printed::add)
        System.out.print("logged\n")
        System.err.print("stack trace\n")

        assertEquals(listOf("logged", "stack trace"), printed)

        ConsoleOutput.uninstall()

        assertEquals(out, System.out)
        assertEquals(error, System.err)
    }

    @Test
    fun `Installing twice keeps the original streams`() {
        val out = System.out
        val error = System.err

        ConsoleOutput.install {}
        ConsoleOutput.install {}
        ConsoleOutput.uninstall()

        assertEquals(out, System.out)
        assertEquals(error, System.err)
    }

    @Test
    fun `Windows line endings are trimmed`() {
        output.print("Tick 54 took 103ms\r\n")

        assertEquals(listOf("Tick 54 took 103ms"), printed)
    }
}

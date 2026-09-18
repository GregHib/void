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
    fun `Windows line endings are trimmed`() {
        output.print("Tick 54 took 103ms\r\n")

        assertEquals(listOf("Tick 54 took 103ms"), printed)
    }
}

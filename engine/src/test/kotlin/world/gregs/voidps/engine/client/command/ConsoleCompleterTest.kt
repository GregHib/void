package world.gregs.voidps.engine.client.command

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConsoleCompleterTest {

    @BeforeEach
    fun setup() {
        ConsoleCommands.clear()
    }

    @AfterEach
    fun teardown() {
        ConsoleCommands.clear()
    }

    @Test
    fun `Command names are completed`() {
        ConsoleCommands.register("players") { emptyList() }
        ConsoleCommands.register("save") { emptyList() }

        assertEquals(listOf("players"), complete("pl"))
    }

    @Test
    fun `Every command is offered for an empty line`() {
        ConsoleCommands.register("players") { emptyList() }
        ConsoleCommands.register("save") { emptyList() }

        assertEquals(listOf("players", "save"), complete(""))
    }

    @Test
    fun `Argument values are completed from the commands autofill`() {
        ConsoleCommands.register("kick", stringArg("player-name", autofill = setOf("harley", "greg"))) { emptyList() }

        assertEquals(listOf("harley"), complete("kick h"))
    }

    @Test
    fun `Every argument value is offered once the command is typed`() {
        ConsoleCommands.register("kick", stringArg("player-name", autofill = setOf("harley", "greg"))) { emptyList() }

        assertEquals(listOf("harley", "greg"), complete("kick "))
    }

    @Test
    fun `Arguments without autofill have nothing to complete`() {
        ConsoleCommands.register("announce", stringArg("message")) { emptyList() }

        assertEquals(emptyList<String>(), complete("announce h"))
    }

    @Test
    fun `Unknown command has nothing to complete`() {
        ConsoleCommands.register("players") { emptyList() }

        assertEquals(emptyList<String>(), complete("bogus a"))
    }

    @Test
    fun `The word being completed is where the replacement starts`() {
        ConsoleCommands.register("kick", stringArg("player-name", autofill = setOf("harley"))) { emptyList() }

        assertEquals("kick ".length, ConsoleCompleter.complete("kick har", "kick har".length).start)
    }

    private fun complete(line: String): List<String> = ConsoleCompleter.complete(line, line.length).candidates
}

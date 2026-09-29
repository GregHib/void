package world.gregs.voidps.engine.client.command

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConsoleCommandsTest {

    private val calls = mutableListOf<List<String>>()

    @BeforeEach
    fun setup() {
        ConsoleCommands.clear()
        calls.clear()
    }

    @AfterEach
    fun teardown() {
        ConsoleCommands.clear()
    }

    @Test
    fun `Split a line into command and arguments`() {
        assertEquals(listOf("kick", "player"), ConsoleCommands.parse("  kick   player "))
    }

    @Test
    fun `Quoted arguments are kept together`() {
        assertEquals(listOf("announce", "Server restarting soon"), ConsoleCommands.parse("""announce "Server restarting soon""""))
    }

    @Test
    fun `Command name is case insensitive`() {
        register("players")

        assertEquals(listOf("ran"), ConsoleCommands.execute("PLAYERS"))
        assertEquals(1, calls.size)
    }

    @Test
    fun `Arguments are passed to the handler`() {
        register("kick", stringArg("player-name"))

        ConsoleCommands.execute("""kick "harley gilpin"""")

        assertEquals(listOf(listOf("harley gilpin")), calls)
    }

    @Test
    fun `Unknown command lists the available commands`() {
        register("players")
        register("save")

        val output = ConsoleCommands.execute("wildlyoff")

        assertEquals("Unknown command 'wildlyoff'.", output[0])
        assertTrue(output.any { it.contains("players") })
        assertTrue(output.any { it.contains("save") })
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `Missing required argument returns the usage`() {
        register("kick", stringArg("player-name"))

        assertEquals(listOf("Usage: kick (player-name:string)"), ConsoleCommands.execute("kick"))
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `Too many arguments returns the usage`() {
        register("kick", stringArg("player-name"))

        assertEquals(listOf("Usage: kick (player-name:string)"), ConsoleCommands.execute("kick one two"))
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `Rest arguments take the remainder of the line unquoted`() {
        ConsoleCommands.register("announce", stringArg("message"), rest = true) { args ->
            calls.add(args)
            listOf("ran")
        }

        ConsoleCommands.execute("announce server restarting soon")

        assertEquals(listOf(listOf("server restarting soon")), calls)
    }

    @Test
    fun `Rest arguments only take what's past the earlier ones`() {
        ConsoleCommands.register("mute", stringArg("hours"), stringArg("reason"), rest = true) { args ->
            calls.add(args)
            listOf("ran")
        }

        ConsoleCommands.execute("mute 5 being rude in chat")

        assertEquals(listOf(listOf("5", "being rude in chat")), calls)
    }

    @Test
    fun `Quoting still works for a rest argument`() {
        ConsoleCommands.register("announce", stringArg("message"), rest = true) { args ->
            calls.add(args)
            listOf("ran")
        }

        ConsoleCommands.execute("""announce "server restarting soon"""")

        assertEquals(listOf(listOf("server restarting soon")), calls)
    }

    @Test
    fun `Commands without a rest argument still reject extra arguments`() {
        register("players")

        assertEquals(listOf("Usage: players"), ConsoleCommands.execute("players and more"))
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `Aliases run the command they point at`() {
        register("shutdown")
        ConsoleCommands.alias("shutdown", "quit")

        assertEquals(listOf("ran"), ConsoleCommands.execute("quit"))
        assertEquals(1, calls.size)
    }

    @Test
    fun `Half a command name suggests the whole one`() {
        register("players")
        register("save")

        assertEquals(listOf("Unknown command 'play'. Did you mean 'players'?"), ConsoleCommands.execute("play"))
    }

    @Test
    fun `Half a name matching several commands lists them`() {
        register("save")
        register("say")

        val output = ConsoleCommands.execute("sa")

        assertEquals(listOf("Unknown command 'sa'.", "Matches:", "  save", "  say"), output)
    }

    @Test
    fun `A near miss suggests the command meant`() {
        register("players")
        register("save")

        assertEquals(listOf("Unknown command 'playrs'. Did you mean 'players'?"), ConsoleCommands.execute("playrs"))
    }

    @Test
    fun `Nothing close lists the commands instead`() {
        register("players")

        val output = ConsoleCommands.execute("absolutely-not-a-command")

        assertEquals("Unknown command 'absolutely-not-a-command'.", output[0])
        assertTrue(output.any { it.contains("players") })
    }

    @Test
    fun `Help describes a single command`() {
        ConsoleCommands.register("kick", stringArg("player-name", desc = "Display name of the player"), desc = "Disconnect a player") { emptyList() }

        assertEquals(
            listOf(
                "kick (player-name:string)",
                "  Disconnect a player",
                "  (player-name:string) Display name of the player",
            ),
            ConsoleCommands.help("kick"),
        )
    }

    @Test
    fun `Help for an unknown command suggests instead`() {
        register("players")

        assertEquals(listOf("Unknown command 'playrs'. Did you mean 'players'?"), ConsoleCommands.help("playrs"))
    }

    @Test
    fun `Optional arguments can be left out`() {
        register("shutdown", intArg("seconds", optional = true))

        assertEquals(listOf("ran"), ConsoleCommands.execute("shutdown"))
        assertEquals(listOf(emptyList<String>()), calls)
    }

    @Test
    fun `Argument of the wrong type returns the usage`() {
        register("shutdown", intArg("seconds", optional = true))

        val output = ConsoleCommands.execute("shutdown soon")

        assertEquals("Invalid value 'soon' for argument [seconds:int].", output[0])
        assertEquals("Usage: shutdown [seconds:int]", output[1])
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `Blank line does nothing`() {
        register("players")

        assertTrue(ConsoleCommands.execute("   ").isEmpty())
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `Exception in a command is caught`() {
        ConsoleCommands.register("boom") { throw IllegalStateException("broken") }

        assertEquals(listOf("Error in command 'boom': broken"), ConsoleCommands.execute("boom"))
    }

    @Test
    fun `Submitted lines are executed on run`() {
        register("players")

        ConsoleCommands.submit("players")
        ConsoleCommands.submit("players")
        assertTrue(calls.isEmpty())

        ConsoleCommands.run()

        assertEquals(2, calls.size)
    }

    @Test
    fun `Replies are written to the output`() {
        val replies = mutableListOf<String>()
        ConsoleCommands.output = replies::add
        register("players")

        ConsoleCommands.submit("players")
        ConsoleCommands.run()

        assertEquals(listOf("ran"), replies)
    }

    private fun register(name: String, vararg args: CommandArgument) {
        ConsoleCommands.register(name, *args) { arguments ->
            calls.add(arguments)
            listOf("ran")
        }
    }
}

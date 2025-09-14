package world.gregs.voidps.engine.client.ui.event

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.engine.containsMessage
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.messages

class CommandsTest {

    private lateinit var commands: Commands
    private lateinit var player: Player
    private var calls = 0

    @BeforeEach
    fun setup() {
        startKoin {
            modules(module { single { FontDefinitions(arrayOf(FontDefinition(0, glyphWidths = ByteArray(200)))).apply { ids = mapOf("p12_full" to 0) } } })
        }
        commands = Commands()
        player = Player()
        calls = 0
    }

    @AfterEach
    fun shutdown() {
        stopKoin()
    }

    @Test
    fun `Find a command by name`() {
        register("name")

        val command = commands.find(player, "name")
        assertNotNull(command)
        assertEquals("name", command!!.name)
        assertEquals(PlayerRights.None, command.rights)
        assertEquals(1, command.signatures.size)
    }

    @Test
    fun `Command is invalid if no rights`() {
        register("name", rights = PlayerRights.Mod)

        val command = commands.find(player, "name")
        assertNull(command)
        assertTrue(player.containsMessage("Unauthorized command: name"))
    }

    @Test
    fun `Find a command by alias`() {
        register("name")
        commands.alias("name", "thing")

        val command = commands.find(player, "thing")
        assertNotNull(command)
        assertEquals("name", command!!.name)
    }

    @Test
    fun `Invalid command name`() {
        val command = commands.find(player, "thing")
        assertNull(command)
        assertTrue(player.containsMessage("Unknown command: thing."))
    }

    @Test
    fun `Suggest alternative command name`() {
        register("name")
        commands.suggest("name", "command")

        val command = commands.find(player, "command")
        assertNull(command)
        assertTrue(player.containsMessage("Did you mean 'name'?"))
    }

    @Test
    fun `Suggest similar command name`() {
        register("command")

        val command = commands.find(player, "commd")
        assertNull(command)
        assertTrue(player.containsMessage("Did you mean 'command'?"))
    }

    @Test
    fun `Autofill empty command`() {
        autofill("")
        assertTrue(player.messages.isEmpty())
    }

    @Test
    fun `Autofill complete command name`() {
        register("command")
        autofill("command")
        assertTrue(player.containsMessage("command"))
    }

    @Test
    fun `Autofill partial command name`() {
        register("command")
        autofill("com")
        assertTrue(player.containsMessage("command"))
    }

    @Test
    fun `Autofill invalid command name`() {
        autofill("com")
        assertTrue(player.messages.isEmpty())
    }

    @Test
    fun `Autofill multiple matching command names`() {
        register("command")
        register("commander")
        register("commandments")
        autofill("comm")
        assertTrue(player.containsMessage("command"))
        assertTrue(player.containsMessage("Multiple matches:"))
        assertTrue(player.containsMessage("command"))
        assertTrue(player.containsMessage("commander"))
        assertTrue(player.containsMessage("commandments"))
    }

    @Test
    fun `Autofill complete command argument`() {
        register("command", arg<String>("test", autofill = setOf("test")))
        autofill("command test ")
        assertTrue(player.messages.isEmpty())
    }

    @Test
    fun `Autofill partial command argument`() {
        register("command", arg<String>("test", autofill = setOf("test")))
        autofill("command t")
        assertTrue(player.containsMessage("command test"))
    }

    @Test
    fun `Autofill singular argument`() {
        register("command", arg<String>("test", autofill = setOf("test")))
        autofill("command ")
        assertTrue(player.containsMessage("command test"))
    }

    @Test
    fun `Autofill invalid command argument`() {
        register("command", arg<String>("test", autofill = setOf("test")))
        autofill("command inval")
        assertTrue(player.messages.isEmpty())
    }

    @Test
    fun `Don't autofill commands without rights`() {
        register("name", arg<String>("test", autofill = setOf("test")), rights = PlayerRights.Admin)
        autofill("na")
        assertTrue(player.messages.isEmpty())
    }

    @Test
    fun `Don't autofill arguments without rights`() {
        register("name", arg<String>("test", autofill = setOf("test")), rights = PlayerRights.Admin)
        autofill("name t")
        assertTrue(player.containsMessage("Unauthorized command: name"))
    }

    @Test
    fun `Autofill multiple matching command arguments`() {
        register("command", arg<String>("test", autofill = setOf("testable", "testing", "tester")))
        autofill("command te")
        assertTrue(player.containsMessage("command test"))
    }

    @Test
    fun `Call a command`() = runTest {
        register("name")
        commands.call(player, "name")
        assertEquals(1, calls)
    }

    @Test
    fun `Call a command with invalid argument`() = runTest {
        register("name", arg<String>("test"))
        commands.call(player, "name test two")
        assertEquals(0, calls)
        assertTrue(player.containsMessage("Unknown arguments for command 'name'"))
        assertTrue(player.containsMessage("Valid arguments: (test:string)"))
    }

    @Test
    fun `Call a command with invalid arguments`() = runTest {
        commands.register("name", listOf(CommandSignature { _, _ -> }, CommandSignature(listOf(arg<String>("test"))) { _, _ -> }))
        commands.call(player, "name one two")
        assertEquals(0, calls)
        assertTrue(player.containsMessage("Unknown arguments for command 'name'"))
        assertTrue(player.containsMessage("Usages:"))
        assertTrue(player.containsMessage("name"))
        assertTrue(player.containsMessage("name (test:string)"))
    }

    @Test
    fun `Call an invalid command`() = runTest {
        register("name")
        commands.call(player, "invalid arg")
        assertEquals(0, calls)
    }

    @Test
    fun `Call with exception gets caught`() {
        commands.register("name", listOf(CommandSignature { _, _ -> calls++; throw IllegalStateException("error") }))
        assertDoesNotThrow {
            runTest {
                commands.call(player, "name")
            }
        }
        assertEquals(1, calls)
    }

    fun register(name: String, vararg arguments: CommandArgument, rights: PlayerRights = PlayerRights.None, desc: String = "") {
        commands.register(name, listOf(CommandSignature(arguments.toList(), desc) { player, args -> calls++ }), rights)
    }

    fun autofill(command: String) {
        commands.autofill(player, command)
    }
}
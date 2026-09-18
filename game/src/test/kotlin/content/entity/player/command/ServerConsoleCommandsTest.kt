package content.entity.player.command

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.command.ConsoleCommands
import world.gregs.voidps.engine.entity.character.player.Players
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerConsoleCommandsTest : WorldTest() {

    @Test
    fun `Queued console commands are run on the game thread`() {
        createPlayer(name = "console listener")

        ConsoleCommands.submit("players")
        tick()

        assertEquals(1, Players.size)
    }

    @Test
    fun `Kick disconnects an online player`() {
        val player = createPlayer(name = "console kicked")

        ConsoleCommands.submit("kick console_kicked")
        tick(3)

        assertTrue(player["logged_out", false])
    }

    @Test
    fun `Kick of an offline player is reported`() {
        assertEquals(listOf("Unable to find player 'nobody' online."), ConsoleCommands.execute("kick nobody"))
    }
}

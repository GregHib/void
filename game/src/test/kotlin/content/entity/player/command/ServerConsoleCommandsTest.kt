package content.entity.player.command

import WorldTest
import containsMessage
import content.social.report.isMuted
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.command.ConsoleCommands
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `Kick takes a spaced name without quoting it`() {
        val player = createPlayer(name = "console spaced")

        ConsoleCommands.submit("kick console spaced")
        tick(3)

        assertTrue(player["logged_out", false])
    }

    @Test
    fun `Announce reaches everyone online`() {
        val player = createPlayer(name = "console listener two")

        ConsoleCommands.submit("announce server restarting soon")
        tick()

        assertTrue(player.containsMessage("server restarting soon"))
    }

    @Test
    fun `Save queues everyone online`() {
        createPlayer(name = "console saved")

        val output = ConsoleCommands.execute("save")

        assertEquals(listOf("Saving 1 player."), output)
    }

    @Test
    fun `Shutdown queues the world update`() {
        ConsoleCommands.submit("shutdown 5")
        tick()

        assertTrue(World.containsQueue("system_update"))

        // Left queued it would stop the server when the world is cleared down
        World.clearQueue("system_update")
        World.clearQueue("system_shutdown")
    }

    @Test
    fun `Mute stops a player chatting and unmute restores it`() {
        val player = createPlayer(name = "console muted")

        ConsoleCommands.submit("mute console_muted 3")
        tick()

        assertTrue(player.isMuted)

        ConsoleCommands.submit("unmute console_muted")
        tick()

        assertFalse(player.isMuted)
    }

    @Test
    fun `Uptime reports the tick count and players online`() {
        createPlayer(name = "console counted")

        val output = ConsoleCommands.execute("uptime")

        assertTrue(output.any { it.contains("ticks") })
        assertTrue(output.any { it.contains("1 player online") })
    }

    @Test
    fun `Reload of an unknown config type is reported`() {
        assertEquals(listOf("Unknown config type 'nonsense'."), ConsoleCommands.execute("reload nonsense"))
    }

    @Test
    fun `Aliases reach the command they point at`() {
        createPlayer(name = "console aliased")

        assertEquals(ConsoleCommands.execute("players"), ConsoleCommands.execute("list"))
    }
}

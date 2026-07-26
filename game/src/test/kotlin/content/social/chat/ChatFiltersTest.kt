package content.social.chat

import WorldTest
import interfaceOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.player.Player

internal class ChatFiltersTest : WorldTest() {

    private lateinit var player: Player

    @BeforeEach
    fun start() {
        runBlocking(Dispatchers.Default) {
            player = createPlayer(name = "player")
        }
    }

    @Test
    fun `Viewing the clan filter tab does not change the filter status`() {
        player.interfaceOption("filter_buttons", "clan", "View")

        assertEquals("on", player["clan_status", "on"])
    }

    @Test
    fun `Clan filter option is applied and persisted`() {
        player.interfaceOption("filter_buttons", "clan", "Friends")

        assertEquals("friends", player["clan_status", "on"])

        player.interfaceOption("filter_buttons", "clan", "On")

        assertEquals("on", player["clan_status", "on"])
    }

    @Test
    fun `Game filter option is applied`() {
        player.interfaceOption("filter_buttons", "game", "Filter")

        assertEquals("filter", player["game_status", "all"])

        player.interfaceOption("filter_buttons", "game", "View")

        assertEquals("filter", player["game_status", "all"])
    }

    @Test
    fun `Invalid persisted filter statuses are cleared on spawn`() {
        val poisoned = createPlayer(name = "poisoned")
        poisoned["clan_status"] = "view"
        poisoned["private_status"] = "view"

        runBlocking(Dispatchers.Default) {
            Spawn.player(poisoned)
        }

        assertEquals("on", poisoned["clan_status", "on"])
        assertEquals("on", poisoned["private_status", "on"])
    }
}

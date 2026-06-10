package content.social.report

import WorldTest
import interfaceOption
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.network.client.instruction.ChatPublic
import world.gregs.voidps.network.client.instruction.ReportAbuse
import world.gregs.voidps.network.login.protocol.encode.interfaceVisibility
import world.gregs.voidps.network.login.protocol.encode.message
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ReportAbuseTest : WorldTest() {

    @BeforeAll
    fun start() {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ChatEncoderKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.InterfaceEncodersKt")
    }

    @Test
    fun `Report a player`() = runTest {
        val (player, client) = createClient("snitch")
        createPlayer(name = "scammer")

        player.instructions.send(ReportAbuse("scammer", 15, 0, ""))
        tick()

        verify {
            client.message("Thank-you, your abuse report has been received.", ChatType.Game.id)
        }
    }

    @Test
    fun `Can't report yourself`() = runTest {
        val (player, client) = createClient("snitch")

        player.instructions.send(ReportAbuse("snitch", 15, 0, ""))
        tick()

        verify {
            client.message("You can't report yourself.", ChatType.Game.id)
        }
    }

    @Test
    fun `Can't report unknown player`() = runTest {
        val (player, client) = createClient("snitch")

        player.instructions.send(ReportAbuse("nobody", 15, 0, ""))
        tick()

        verify {
            client.message("Unable to find player 'nobody'.", ChatType.Game.id)
        }
    }

    @Test
    fun `Can't report twice within a minute`() = runTest {
        val (player, client) = createClient("snitch")
        createPlayer(name = "scammer")
        createPlayer(name = "macroer")

        player.instructions.send(ReportAbuse("scammer", 15, 0, ""))
        tick()
        player.instructions.send(ReportAbuse("macroer", 7, 0, ""))
        tick()

        verify {
            client.message("You can only submit one report per minute.", ChatType.Game.id)
        }
    }

    @Test
    fun `Invalid rule is ignored`() = runTest {
        val (player, client) = createClient("snitch")
        createPlayer(name = "scammer")

        player.instructions.send(ReportAbuse("scammer", 0, 0, ""))
        tick()

        verify(exactly = 0) {
            client.message("Thank-you, your abuse report has been received.", ChatType.Game.id)
        }
    }

    @Test
    fun `Moderator report with mute flag mutes the target`() = runTest {
        val player = createPlayer(name = "mod_steve")
        player.rights = PlayerRights.Mod
        val target = createPlayer(name = "offender")

        player.instructions.send(ReportAbuse("offender", 16, 1, ""))
        tick()

        assertTrue(target.isMuted)
    }

    @Test
    fun `Regular player report with mute flag doesn't mute`() = runTest {
        val player = createPlayer(name = "snitch")
        val target = createPlayer(name = "offender")

        player.instructions.send(ReportAbuse("offender", 16, 1, ""))
        tick()

        assertFalse(target.isMuted)
    }

    @Test
    fun `Online moderators are notified of reports`() = runTest {
        val player = createPlayer(name = "snitch")
        createPlayer(name = "scammer")
        val (mod, modClient) = createClient("mod_steve")
        mod.rights = PlayerRights.Mod

        player.instructions.send(ReportAbuse("scammer", 15, 0, ""))
        tick()

        verify {
            modClient.message("snitch reported scammer for Scamming.", ChatType.Game.id)
        }
    }

    @Test
    fun `Temporarily muted player can't chat`() = runTest {
        val (player, client) = createClient("offender")
        player.mute()

        player.instructions.send(ChatPublic("hello", 0))
        tick()

        verify {
            client.message("You are temporarily muted because of breaking a rule. This mute will remain for a", ChatType.Game.id)
            client.message("further 2 days. To prevent further mutes please read the rules.", ChatType.Game.id)
        }
    }

    @Test
    fun `Permanently muted player can't chat`() = runTest {
        val (player, client) = createClient("offender")
        player.permMute()

        player.instructions.send(ChatPublic("hello", 0))
        tick()

        verify {
            client.message("You are permanently muted because of breaking a rule.", ChatType.Game.id)
        }
    }

    @Test
    fun `Unmuted player can chat again`() = runTest {
        val (player, client) = createClient("offender")
        player.permMute()
        player.unmute()

        player.instructions.send(ChatPublic("hello", 0))
        tick()

        verify(exactly = 0) {
            client.message("You are permanently muted because of breaking a rule.", ChatType.Game.id)
        }
    }

    @Test
    fun `Report button opens the report abuse interface`() = runTest {
        val (player, _) = createClient("player")

        player.interfaceOption("filter_buttons", "report", "Report Abuse")
        tick()

        assertTrue(player.hasOpen("report_abuse"))
    }

    @Test
    fun `Chat line report abuse option opens the report abuse interface`() = runTest {
        val (player, _) = createClient("player")

        player.interfaceOption("chat_background", "chat_line7", optionIndex = 7)
        tick()

        assertTrue(player.hasOpen("report_abuse"))
    }

    @Test
    fun `Other chat line options don't open the report abuse interface`() = runTest {
        val (player, _) = createClient("player")

        player.interfaceOption("chat_background", "chat_line1", optionIndex = 9)
        tick()

        assertFalse(player.hasOpen("report_abuse"))
    }

    @Test
    fun `Private chat report abuse option opens the report abuse interface`() = runTest {
        val (player, _) = createClient("player")

        player.interfaceOption("private_chat", "line1", "Report Abuse")
        tick()

        assertTrue(player.hasOpen("report_abuse"))
    }

    @Test
    fun `Mute toggle is revealed for moderators`() = runTest {
        val (player, client) = createClient("mod_steve")
        player.rights = PlayerRights.Mod

        player.interfaceOption("filter_buttons", "report", "Report Abuse")
        tick()

        verify {
            client.interfaceVisibility(InterfaceDefinition.pack(594, 8), false)
            client.interfaceVisibility(InterfaceDefinition.pack(594, 52), false)
            client.interfaceVisibility(InterfaceDefinition.pack(594, 66), false)
        }
    }

    @Test
    fun `Mute toggle stays hidden for regular players`() = runTest {
        val (player, client) = createClient("player")

        player.interfaceOption("filter_buttons", "report", "Report Abuse")
        tick()

        verify(exactly = 0) {
            client.interfaceVisibility(InterfaceDefinition.pack(594, 8), false)
            client.interfaceVisibility(InterfaceDefinition.pack(594, 52), false)
            client.interfaceVisibility(InterfaceDefinition.pack(594, 66), false)
        }
    }
}

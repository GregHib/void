package world.gregs.voidps.world.community.friend

import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.network.client.instruction.*
import world.gregs.voidps.network.login.protocol.encode.*
import content.social.chat.privateStatus
import world.gregs.voidps.world.community.clan.ownClan
import world.gregs.voidps.world.script.WorldTest
import kotlin.collections.set
import kotlin.test.assertContains
import kotlin.test.assertTrue

internal class IgnoreTest : WorldTest() {

    @BeforeAll
    fun start() {
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.IgnoreEncoderKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ChatEncoderKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ClanEncoderKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.FriendsEncoderKt")
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
    }

    @Test
    fun `Add player to empty ignores list`() = runTest {
        val (player, client) = createClient("player")
        createPlayer("nuisance")

        player.instructions.send(IgnoreAdd("nuisance"))
        tick()

        verify {
            client.sendIgnoreList(listOf("nuisance" to ""))
        }
        assertContains(player.ignores, "nuisance")
    }

    @Test
    fun `Add player to full ignores list`() = runTest {
        val (player, client) = createClient("player")
        repeat(200) {
            player.ignores.add(it.toString())
        }
        createPlayer("nuisance")

        player.instructions.send(IgnoreAdd("nuisance"))
        tick()

        verify {
            client.message("Your ignore list is full. Max of 100.", ChatType.Game.id)
        }
    }

    @Test
    fun `Add non-existent player`() = runTest {
        val (player, client) = createClient("player")

        player.instructions.send(IgnoreAdd("random"))
        tick()

        verify {
            client.message("Unable to find player with name 'random'.", ChatType.Game.id)
        }
    }

    @Test
    fun `Re-add an existing player`() = runTest {
        val player = createPlayer("player")
        createPlayer("nuisance")
        player.ignores.add("nuisance")

        player.instructions.send(IgnoreAdd("nuisance"))
        tick()

        verify {
            player.message("nuisance is already on your ignores list.")
        }
    }

    @Test
    fun `Try to ignore a friend`() = runTest {
        val player = createPlayer("player")
        createPlayer("friend")
        player.friends["friend"] = ClanRank.Friend

        player.instructions.send(IgnoreAdd("friend"))
        tick()

        verify {
            player.message("Please remove friend from your ignores list first.")
        }
    }

    @Test
    fun `Delete ignore`() = runTest {
        val player = createPlayer("player")
        player.privateStatus = "on"
        val (nuisance, client) = createClient("nuisance")
        player.ignores.add("nuisance")
        nuisance.friends["player"] = ClanRank.Friend

        player.instructions.send(IgnoreDelete("nuisance"))
        tick()

        verify {
            client.sendFriendsList(listOf(Friend("player", "", world = 16, worldName = "World 16")))
        }
        assertTrue(player.ignores.isEmpty())
    }

    @Test
    fun `Chat messages not receive from ignored players`() = runTest {
        val (player, playerClient) = createClient("player")
        val (nuisance, nuisanceClient) = createClient("nuisance")
        player.ignores.add("nuisance")

        nuisance.instructions.send(ChatPublic("rude", 0))
        tick()

        verify {
            nuisanceClient.publicChat(any(), 0, 0, byteArrayOf(4, -81, -108, -64))
        }
        verify(exactly = 0) {
            playerClient.publicChat(any(), any(), any(), any())
        }
    }

    @Test
    fun `Private messages not receive from ignored players`() = runTest {
        val player = createPlayer("player")
        val (nuisance, client) = createClient("nuisance")
        player.ignores.add("nuisance")

        nuisance.instructions.send(ChatPrivate("player", "rude"))
        tick()

        verify {
            client.message("Unable to send message - player unavailable.", ChatType.Game.id)
        }
    }

    @Test
    fun `Clan messages not receive from ignored players`() = runTest {
        val (player, playerClient) = createClient("player")
        val (nuisance, nuisanceClient) = createClient("nuisance")
        player.ownClan?.name = "clan"
        player.instructions.send(ClanChatJoin("player"))
        nuisance.instructions.send(ClanChatJoin("player"))
        tick()
        player.ignores.add("nuisance")

        nuisance.instructions.send(ChatTypeChange(1))
        nuisance.instructions.send(ChatPublic("rude", 0))
        tick()

        verify {
            nuisanceClient.clanChat("nuisance", "clan", 0, byteArrayOf(4, -81, -108, -64))
        }
        verify(exactly = 0) {
            playerClient.clanChat(any(), any(), any(), any())
        }
    }
}
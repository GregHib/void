package content.social.clan

import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.secure.Huffman
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.instruction.*
import world.gregs.voidps.network.login.protocol.encode.clanChat
import world.gregs.voidps.network.login.protocol.encode.message
import WorldTest
import interfaceOption
import kotlin.collections.set
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class ClanTest : WorldTest() {

    lateinit var huffman: Huffman

    @BeforeEach
    fun start() {
        huffman = get()
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ChatEncoderKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.ClanEncoderKt")
    }

    @Test
    fun `Can't join another players deactivated clan chat`() = runTest {
        createPlayer("player")
        val (joiner, client) = createClient("joiner")

        joiner.instructions.send(ClanChatJoin("player"))
        tick()

        verify {
            client.message("The channel you tried to join does not exist.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Can't join non existent clan chat`() = runTest {
        val (player, client) = createClient("player")

        player.instructions.send(ClanChatJoin("non-existent"))
        tick()

        verify {
            client.message("The channel you tried to join does not exist.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Can't join another clan without the minimum join rank`() = runTest {
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        owner.ownClan?.joinRank = ClanRank.General
        val (joiner, client) = createClient("joiner")

        joiner.instructions.send(ClanChatJoin("player"))
        tick()

        verify {
            client.message("You do not have a high enough rank to join this clan chat channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Create and join your own clan chat`() = runTest {
        val (player, client) = createClient("player")
        val clan = player.ownClan!!

        player.instructions.send(ClanChatJoin("player"))
        tick()

        player.instructions.send(ClanChatJoin("player"))
        tick()

        assertEquals("player", clan.name)
        assertEquals(1, clan.members.size)
        verify {
            client.message("Now talking in clan channel player", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Deactivate your own clan chat`() = runTest {
        val player = createPlayer("player")
        val clan = player.ownClan!!
        clan.name = "clan"
        player.instructions.send(ClanChatJoin("player"))
        tick()

        player.interfaceOption("clan_chat", "settings", "Clan Setup")
        player.interfaceOption("clan_chat_setup", "name", "Disable")
        tick()

        assertEquals("", clan.name)
        assertEquals(0, clan.members.size)
    }

    @Test
    fun `Join another clan chat with correct rank`() = runTest {
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        owner.ownClan?.joinRank = ClanRank.Corporeal
        owner.friends["joiner"] = ClanRank.Corporeal
        val (joiner, client) = createClient("joiner")

        joiner.instructions.send(ClanChatJoin("player"))
        tick()

        verify {
            client.message("Now talking in clan channel clan", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Leave a clan chat`() = runTest {
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        val (joiner, client) = createClient("joiner")
        joiner.instructions.send(ClanChatJoin("player"))
        tick()

        joiner.instructions.send(ClanChatJoin(""))
        tick()

        verify {
            client.message("You have left the channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Change clan rank of a friend`() = runTest {
        createPlayer("friend")
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        owner.friends["friend"] = ClanRank.Friend

        owner.instructions.send(ClanChatRank("friend", 2))
        tick()

        assertEquals(ClanRank.Corporeal, owner.friends["friend"])
    }

    @Test
    fun `Deleting a friend kicks them if their rank is below minimum required`() = runTest {
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        owner.ownClan?.joinRank = ClanRank.Corporeal
        owner.friends["joiner"] = ClanRank.Corporeal
        val (joiner, client) = createClient("joiner")
        joiner.instructions.send(ClanChatJoin("player"))
        tick()

        owner.instructions.send(FriendDelete("joiner"))
        tick()

        verify {
            client.message("You have been kicked from the channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Kick a player from a clan chat`() = runTest {
        val owner = createPlayer("player")
        val clan = owner.ownClan!!
        clan.name = "clan"
        owner.friends["joiner"] = ClanRank.Friend
        val (joiner, client) = createClient("joiner")
        owner.instructions.send(ClanChatJoin("player"))
        joiner.instructions.send(ClanChatJoin("player"))
        tick()

        owner.instructions.send(ClanChatKick("joiner"))
        tick()

        assertFalse(clan.members.contains(joiner))
        verify {
            client.message("You have been kicked from the channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Admins can't be kicked`() = runTest {
        val (owner, client) = createClient("player")
        val clan = owner.ownClan!!
        clan.name = "clan"
        val admin = createPlayer("admin")
        admin.rights = PlayerRights.Admin
        admin.instructions.send(ClanChatJoin("player"))
        tick()

        owner.instructions.send(ClanChatKick("admin"))
        tick()

        assertTrue(clan.members.contains(admin))
        verify {
            client.message("You are not allowed to kick in this clan chat channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Admins can change another clans settings`() = runTest {
        val owner = createPlayer("player")
        val clan = owner.ownClan!!
        clan.name = "clan"
        val admin = createPlayer("admin")
        admin.rights = PlayerRights.Admin
        admin.instructions.send(ClanChatJoin("player"))
        tick()

        owner.interfaceOption("clan_chat", "settings", "Clan Setup")
        owner.interfaceOption("clan_chat_setup", "talk", "Sergeant+")

        assertEquals(ClanRank.Sergeant, clan.talkRank)
    }

    @Test
    fun `Players can't access another clans settings`() = runTest {
        val owner = createPlayer("player")
        val clan = owner.ownClan!!
        clan.name = "clan"
        val (member, client) = createClient("member")
        member.instructions.send(ClanChatJoin("player"))
        tick()

        member.interfaceOption("clan_chat", "settings", "Clan Setup")
        member.interfaceOption("clan_chat_setup", "talk", "Sergeant+")

        verify {
            client.message("Only the clan chat owner can do this.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Talk in clan chat`() = runTest {
        val owner = createPlayer("owner")
        val clan = owner.ownClan!!
        clan.name = "clan"
        clan.talkRank = ClanRank.Corporeal
        val (player, client) = createClient("player")
        owner.friends["player"] = ClanRank.Corporeal
        player.instructions.send(ClanChatJoin("owner"))

        player.instructions.send(ChatTypeChange(1))
        player.instructions.send(ChatPublic("Hi", 0))
        tick()

        verify {
            client.clanChat("player", "clan", 0, byteArrayOf(2, 13, -56))
        }
    }

    @Test
    fun `Can't talk without the minimum rank`() = runTest {
        val owner = createPlayer("owner")
        val clan = owner.ownClan!!
        val (player, client) = createClient("player")
        clan.name = "clan"
        clan.talkRank = ClanRank.General
        player.instructions.send(ClanChatJoin("owner"))

        player.instructions.send(ChatTypeChange(1))
        player.instructions.send(ChatPublic("Hi", 0))
        tick()

        verify {
            client.message("You are not allowed to talk in this clan chat.", ChatType.ClanChat.id)
        }
    }
}
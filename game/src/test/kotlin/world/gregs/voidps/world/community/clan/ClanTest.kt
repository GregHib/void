package world.gregs.voidps.world.community.clan

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.compress
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.network.encode.clanChat
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.*
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption
import kotlin.collections.set
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class ClanTest : WorldTest() {

    @BeforeEach
    fun start() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        mockkStatic("world.gregs.voidps.network.encode.ChatEncoderKt")
        mockkStatic("world.gregs.voidps.network.encode.ClanEncoderKt")
    }

    @Test
    fun `Can't join another players deactivated clan chat`() = runBlockingTest {
        createPlayer("player")
        val (joiner, client) = createClient("joiner")

        joiner.instructions.emit(ClanChatJoin("player"))
        tick()

        verify {
            client.message("The channel you tried to join does not exist.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Can't join non existent clan chat`() = runBlockingTest {
        val (player, client) = createClient("player")

        player.instructions.emit(ClanChatJoin("non-existent"))
        tick()

        verify {
            client.message("The channel you tried to join does not exist.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Can't join another clan without the minimum join rank`() = runBlockingTest {
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        owner.ownClan?.joinRank = Rank.General
        val (joiner, client) = createClient("joiner")

        joiner.instructions.emit(ClanChatJoin("player"))
        tick()

        verify {
            client.message("You do not have a high enough rank to join this clan chat channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Create and join your own clan chat`() = runBlockingTest {
        val (player, client) = createClient("player")
        val clan = player.ownClan!!

        player.instructions.emit(ClanChatJoin("player"))
        tick()

        player.instructions.emit(ClanChatJoin("player"))
        tick()

        assertEquals("player", clan.name)
        assertEquals(1, clan.members.size)
        verify {
            client.message("Now talking in clan channel player", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Deactivate your own clan chat`() = runBlockingTest {
        val player = createPlayer("player")
        val clan = player.ownClan!!
        clan.name = "clan"
        player.instructions.emit(ClanChatJoin("player"))
        tick()

        player.interfaceOption("clan_chat_setup", "name", "Disable")
        tick()

        assertEquals("", clan.name)
        assertEquals(0, clan.members.size)
    }

    @Test
    fun `Join another clan chat with correct rank`() = runBlockingTest {
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        owner.ownClan?.joinRank = Rank.Corporeal
        owner.friends["joiner"] = Rank.Corporeal
        val (joiner, client) = createClient("joiner")

        joiner.instructions.emit(ClanChatJoin("player"))
        tick()

        verify {
            client.message("Now talking in clan channel clan", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Leave a clan chat`() = runBlockingTest {
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        val (joiner, client) = createClient("joiner")
        joiner.instructions.emit(ClanChatJoin("player"))
        tick()

        joiner.instructions.emit(ClanChatJoin(""))
        tick()

        verify {
            client.message("You have left the channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Change clan rank of a friend`() = runBlockingTest {
        createPlayer("friend")
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        owner.friends["friend"] = Rank.Friend

        owner.instructions.emit(ClanChatRank("friend", 2))
        tick()

        assertEquals(Rank.Corporeal, owner.friends["friend"])
    }

    @Test
    fun `Deleting a friend kicks them if their rank is below minimum required`() = runBlockingTest {
        val owner = createPlayer("player")
        owner.ownClan?.name = "clan"
        owner.ownClan?.joinRank = Rank.Corporeal
        owner.friends["joiner"] = Rank.Corporeal
        val (joiner, client) = createClient("joiner")
        joiner.instructions.emit(ClanChatJoin("player"))
        tick()

        owner.instructions.emit(FriendDelete("joiner"))
        tick()

        verify {
            client.message("You have been kicked from the channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Kick a player from a clan chat`() = runBlockingTest {
        val owner = createPlayer("player")
        val clan = owner.ownClan!!
        clan.name = "clan"
        owner.friends["joiner"] = Rank.Friend
        val (joiner, client) = createClient("joiner")
        owner.instructions.emit(ClanChatJoin("player"))
        joiner.instructions.emit(ClanChatJoin("player"))
        tick()

        owner.instructions.emit(ClanChatKick("joiner"))
        tick()

        assertFalse(clan.members.contains(joiner))
        verify {
            client.message("You have been kicked from the channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Admins can't be kicked`() = runBlockingTest {
        val (owner, client) = createClient("player")
        val clan = owner.ownClan!!
        clan.name = "clan"
        val admin = createPlayer("admin")
        admin.rights = PlayerRights.Admin
        admin.instructions.emit(ClanChatJoin("player"))
        tick()

        owner.instructions.emit(ClanChatKick("admin"))
        tick()

        assertTrue(clan.members.contains(admin))
        verify {
            client.message("You are not allowed to kick in this clan chat channel.", ChatType.ClanChat.id)
        }
    }

    @Test
    fun `Admins can change another clans settings`() = runBlockingTest {
        val owner = createPlayer("player")
        val clan = owner.ownClan!!
        clan.name = "clan"
        val admin = createPlayer("admin")
        admin.rights = PlayerRights.Admin
        admin.instructions.emit(ClanChatJoin("player"))
        tick()

        owner.interfaceOption("clan_chat", "settings", "Clan Setup")
        owner.interfaceOption("clan_chat_setup", "talk", "Sergeant+")

        assertEquals(Rank.Sergeant, clan.talkRank)
    }

    @Test
    fun `Players can't access another clans settings`() = runBlockingTest {
        val owner = createPlayer("player")
        val clan = owner.ownClan!!
        clan.name = "clan"
        val (member, client) = createClient("member")
        member.instructions.emit(ClanChatJoin("player"))
        tick()

        member.interfaceOption("clan_chat", "settings", "Clan Setup")
        member.interfaceOption("clan_chat_setup", "talk", "Sergeant+")

        verify {
            client.message("Only the clan chat owner can do this.", ChatType.ClanChat.id)
        }
    }
    @Test
    fun `Talk in clan chat`() = runBlockingTest {
        val owner = createPlayer("owner")
        val clan = owner.ownClan!!
        clan.name = "clan"
        clan.talkRank = Rank.Corporeal
        val (player, client) = createClient("player")
        owner.friends["player"] = Rank.Corporeal
        player.instructions.emit(ClanChatJoin("owner"))
        every { "Hi".compress() } returns byteArrayOf(2, 13, -56)

        player.instructions.emit(ChatTypeChange(1))
        player.instructions.emit(ChatPublic("Hi", 0))
        tick()

        verify {
            client.clanChat("player", "clan", 0, byteArrayOf(2, 13, -56))
        }
    }

    @Test
    fun `Can't talk without the minimum rank`() = runBlockingTest {
        val owner = createPlayer("owner")
        val clan = owner.ownClan!!
        val (player, client) = createClient("player")
        clan.name = "clan"
        clan.talkRank = Rank.General
        player.instructions.emit(ClanChatJoin("owner"))

        player.instructions.emit(ChatTypeChange(1))
        player.instructions.emit(ChatPublic("Hi", 0))
        tick()

        verify {
            client.message("You are not allowed to talk in this clan chat.", ChatType.ClanChat.id)
        }
    }
}
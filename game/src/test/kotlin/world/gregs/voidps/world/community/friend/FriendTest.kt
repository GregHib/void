package world.gregs.voidps.world.community.friend

import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.network.encode.Friend
import world.gregs.voidps.network.encode.sendFriendsList
import world.gregs.voidps.network.instruct.FriendAdd
import world.gregs.voidps.network.instruct.FriendDelete
import world.gregs.voidps.world.script.WorldMock
import kotlin.test.assertContains
import kotlin.test.assertTrue

internal class FriendTest : WorldMock() {

    @BeforeEach
    fun start() {
        mockkStatic("world.gregs.voidps.engine.client.EncodeExtensionsKt")
        mockkStatic("world.gregs.voidps.network.encode.FriendsEncoderKt")
    }

    @Test
    fun `Add friend to empty friends list`() = runBlockingTest {
        val player = createPlayer("player")
        val (_, client) = createClient("friend")
        player["private_status"] = "friends"

        player.instructions.emit(FriendAdd("friend"))

        tick()

        verify {
            client.sendFriendsList(listOf(Friend("player", "", online = true)))
        }
        assertContains(player.friends, "friend")
    }

    @Test
    fun `Add friend to full friends list`() = runBlockingTest {
        val player = createPlayer("player")
        createPlayer("friend")
        repeat(200) {
            player.friends[it.toString()] = Rank.Friend
        }

        player.instructions.emit(FriendAdd("friend"))
        tick()

        verify {
            player.message("Your friends list is full. Max of 100 for free users, and 200 for members.")
        }
    }

    @Test
    fun `Add non-existent friend`() = runBlockingTest {
        val player = createPlayer("player")

        player.instructions.emit(FriendAdd("friend"))
        tick()

        verify {
            player.message("Unable to find player with name 'friend'.")
        }
    }

    @Test
    fun `Re-add an existing friend`() = runBlockingTest {
        val player = createPlayer("player")
        createPlayer("friend")
        player.friends["friend"] = Rank.Friend

        player.instructions.emit(FriendAdd("friend"))
        tick()

        verify {
            player.message("friend is already on your friends list.")
        }
    }

    @Test
    fun `Add ignored friend`() = runBlockingTest {
        val player = createPlayer("player")
        createPlayer("friend")
        player.ignores.add("friend")

        player.instructions.emit(FriendAdd("friend"))
        tick()

        verify {
            player.message("Please remove friend from your ignore list first.")
        }
    }

    @Test
    fun `Delete friend`() = runBlockingTest {
        val player = createPlayer("player")
        player["private_status"] = "friends"
        val (_, client) = createClient("friend")
        player.friends["friend"] = Rank.Friend

        player.instructions.emit(FriendDelete("friend"))
        tick()

        verify {
            client.sendFriendsList(listOf(Friend("player", "", online = false)))
        }
        assertTrue(player.friends.isEmpty())
    }
}
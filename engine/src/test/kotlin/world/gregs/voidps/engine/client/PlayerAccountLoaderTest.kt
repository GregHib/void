package world.gregs.voidps.engine.client

import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.NetworkQueue
import world.gregs.voidps.network.Response

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class PlayerAccountLoaderTest : KoinMock() {

    @RelaxedMockK
    private lateinit var queue: NetworkQueue

    @RelaxedMockK
    private lateinit var factory: PlayerFactory

    private lateinit var collisions: Collisions

    private lateinit var players: Players

    private lateinit var loader: PlayerAccountLoader

    @BeforeEach
    fun setup() {
        collisions = Collisions()
        players = Players()
        loader = spyk(PlayerAccountLoader(queue, factory, UnconfinedTestDispatcher(), collisions, players))
    }

    @Test
    fun `Invalid credentials`() = runTest {
        val client: Client = mockk(relaxed = true)
        val player: Player = mockk()
        every { player.passwordHash } returns ""
        every { factory.getOrElse("name", 2, any()) } returns player

        loader.load(client, "name", "pass", 2, 3)

        coVerify {
            client.disconnect(Response.INVALID_CREDENTIALS)
        }
    }

    @Test
    fun `Successful login`() = runTest {
        val client: Client = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { player.passwordHash } returns "\$2a\$10\$cPB7bqICWrOILrWnXuYNDu1EsbZal9AjxYMbmpMOtI1kwruazGiby"
        every { player.instructions } returns MutableSharedFlow()
        every { factory.getOrElse("name", 2, any()) } returns player

        loader.load(client, "name", "pass", 2, 3)

        coVerify {
            player.login(client, 3, collisions, players)
        }
    }

}
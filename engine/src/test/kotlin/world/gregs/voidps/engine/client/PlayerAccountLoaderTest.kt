package world.gregs.voidps.engine.client

import io.ktor.utils.io.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import world.gregs.voidps.network.Network
import java.math.BigInteger

@ExtendWith(MockKExtension::class)
internal class PlayerAccountLoaderTest {
    @MockK
    lateinit var network: Network

    @MockK
    lateinit var loader: Network.AccountLoader

    @RelaxedMockK
    lateinit var read: ByteReadChannel

    @RelaxedMockK
    lateinit var write: ByteWriteChannel

    @BeforeEach
    fun setup() {
        network = spyk(Network(123, BigInteger.ONE, BigInteger.TWO, loader))
    }

    /*@Test
    fun `Player already online`() = runBlockingTest {
        val client: Client = mockk()
        every { loginQueue.isOnline(any()) } returns true
        network.login(read, write, client, "bob", "axes", 0)
        coVerify {
            write.writeByte(5)
            write.close()
        }
    }

    @Test
    fun `World full`() = runBlockingTest {
        val client: Client = mockk(relaxed = true)
        every { loginQueue.isOnline(any()) } returns false
        every { loginQueue.login(any(), any()) } returns null
        every { loginQueue.logins(any()) } returns 0
        every { loginQueue.logout(any(), any(), any()) } just Runs
        network.login(read, write, client, "bob", "axes", 0)
        coVerify {
            write.writeByte(7)
            write.close()
        }
    }

    @Test
    fun `Login limit exceeded`() = runBlockingTest {
        val client: Client = mockk(relaxed = true)
        every { loginQueue.isOnline(any()) } returns false
        every { loginQueue.logins(any()) } returns 1
        network.login(read, write, client, "bob", "axes", 0)
        coVerify {
            write.writeByte(9)
            write.close()
        }
    }

    @Test
    fun `Load new account`() = runBlockingTest {
        val client: Client = mockk(relaxed = true)
        coEvery { loginQueue.await() } just Runs
        every { loginQueue.isOnline(any()) } returns false
        every { loginQueue.login(any(), any()) } returns 1
        every { loginQueue.logins(any()) } returns 0
        every { factory.load(any()) } returns null
        val player: Player = mockk(relaxed = true)
        every { factory.create(any(), any()) } returns player
        every { factory.initPlayer(player, 1) } just Runs
        coEvery { network.readPackets(client, player, read) } just Runs
        network.login(read, write, client, "bob", "axes", 0)
        coVerify {
            network.readPackets(client, player, read)
            factory.create("bob", "axes")
            factory.initPlayer(player, 1)
        }
    }

    @Test
    fun `Invalid credentials`() = runBlockingTest {
        val client: Client = mockk(relaxed = true)
        val player: Player = mockk()
        every { loginQueue.isOnline(any()) } returns false
        every { loginQueue.login(any(), any()) } returns 1
        every { loginQueue.logins(any()) } returns 0
        every { loginQueue.logout(any(), any(), any()) } just Runs
        every { factory.load(any()) } returns player
        every { player.passwordHash } returns ""
        network.login(read, write, client, "bob", "axes", 0)
        coVerify {
            write.writeByte(3)
            write.close()
        }
    }

    @Test
    fun `Successful login`() = runBlockingTest {
        val client: Client = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { loginQueue.isOnline(any()) } returns false
        every { loginQueue.login(any(), any()) } returns 1
        every { loginQueue.logins(any()) } returns 0
        every { loginQueue.logout(any(), any(), any()) } just Runs
        every { factory.load(any()) } returns player
        every { factory.initPlayer(player, 1) } just Runs
        coEvery { loginQueue.await() } just Runs
        coEvery { network.readPackets(client, player, read) } just Runs
        every { player.passwordHash } returns "\$2a\$10\$4ruibyyD1l.sGkzHLJvVn.keNd/jNWkoMNXaP0pVBdk8oSCKyWYhK"
        network.login(read, write, client, "bob", "axes", 0)
        coVerifyOrder {
            write.writeByte(2)
            write.writeByte(17)
            write.writeByte(2)
            write.writeShort(1)
            write.writeString("bob")
        }
    }*/
}
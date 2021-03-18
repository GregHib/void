package world.gregs.voidps.network

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import world.gregs.voidps.engine.data.PlayerLoader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import java.math.BigInteger

@ExtendWith(MockKExtension::class)
internal class NetworkTest {
    @MockK
    lateinit var network: Network

    @MockK
    lateinit var loginQueue: LoginQueue

    @MockK
    lateinit var loader: PlayerLoader

    lateinit var protocol: MutableMap<Int, Decoder>

    @RelaxedMockK
    lateinit var read: ByteReadChannel

    @RelaxedMockK
    lateinit var write: ByteWriteChannel

    @BeforeEach
    fun setup() {
        protocol = mutableMapOf()
        network = spyk(Network(protocol, 123, BigInteger.ONE, BigInteger.TWO, loginQueue, loader, TestCoroutineDispatcher(), 1))
    }

    @Test
    fun `Synchronise client`() = runBlockingTest {
        coEvery { read.readByte() } returns 14
        network.synchronise(read, write)
        coVerify { write.writeByte(0) }
    }

    @Test
    fun `Synchronise with wrong id`() = runBlockingTest {
        coEvery { read.readByte() } returns 0
        network.synchronise(read, write)
        coVerify(exactly = 0) { write.writeByte(0) }
        coVerify {
            write.close()
        }
    }

    @Test
    fun `Begin client login`() = runBlockingTest {
        coEvery { read.readByte() } returns 16
        coEvery { read.readShort() } returns 0
        val packet: ByteReadPacket = mockk()
        coEvery { read.readPacket(0) } returns packet
        coEvery { network.checkClientVersion(read, packet, write, any()) } just Runs
        network.login(read, write, "localhost")
        coVerify {
            network.checkClientVersion(read, packet, write, any())
        }
    }

    @Test
    fun `Invalid client login`() = runBlockingTest {
        coEvery { read.readByte() } returns 100
        network.login(read, write, "localhost")
        coVerify {
            write.writeByte(11)
            write.close()
        }
    }

    @Test
    fun `Check client version`() = runBlockingTest {
        mockkStatic("io.ktor.utils.io.core.InputPrimitivesKt")
        val packet: ByteReadPacket = mockk(relaxed = true)
        coEvery { packet.readInt() } returns 123
        val rsa: ByteReadPacket = mockk()
        coEvery { network.decryptRSA(packet) } returns rsa
        coEvery { network.validateSession(read, any(), packet, write, any()) } just Runs
        network.checkClientVersion(read, packet, write, "127.0.0.1")
        coVerify {
            network.validateSession(read, any(), packet, write, any())
        }
    }

    @Test
    fun `Check outdated client version`() = runBlockingTest {
        mockkStatic("io.ktor.utils.io.core.InputPrimitivesKt")
        val packet: ByteReadPacket = mockk(relaxed = true)
        coEvery { packet.readInt() } returns 0
        network.checkClientVersion(read, packet, write, "localhost")
        coVerify {
            write.writeByte(6)
            write.close()
        }
    }

    @Test
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
        every { loader.load(any()) } returns null
        val player: Player = mockk(relaxed = true)
        every { loader.create(any(), any()) } returns player
        every { loader.initPlayer(player, 1) } just Runs
        coEvery { network.readPackets(client, read) } just Runs
        network.login(read, write, client, "bob", "axes", 0)
        coVerify {
            network.readPackets(client, read)
            loader.create("bob", "axes")
            loader.initPlayer(player, 1)
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
        every { loader.load(any()) } returns player
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
        every { loader.load(any()) } returns player
        every { loader.initPlayer(player, 1) } just Runs
        coEvery { loginQueue.await() } just Runs
        coEvery { network.readPackets(client, read) } just Runs
        every { player.passwordHash } returns "\$2a\$10\$4ruibyyD1l.sGkzHLJvVn.keNd/jNWkoMNXaP0pVBdk8oSCKyWYhK"
        network.login(read, write, client, "bob", "axes", 0)
        coVerifyOrder {
            write.writeByte(2)
            write.writeByte(17)
            write.writeByte(2)
            write.writeShort(1)
            write.writeString("bob")
        }
    }
}
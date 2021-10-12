package world.gregs.voidps.network

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigInteger

@ExtendWith(MockKExtension::class)
internal class NetworkTest {
    @MockK
    lateinit var network: Network

    @RelaxedMockK
    lateinit var loader: Network.AccountLoader

    @RelaxedMockK
    lateinit var read: ByteReadChannel

    @RelaxedMockK
    lateinit var write: ByteWriteChannel

    @BeforeEach
    fun setup() {
        network = spyk(Network(123, BigInteger.ONE, BigInteger.TWO, loader))
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
    fun `Load account`() = runBlockingTest {
        val client: Client = mockk()
        network.login(read, write, client, "bob", "axes", 0)
        coVerify {
            loader.load(write, client, "bob", "axes", 0)
        }
    }
}
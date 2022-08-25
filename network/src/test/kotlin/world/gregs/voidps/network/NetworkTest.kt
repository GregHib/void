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
import java.math.BigInteger

@ExtendWith(MockKExtension::class)
internal class NetworkTest {
    @MockK
    lateinit var network: Network

    @RelaxedMockK
    lateinit var gatekeeper: NetworkGatekeeper

    @RelaxedMockK
    lateinit var loader: AccountLoader

    @RelaxedMockK
    lateinit var read: ByteReadChannel

    @RelaxedMockK
    lateinit var write: ByteWriteChannel

    @BeforeEach
    fun setup() {
        network = spyk(
            Network(
                123,
                BigInteger.ONE,
                BigInteger.valueOf(2),
                gatekeeper,
                loader,
                2,
                TestCoroutineDispatcher(),
                protocol(mockk())
            )
        )
    }

    @Test
    fun `Login limit exceeded`() = runBlockingTest {
        every { gatekeeper.connections("") } returns 1000

        network.connect(read, write, "")

        coVerify {
            write.writeByte(Response.LOGIN_LIMIT_EXCEEDED)
            write.close()
        }
    }

    @Test
    fun `Login server rejected synchronisation`() = runBlockingTest {
        coEvery { read.readByte() } returns 15

        network.connect(read, write, "")

        coVerify {
            write.writeByte(Response.LOGIN_SERVER_REJECTED_SESSION)
            write.close()
        }
    }

    @Test
    fun `Login server rejected session`() = runBlockingTest {
        var index = 0
        val array = arrayOf(14, 17)
        coEvery { read.readByte() } answers {
            array[index++].toByte()
        }

        network.connect(read, write, "")

        coVerify {
            write.writeByte(0)
            write.writeByte(Response.LOGIN_SERVER_REJECTED_SESSION)
            write.close()
        }
    }

    @Test
    fun `Game update`() = runBlockingTest {
        var index = 0
        val array = arrayOf(14, 16)
        coEvery { read.readByte() } answers {
            array[index++].toByte()
        }
        coEvery { read.readShort() } returns 4
        coEvery { read.readPacket(4) } returns ByteReadPacket(byteArrayOf(0, 0, 2, 123))

        network.connect(read, write, "")

        coVerify {
            write.writeByte(0)
            write.writeByte(Response.GAME_UPDATE)
            write.close()
        }
    }

    @Test
    fun `Bad session id`() = runBlockingTest {
        val rsa: ByteReadPacket = mockk()
        val packet: ByteReadPacket = mockk()
        every { rsa.readUByte() } returns 9.toUByte()

        network.validateSession(read, rsa, packet, write, "")

        coVerify {
            write.writeByte(Response.BAD_SESSION_ID)
            write.close()
        }
    }

    @Test
    fun `Bad password marker session`() = runBlockingTest {
        mockkStatic("io.ktor.utils.io.core.InputPrimitivesKt")
        val rsa: ByteReadPacket = mockk()
        val packet: ByteReadPacket = mockk()
        every { rsa.readUByte() } returns 10.toUByte()
        every { rsa.readInt() } returns 0
        every { rsa.readLong() } returns 1L

        network.validateSession(read, rsa, packet, write, "")

        coVerify {
            write.writeByte(Response.BAD_SESSION_ID)
            write.close()
        }
    }

    @Test
    fun `Account already online`() = runBlockingTest {
        mockkStatic("io.ktor.utils.io.core.InputPrimitivesKt")
        mockkStatic("io.ktor.utils.io.core.StringsKt")
        mockkStatic("world.gregs.voidps.network.JagExtensionsKt")
        val rsa: ByteReadPacket = mockk()
        val packet: ByteReadPacket = mockk()
        every { rsa.readUByte() } returns 10.toUByte()
        every { rsa.readInt() } returns 0
        every { rsa.readLong() } returns 0L
        every { rsa.readString() } returns "pass"
        every { packet.remaining } returns 1
        every { packet.readBytes(1) } returns byteArrayOf(0)
        every { gatekeeper.connected("") } returns true

        network.validateSession(read, rsa, packet, write, "")

        coVerify {
            write.writeByte(Response.ACCOUNT_ONLINE)
            write.close()
        }
    }

    @Test
    fun `World full`() = runBlockingTest {
        val client: Client = mockk(relaxed = true)
        every { client.address } returns "address"
        every { gatekeeper.connect("name", "address") } returns null

        network.login(read, client, "name", "password", 1)

        coVerify {
            client.disconnect(Response.WORLD_FULL)
        }
    }

    @Test
    fun `Read packet instructions`() = runBlockingTest {
        val client: Client = mockk(relaxed = true)
        every { client.address } returns "address"
        every { gatekeeper.connect("name", "address") } returns 123
        coEvery { loader.load(client, any(), any(), any(), any()) } returns null

        network.login(read, client, "name", "password", 1)

        coVerify {
            loader.load(client, "name", "password", 123, 1)
        }
    }

}
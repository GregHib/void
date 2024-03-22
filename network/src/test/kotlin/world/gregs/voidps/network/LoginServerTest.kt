package world.gregs.voidps.network

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.AccountLoader
import world.gregs.voidps.network.login.PasswordManager
import world.gregs.voidps.network.login.protocol.decoders
import world.gregs.voidps.network.login.protocol.readString
import java.math.BigInteger

@ExtendWith(MockKExtension::class)
@ExperimentalUnsignedTypes
internal class LoginServerTest {
    @MockK
    lateinit var network: LoginServer

    @RelaxedMockK
    lateinit var loader: AccountLoader

    @RelaxedMockK
    lateinit var read: ByteReadChannel

    @RelaxedMockK
    lateinit var write: ByteWriteChannel

    private lateinit var passwordManager: PasswordManager

    @BeforeEach
    fun setup() {
        passwordManager = PasswordManager(loader)
        network = spyk(
            LoginServer(decoders(mockk()), 123, BigInteger.ONE, BigInteger.valueOf(2), loader, passwordManager)
        )
    }

    @Test
    fun `Login server rejected synchronisation`() = runTest {
        coEvery { read.readByte() } returns 15

        network.connect(read, write, "")

        coVerify {
            write.writeByte(Response.LOGIN_SERVER_REJECTED_SESSION)
            write.close()
        }
    }

    @Test
    fun `Login server rejected session`() = runTest {
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
    fun `Login server rejected username`() = runTest {
        mockkStatic("io.ktor.utils.io.core.InputPrimitivesKt")
        mockkStatic("io.ktor.utils.io.core.StringsKt")
        mockkStatic("world.gregs.voidps.network.login.protocol.JagExtensionsKt")
        val rsa: ByteReadPacket = mockk()
        val packet: ByteReadPacket = mockk()
        every { rsa.readUByte() } returns 10.toUByte()
        every { rsa.readInt() } returns 0
        every { rsa.readLong() } returns 0L
        every { rsa.readString() } returns "password"
        val data = "A username that is too long".toByteArray()
        Xtea.encipher(data, 0,  data.size, intArrayOf(0, 0, 0, 0))
        every { packet.remaining } returns data.size.toLong()
        every { packet.readBytes(data.size) } returns data

        network.validateSession(read, rsa, packet, write, "")

        coVerify {
            write.writeByte(Response.LOGIN_SERVER_REJECTED_SESSION)
            write.close()
        }
    }

    @Test
    fun `Game update`() = runTest {
        coEvery { read.readByte() } returns 16
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
    fun `Bad session id`() = runTest {
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
    fun `Bad password marker session`() = runTest {
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

    private val passwordHash = "\$2a\$10${"$"}b6AHMNNHed/zUC/CMl3AnudrMwPvy/td..Ke3O2RcFg0jyLtLED5e"

    @Test
    fun `Account already online`() = runTest {
        every { loader.password(any()) } returns passwordHash
        every { loader.assignIndex("") } returns 1

        val index = network.validate(write, "name", "password")
        assertEquals(0, index)
        val result = network.validate(write, "name", "password")
        assertNull(result)

        coVerify {
            write.writeByte(Response.ACCOUNT_ONLINE)
            write.close()
        }
    }

    @Test
    fun `World full`() = runTest {
        every { loader.password(any()) } returns passwordHash
        every { loader.assignIndex("name") } returns null

        val result = network.validate(write, "name", "password")

        assertNull(result)

        coVerify {
            write.writeByte(Response.WORLD_FULL)
            write.close()
        }
    }

    @Test
    fun `Read packet instructions`() = runTest {
        val client: Client = mockk(relaxed = true)
        every { loader.assignIndex("name") } returns 123
        coEvery { loader.load(client, any(), any(), any(), any()) } returns null

        network.login(read, client, "name", "password", 123, 1)

        coVerify {
            loader.load(client, "name", "password", 123, 1)
        }
    }

}
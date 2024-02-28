package world.gregs.voidps.network

import io.ktor.utils.io.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalUnsignedTypes
internal class GameServerTest {
    @MockK
    lateinit var server: GameServer

    @RelaxedMockK
    lateinit var manager: SessionManager

    @RelaxedMockK
    lateinit var read: ByteReadChannel

    @RelaxedMockK
    lateinit var write: ByteWriteChannel

    @BeforeEach
    fun setup() {
        manager = mockk(relaxed = true)
        server = spyk(
            GameServer(
                manager,
                2,
                mockk(relaxed = true)
            )
        )
        server.loginServer = mockk(relaxed = true)
    }

    @Test
    fun `Login limit exceeded`() = runTest {
        every { manager.count("") } returns 1000

        server.connect(read, write, "")

        coVerify {
            write.writeByte(Response.LOGIN_LIMIT_EXCEEDED)
            write.close()
        }
    }

    @Test
    fun `Network rejected synchronisation`() = runTest {
        coEvery { read.readByte() } returns 12

        server.connect(read, write, "")

        coVerify {
            write.writeByte(Response.INVALID_LOGIN_SERVER)
            write.close()
        }
    }

    @Test
    fun `No login server response`() = runTest {
        every { manager.count("") } returns 1000
        coEvery { read.readByte() } returns 14
        server.loginServer = null

        server.connect(read, write, "123")

        coVerify {
            manager.add("123")
            write.writeByte(Response.LOGIN_SERVER_OFFLINE)
            manager.remove("123")
        }
    }
}

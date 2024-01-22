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
    lateinit var gatekeeper: NetworkGatekeeper

    @RelaxedMockK
    lateinit var read: ByteReadChannel

    @RelaxedMockK
    lateinit var write: ByteWriteChannel

    @BeforeEach
    fun setup() {
        server = spyk(
            GameServer(
                gatekeeper,
                2,
                mockk(relaxed = true),
                mockk(relaxed = true)
            )
        )
    }

    @Test
    fun `Login limit exceeded`() = runTest {
        every { gatekeeper.connections("") } returns 1000

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
}

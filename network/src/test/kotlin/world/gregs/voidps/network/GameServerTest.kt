package world.gregs.voidps.network

import io.ktor.util.network.*
import io.ktor.utils.io.*
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.network.client.ConnectionTracker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.util.*

internal class GameServerTest {

    private lateinit var server: GameServer
    private lateinit var tracker: ConnectionTracker
    private val fileServer = object : Server {
        override suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
            write.writeByte(Response.SUCCESS)
        }
    }
    private val loginServer = object : Server {
        override suspend fun connect(read: ByteReadChannel, write: ByteWriteChannel, hostname: String) {
            write.writeByte(Response.SUCCESS)
        }
    }

    @BeforeEach
    fun setup() {
        tracker = ConnectionTracker(2)
        server = spyk(GameServer(fileServer, tracker))
    }

    @Test
    fun `Exception on server setup shuts down`() {
        assertThrows<IllegalArgumentException> {
            server.start(-1)
        }
        verify {
            server.stop()
        }
    }

    @Test
    fun `Server starts socket connections inside coroutine`() {
        val job = server.start(2345)
        assertTrue(job.isActive)
    }

    @Test
    fun `Server accepts connection and returns too many connections`() {
        val job = server.start(1234)
        assertTrue(job.isActive)
        Socket("localhost", 1234).use { client ->
            val address = client.localSocketAddress.hostname
            tracker.add(address)
            tracker.add(address)
            assertTrue(client.isConnected)
            val input = BufferedReader(InputStreamReader(client.getInputStream()))
            assertEquals(Response.LOGIN_LIMIT_EXCEEDED, input.read())
        }
    }

    @Test
    fun `Stopped server shuts down`() {
        val job = server.start(4321)
        server.stop()
        assertFalse(job.isActive)
    }

    @Test
    fun `Connect to login server`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        server.loginServer = loginServer

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.writeByte(Request.CONNECT_LOGIN.toByte())

        assertEquals(Response.SUCCESS, writeChannel.readByte().toInt())
        assertFalse(writeChannel.isClosedForRead)
    }

    @Test
    fun `Connect with invalid connection opcode`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.writeByte(-1)

        assertEquals(Response.INVALID_LOGIN_SERVER, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Connect to file server`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.writeByte(Request.CONNECT_JS5.toByte())

        assertEquals(Response.SUCCESS, writeChannel.readByte().toInt())
        assertFalse(writeChannel.isClosedForRead)
    }

    @Test
    fun `Connect to offline login server`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.writeByte(Request.CONNECT_LOGIN.toByte())

        assertEquals(Response.LOGIN_SERVER_OFFLINE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Connect too many times`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        tracker.add("localhost")
        tracker.add("localhost")

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.writeByte(Request.CONNECT_LOGIN.toByte())

        assertEquals(Response.LOGIN_LIMIT_EXCEEDED, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @ExperimentalUnsignedTypes
    @Test
    fun `Connect with properties server`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        val properties = Properties()
        properties.setProperty("network.maxClientPerIP", "0")
        properties.setProperty("storage.cache.server", "false")
        server = GameServer.load(mockk(relaxed = true), properties)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.writeByte(Request.CONNECT_LOGIN.toByte())

        assertEquals(Response.LOGIN_LIMIT_EXCEEDED, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @AfterEach
    fun shutdown() {
        server.stop()
    }
}

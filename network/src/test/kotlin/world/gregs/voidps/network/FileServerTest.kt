package world.gregs.voidps.network

import io.ktor.utils.io.*
import io.mockk.mockk
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.network.file.FileProvider
import world.gregs.voidps.network.login.protocol.writeMedium
import java.util.*

class FileServerTest {

    private lateinit var server: FileServer
    private lateinit var provider: FileProvider
    private val prefetchKeys = IntArray(5) { it }
    private var data: ByteArray? = null

    @BeforeEach
    fun before() {
        provider = object : FileProvider {
            override fun data(index: Int, archive: Int): ByteArray? {
                return data
            }

            override suspend fun encode(write: ByteWriteChannel, data: ByteArray) {
                write.writeFully(data)
            }
        }

        server = FileServer(123, prefetchKeys, provider)
    }

    @ParameterizedTest
    @ValueSource(ints = [Request.PRIORITY_REQUEST, Request.PREFETCH_REQUEST])
    fun `Request file`(request: Int) = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        val index = 4
        val archive = 3
        val compression = 2
        data = byteArrayOf(compression.toByte(), 9, 8, 7, 6, 5, 4, 3, 2, 1)

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }

        readChannel.writeInt(123)
        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        for (key in prefetchKeys) {
            assertEquals(key, writeChannel.readInt())
        }
        readChannel.writeByte(Request.ACKNOWLEDGE)
        readChannel.writeMedium(3)

        readChannel.writeByte(request)
        readChannel.writeMedium((index shl 16) or archive)

        assertEquals(index, writeChannel.readByte().toInt())
        assertEquals(archive, writeChannel.readShort().toInt())
        if (request == Request.PRIORITY_REQUEST) {
            assertEquals(compression, writeChannel.readByte().toInt())
        } else {
            assertEquals((compression or 0x80).toByte(), writeChannel.readByte())
        }
        for (byte in data!!) {
            assertEquals(byte, writeChannel.readByte())
        }
        job.cancelAndJoin()
    }

    @ParameterizedTest
    @ValueSource(ints = [Request.STATUS_LOGGED_OUT, Request.STATUS_LOGGED_IN, Request.ENCRYPTION_KEY_UPDATE])
    fun `Status update`(request: Int) = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }

        readChannel.writeInt(123)
        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        for (key in prefetchKeys) {
            assertEquals(key, writeChannel.readInt())
        }
        readChannel.writeByte(Request.ACKNOWLEDGE)
        readChannel.writeMedium(3)

        delay(10) // required for coroutine context switching
        readChannel.writeByte(request)
        readChannel.writeMedium(0)
        job.cancelAndJoin()
    }

    @Test
    fun `Invalid revision`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }

        readChannel.writeInt(11)
        assertEquals(Response.GAME_UPDATE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
        job.cancelAndJoin()
    }

    @Test
    fun `Invalid acknowledge`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }

        readChannel.writeInt(123)
        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        for (key in prefetchKeys) {
            assertEquals(key, writeChannel.readInt())
        }
        readChannel.writeByte(4) // invalid ack

        assertEquals(Response.LOGIN_SERVER_REJECTED_SESSION, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
        job.cancelAndJoin()
    }

    @Test
    fun `Invalid acknowledge id`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }

        readChannel.writeInt(123)
        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        for (key in prefetchKeys) {
            assertEquals(key, writeChannel.readInt())
        }
        readChannel.writeByte(Request.ACKNOWLEDGE)
        readChannel.writeMedium(4) // invalid ack id

        assertEquals(Response.BAD_SESSION_ID, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
        job.cancelAndJoin()
    }

    @Test
    fun `Invalid verify`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }

        readChannel.writeInt(123)
        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        for (key in prefetchKeys) {
            assertEquals(key, writeChannel.readInt())
        }
        readChannel.writeByte(Request.ACKNOWLEDGE)
        readChannel.writeMedium(3)
        readChannel.writeByte(Request.STATUS_LOGGED_IN)
        readChannel.writeMedium(2) // invalid

        assertEquals(Response.BAD_SESSION_ID, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
        job.cancelAndJoin()
    }

    @Test
    fun `Invalid request`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }

        readChannel.writeInt(123)
        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        for (key in prefetchKeys) {
            assertEquals(key, writeChannel.readInt())
        }
        readChannel.writeByte(Request.ACKNOWLEDGE)
        readChannel.writeMedium(3)

        delay(10) // required for coroutine context switching
        readChannel.writeByte(Request.LOGIN)
        delay(10)
        assertTrue(readChannel.isClosedForWrite)
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Load offline file server from properties`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        val properties = Properties()
        properties.setProperty("storage.cache.server", "external")

        val server = FileServer.load(mockk(relaxed = true), properties)

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }

        readChannel.writeInt(11)
        assertEquals(Response.LOGIN_SERVER_OFFLINE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
        job.cancelAndJoin()
    }

    @Test
    fun `Load file server from properties`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        val properties = Properties()
        properties.setProperty("storage.cache.server", "internal")
        properties.setProperty("server.revision", "123")
        val prefetchKeys = listOf(1, 2, 3, 4)
        properties.setProperty("prefetch.keys", prefetchKeys.joinToString(","))
        val server = FileServer.load(mockk(relaxed = true), properties)

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.writeInt(123)
        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        for (key in prefetchKeys) {
            assertEquals(key, writeChannel.readInt())
        }
        readChannel.writeByte(4) // invalid ack

        assertEquals(Response.LOGIN_SERVER_REJECTED_SESSION, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
        job.cancelAndJoin()
    }
}
package world.gregs.voidps.network

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.AccountLoader
import world.gregs.voidps.network.login.PasswordManager
import world.gregs.voidps.network.login.protocol.Decoder
import java.math.BigInteger
import java.util.*

@ExperimentalUnsignedTypes
internal class LoginServerTest {

    private lateinit var server: LoginServer
    private val protocol = Array<Decoder?>(10) { null }
    private lateinit var accounts: AccountLoader
    private lateinit var passwordManager: PasswordManager
    private lateinit var instructions: Channel<Instruction>
    private var client: Client? = null
    private var password: String? = null

    private data class TestInstruction(val value: Int) : Instruction

    @BeforeEach
    fun setup() {
        client = null
        password = "\$2a\$10${"$"}iIdTrtrJ5ibgFcJToZW7ueGkymDed2Ws2FoE8JnrXPGiY2YNVa9y6"
        instructions = Channel(capacity = 1)
        accounts = object : AccountLoader {
            override fun exists(username: String) = true

            override fun password(username: String) = password

            override suspend fun load(client: Client, username: String, passwordHash: String, displayMode: Int): SendChannel<Instruction> {
                this@LoginServerTest.client = client
                client.send(0) {
                    writeByte(Response.SUCCESS)
                }
                return instructions
            }
        }
        protocol[0] = object : Decoder(4) {
            override suspend fun decode(packet: ByteReadPacket): Instruction {
                val value = packet.readInt()
                return TestInstruction(value)
            }
        }
        passwordManager = PasswordManager(accounts)
        server = LoginServer(protocol, 123, 10, modulus, BigInteger("10001", 16), accounts, passwordManager)
    }

    @TestFactory
    fun `Login to server and send packet`() = listOf(
        Request.LOGIN to 4,
        Request.LOGIN to Decoder.BYTE,
        Request.RECONNECT to Decoder.SHORT,
    ).map { (request, size) ->
        dynamicTest("${if (request == Request.RECONNECT) "Reconnect" else "Login"} to server and send $size packet") {
            runTest {
                val readChannel = ByteChannel(autoFlush = true)
                val writeChannel = ByteChannel(autoFlush = true)
                protocol[0] = object : Decoder(size) {
                    override suspend fun decode(packet: ByteReadPacket): Instruction {
                        val value = packet.readInt()
                        return TestInstruction(value)
                    }
                }

                val job = launch {
                    server.connect(readChannel, writeChannel, "localhost")
                }
                writeLoginPacket(readChannel, request)

                assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
                assertEquals(118, writeChannel.readByte().toInt()) // packet 0
                assertEquals(Response.SUCCESS, writeChannel.readByte().toInt())
                writeTestPacket(readChannel, size)
                val instruction = instructions.tryReceive().getOrNull()
                assertNotNull(instruction)
                assertEquals(TestInstruction(42), instruction)
                job.cancelAndJoin()
            }
        }
    }

    @Test
    fun `Invalid connection request`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.writeByte(Request.LOBBY)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(Response.LOGIN_SERVER_REJECTED_SESSION, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Invalid version`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.apply {
            writeByte(Request.RECONNECT)
            writeShort(4)
            writeInt(718) // version
        }

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(Response.GAME_UPDATE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Invalid packet length`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        launch {
            assertThrows<EOFException> {
                server.connect(readChannel, writeChannel, "localhost")
            }
        }
        readChannel.apply {
            writeByte(Request.RECONNECT)
            writeShort(2)
            writeShort(2)
        }

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
    }

    @Test
    fun `Invalid empty rsa block`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        readChannel.apply {
            writeByte(Request.LOGIN)
            writeShort(6) // length
            writeInt(123)
            writeShort(0)
        }

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
    }

    @Test
    fun `Invalid session`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel, session = 11)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(Response.BAD_SESSION_ID, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Invalid password marker`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel, passwordMarker = 1)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(Response.BAD_SESSION_ID, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Client disconnect after validation`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            assertThrows<ClosedReceiveChannelException> {
                server.connect(readChannel, writeChannel, "localhost")
            }
        }
        writeLoginPacket(readChannel, Request.LOGIN)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(118, writeChannel.readByte().toInt()) // packet 0
        assertEquals(Response.SUCCESS, writeChannel.readByte().toInt())
        assertTrue(server.online.contains("username"))
        readChannel.close()
        delay(10)
        assertFalse(server.online.contains("username"))
    }

    @Test
    fun `Account load fails`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        accounts = object : AccountLoader {

            override fun exists(username: String) = false

            override fun password(username: String) = null

            override suspend fun load(client: Client, username: String, passwordHash: String, displayMode: Int): SendChannel<Instruction>? {
                client.disconnect(Response.ACCOUNT_ONLINE)
                return null
            }
        }
        passwordManager = PasswordManager(accounts)
        server = LoginServer(protocol, 123, 10, modulus, BigInteger("10001", 16), accounts, passwordManager)
        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel, Request.LOGIN)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(Response.ACCOUNT_ONLINE, writeChannel.readByte().toInt())
    }

    @Test
    fun `Disconnect client after first packet`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        server = LoginServer(protocol, 123, 10, modulus, BigInteger("10001", 16), accounts, passwordManager)

        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel)
        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertTrue(server.online.contains("username"))
        client!!.disconnect()
        writeTestPacket(readChannel)
        assertFalse(server.online.contains("username"))
        assertTrue(writeChannel.isClosedForWrite)
    }

    @Test
    fun `No packet decoder`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        protocol[0] = null

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(118, writeChannel.readByte().toInt()) // packet 0
        assertEquals(Response.SUCCESS, writeChannel.readByte().toInt())
        writeTestPacket(readChannel)
        assertNull(instructions.tryReceive().getOrNull())
        assertTrue(writeChannel.isClosedForRead)

        job.cancelAndJoin()
    }

    @Test
    fun `Already online`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        server.online.add("username")
        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(Response.ACCOUNT_ONLINE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `World full no index`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        server.online.addAll((0..10).map { it.toString() })
        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(Response.WORLD_FULL, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Username too long`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel, username = "aLongUsername")

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(Response.LOGIN_SERVER_REJECTED_SESSION, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Invalid password`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        password = "\$2a\$10${"$"}WisAwbHvp9Gj/61o.BElqOGWECIq/Rb2xxAV1he2w9LeVzUkeP6py" // Invalid
        launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(Response.INVALID_CREDENTIALS, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Load from properties`() = runTest {
        val properties = Properties()
        properties.setProperty("security.game.modulus", modulus.toString(16))
        properties.setProperty("security.game.private", "10001")
        properties.setProperty("server.revision", "123")
        properties.setProperty("world.players.max", "10")
        server = LoginServer.load(properties, protocol, accounts)
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        protocol[0] = object : Decoder(4) {
            override suspend fun decode(packet: ByteReadPacket): Instruction {
                val value = packet.readInt()
                return TestInstruction(value)
            }
        }

        val job = launch {
            server.connect(readChannel, writeChannel, "localhost")
        }
        writeLoginPacket(readChannel)

        assertEquals(Response.DATA_CHANGE, writeChannel.readByte().toInt())
        assertEquals(118, writeChannel.readByte().toInt()) // packet 0
        assertEquals(Response.SUCCESS, writeChannel.readByte().toInt())
        writeTestPacket(readChannel)
        val instruction = instructions.tryReceive().getOrNull()
        assertNotNull(instruction)
        assertEquals(TestInstruction(42), instruction)
        job.cancelAndJoin()
    }

    private suspend fun writeTestPacket(readChannel: ByteChannel, size: Int = 4) {
        // Test packet
        readChannel.writePacket {
            writeUByte(405143795.toUByte()) // packet 0
            when (size) {
                Decoder.BYTE -> writeByte(4)
                Decoder.SHORT -> writeShort(4)
            }
            writeInt(42)
        }
        delay(10)
    }

    private suspend fun writeLoginPacket(
        readChannel: ByteChannel,
        request: Int = Request.LOGIN,
        version: Int = 123,
        session: Int = Request.SESSION,
        passwordMarker: Int = 0,
        username: String = "username",
    ) {
        readChannel.apply {
            writeByte(request)
            val data = buildPacket {
                writeInt(version)
                val data = buildPacket {
                    writeUByte(session.toUByte())
                    repeat(4) {
                        // Isaac
                        writeInt(0)
                    }
                    writeLong(passwordMarker.toLong())
                    writeText("password")
                    writeByte(0)
                }.readBytes()
                val rsa = RSA.crypt(data, modulus, private)
                writeUShort(rsa.size.toUShort())
                writeFully(rsa)
                val xtea = buildPacket {
                    writeText(username)
                    writeByte(0)
                    writeByte(0) // social login
                    writeByte(1) // display mode
                }.readBytes()
                Xtea.encipher(xtea, 0, xtea.size, intArrayOf(0, 0, 0, 0))
                writeFully(xtea)
            }.readBytes()
            writeShort(data.size)
            writeFully(data)
        }
    }

    companion object {
        private val modulus =
            BigInteger(
                "ea3680fdebf2621da7a33601ba39925ee203b3fc80775cd3727bf27fd8c0791c803e0bdb42b8b5257567177f8569024569da9147cef59009ed016af6007e57a556f1754f09ca84dd39a03287f7e41e8626fd78ab3b53262bd63f2e37403a549980bf3077bd402b82ef5fac269eb3c04d2a9b7712a67a018321ceba6c3bfb8f7f",
                16,
            )
        private val private =
            BigInteger(
                "8330565e649c16d32f841f0b26a97ad044def821164045b176adf0ae25d5e1c0d2206ef9b8ccc7429d194ab33622149096f3436f2a80a7d6b77794d7087dbc4f21239a4012b18afa3d1bede29d63f33bc553885f7117aa5d842231fae613d6e612c651249e66b7c67d565b21e68202798ccdbd0cc6dea3f6d033e719cb75ea01",
                16,
            )
    }
}

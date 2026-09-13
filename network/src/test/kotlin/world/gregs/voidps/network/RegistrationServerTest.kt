package world.gregs.voidps.network

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.io.writeUByte
import kotlinx.io.writeUShort
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.cache.secure.RSA
import world.gregs.voidps.cache.secure.Xtea
import world.gregs.voidps.network.login.AccountCreator
import world.gregs.voidps.network.login.Registration
import world.gregs.voidps.network.login.protocol.writeByte
import world.gregs.voidps.network.login.protocol.writeShort
import world.gregs.voidps.network.login.registration.RegistrationLimiter
import world.gregs.voidps.network.login.registration.RegistrationResponse
import java.math.BigInteger
import java.util.*

internal class RegistrationServerTest {

    private lateinit var server: RegistrationServer
    private lateinit var limiter: RegistrationLimiter
    private var availableResponse = RegistrationResponse.SUCCESS
    private var createResponse = RegistrationResponse.SUCCESS
    private var checked: String? = null
    private var registration: Registration? = null

    @BeforeEach
    fun setup() {
        availableResponse = RegistrationResponse.SUCCESS
        createResponse = RegistrationResponse.SUCCESS
        checked = null
        registration = null
        limiter = RegistrationLimiter(2, 60_000L) { 0L }
        val creator = object : AccountCreator {
            override fun available(email: String): Int {
                checked = email
                return availableResponse
            }

            override suspend fun create(registration: Registration): Int {
                this@RegistrationServerTest.registration = registration
                return createResponse
            }
        }
        server = RegistrationServer(634, modulus, BigInteger("10001", 16), creator, limiter)
    }

    private suspend fun connect(opcode: Int, readChannel: ByteChannel, writeChannel: ByteChannel): Int {
        server.connect(opcode, readChannel, writeChannel, "localhost")
        return writeChannel.readByte().toInt()
    }

    @Test
    fun `Check available email`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.SIGN_UP, readChannel, writeChannel, "localhost")
        }
        writeCheckPacket(readChannel, email = "Bob@Example.com")

        assertEquals(RegistrationResponse.SUCCESS, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
        assertEquals("bob@example.com", checked)
    }

    @Test
    fun `Check email in use`() = runTest {
        availableResponse = RegistrationResponse.EMAIL_IN_USE
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.SIGN_UP, readChannel, writeChannel, "localhost")
        }
        writeCheckPacket(readChannel, email = "bob@example.com")

        assertEquals(RegistrationResponse.EMAIL_IN_USE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Check invalid email`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.SIGN_UP, readChannel, writeChannel, "localhost")
        }
        writeCheckPacket(readChannel, email = "not an email")

        assertEquals(RegistrationResponse.INVALID_EMAIL, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
        assertNull(checked)
    }

    @Test
    fun `Outdated client revision`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.SIGN_UP, readChannel, writeChannel, "localhost")
        }
        writeCheckPacket(readChannel, revision = 718)

        assertEquals(RegistrationResponse.CLIENT_OUTDATED, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Empty rsa block`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.SIGN_UP, readChannel, writeChannel, "localhost")
        }
        readChannel.apply {
            writeShort(4)
            writeShort(634)
            writeShort(0)
        }

        assertEquals(RegistrationResponse.UNAVAILABLE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Bad session id`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.SIGN_UP, readChannel, writeChannel, "localhost")
        }
        writeCheckPacket(readChannel, session = 11)

        assertEquals(RegistrationResponse.UNAVAILABLE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Truncated packet`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
        }
        readChannel.apply {
            writeShort(2)
            writeShort(634)
        }

        assertEquals(RegistrationResponse.UNAVAILABLE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Create account`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
        }
        writeCreatePacket(readChannel, email = "Bob@Example.com", password = "s3cure", age = 18, optIn = true, affiliate = 5, language = 1, info = "extra")

        assertEquals(RegistrationResponse.SUCCESS, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
        val registration = registration
        assertNotNull(registration)
        assertEquals("bob@example.com", registration!!.email)
        assertTrue(BCrypt.checkpw("s3cure", registration.passwordHash))
        assertEquals("localhost", registration.hostname)
        assertEquals(5, registration.affiliate)
        assertEquals(1, registration.language)
        assertTrue(registration.optIn)
    }

    @Test
    fun `Create account response is forwarded`() = runTest {
        createResponse = RegistrationResponse.EMAIL_IN_USE
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
        }
        writeCreatePacket(readChannel)

        assertEquals(RegistrationResponse.EMAIL_IN_USE, writeChannel.readByte().toInt())
        assertTrue(writeChannel.isClosedForRead)
    }

    @Test
    fun `Create with invalid email`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
        }
        writeCreatePacket(readChannel, email = "bob")

        assertEquals(RegistrationResponse.INVALID_EMAIL, writeChannel.readByte().toInt())
        assertNull(registration)
    }

    @Test
    fun `Create with short password`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
        }
        writeCreatePacket(readChannel, password = "abc")

        assertEquals(RegistrationResponse.PASSWORD_LENGTH, writeChannel.readByte().toInt())
        assertNull(registration)
    }

    @Test
    fun `Create with symbols in password`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
        }
        writeCreatePacket(readChannel, password = "pass word1")

        assertEquals(RegistrationResponse.PASSWORD_CHARACTERS, writeChannel.readByte().toInt())
        assertNull(registration)
    }

    @Test
    fun `Create with guessable password`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
        }
        writeCreatePacket(readChannel, password = "password")

        assertEquals(RegistrationResponse.PASSWORD_GUESSABLE, writeChannel.readByte().toInt())
        assertNull(registration)
    }

    @Test
    fun `Create when underage`() = runTest {
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
        }
        writeCreatePacket(readChannel, age = 12)

        assertEquals(RegistrationResponse.REFUSED, writeChannel.readByte().toInt())
        assertNull(registration)
    }

    @Test
    fun `Create rate limited per address`() = runTest {
        repeat(2) {
            val readChannel = ByteChannel(autoFlush = true)
            val writeChannel = ByteChannel(autoFlush = true)
            launch {
                server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
            }
            writeCreatePacket(readChannel)
            assertEquals(RegistrationResponse.SUCCESS, writeChannel.readByte().toInt())
        }
        registration = null
        val readChannel = ByteChannel(autoFlush = true)
        val writeChannel = ByteChannel(autoFlush = true)
        launch {
            server.connect(Request.CREATE_ACCOUNT, readChannel, writeChannel, "localhost")
        }
        writeCreatePacket(readChannel)

        assertEquals(RegistrationResponse.BUSY, writeChannel.readByte().toInt())
        assertNull(registration)
    }

    @Test
    fun `Load from properties`() {
        val properties = Properties()
        properties.setProperty("security.game.modulus", modulus.toString(16))
        properties.setProperty("security.game.private", private.toString(16))
        properties.setProperty("server.revision", "634")
        properties.setProperty("accounts.registration.maxPerIP", "1")
        properties.setProperty("accounts.registration.windowMinutes", "5")
        val server = RegistrationServer.load(
            properties,
            object : AccountCreator {
                override fun available(email: String) = RegistrationResponse.SUCCESS

                override suspend fun create(registration: Registration) = RegistrationResponse.SUCCESS
            },
        )
        assertNotNull(server)
    }

    private suspend fun writeCheckPacket(
        readChannel: ByteChannel,
        revision: Int = 634,
        session: Int = Request.SESSION,
        email: String = "bob@example.com",
    ) {
        writePacket(readChannel, revision, session) {
            writeText(email)
            writeByte(0)
            writeByte(0) // language
        }
    }

    private suspend fun writeCreatePacket(
        readChannel: ByteChannel,
        email: String = "bob@example.com",
        password: String = "s3cure",
        age: Int = 18,
        optIn: Boolean = false,
        affiliate: Int = 0,
        language: Int = 0,
        info: String? = null,
    ) {
        writePacket(readChannel, 634, Request.SESSION) {
            writeText(email)
            writeByte(0)
            writeUShort(affiliate.toUShort())
            writeText(password)
            writeByte(0)
            writeLong(0L) // user flow
            writeUByte(language.toUByte())
            writeByte(0) // game id
            writeFully(ByteArray(24)) // uid
            if (info != null) {
                writeByte(1)
                writeText(info)
                writeByte(0)
            } else {
                writeByte(0)
            }
            writeUByte(age.toUByte())
            writeByte(if (optIn) 1 else 0)
        }
    }

    private suspend fun writePacket(readChannel: ByteChannel, revision: Int, session: Int, body: BytePacketBuilder.() -> Unit) {
        val keys = intArrayOf(1, 2, 3, 4)
        val data = buildPacket {
            writeUShort(revision.toUShort())
            val rsa = RSA.crypt(
                buildPacket {
                    writeUByte(session.toUByte())
                    for (key in keys) {
                        writeInt(key)
                    }
                    repeat(10) {
                        writeInt(it)
                    }
                    writeShort(42)
                }.readBytes(),
                modulus,
                private,
            )
            writeUShort(rsa.size.toUShort())
            writeFully(rsa)
            val xtea = buildPacket {
                body()
                writeFully(ByteArray(7)) // padding
            }.readBytes()
            Xtea.encipher(xtea, 0, xtea.size, keys)
            writeFully(xtea)
        }.readBytes()
        readChannel.writeShort(data.size)
        readChannel.writeFully(data)
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

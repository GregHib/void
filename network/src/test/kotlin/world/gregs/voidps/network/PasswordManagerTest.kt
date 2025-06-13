package world.gregs.voidps.network

import kotlinx.coroutines.channels.SendChannel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.AccountLoader
import world.gregs.voidps.network.login.PasswordManager

class PasswordManagerTest {

    private lateinit var passwordManager: PasswordManager

    private lateinit var accountLoader: TestAccountLoader

    @BeforeEach
    fun setUp() {
        accountLoader = TestAccountLoader()
        passwordManager = PasswordManager(accountLoader)
    }

    @Test
    fun `Valid credentials is successful`() {
        val username = "test"
        val password = "password"
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        accountLoader.accountMap[username] = hashedPassword

        val result = passwordManager.validate(username, password)

        assertEquals(Response.SUCCESS, result)
    }

    @Test
    fun `Wrong password is invalid`() {
        val username = "test"
        val password = "wrongPassword"
        val hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt())
        accountLoader.accountMap[username] = hashedPassword

        val result = passwordManager.validate(username, password)

        assertEquals(Response.INVALID_CREDENTIALS, result)
    }

    @Test
    fun `New player with no password is successful`() {
        val username = "test"
        val password = "wrongPassword"
        accountLoader.exists = false

        val result = passwordManager.validate(username, password)

        assertEquals(Response.SUCCESS, result)
    }

    @Test
    fun `Existing player with no password is disabled`() {
        val username = "test"
        val password = "password"

        val result = passwordManager.validate(username, password)

        assertEquals(Response.ACCOUNT_DISABLED, result)
    }

    @Test
    fun `Long username is rejected`() {
        val username = "veryLongUsername"
        val password = "password"

        val result = passwordManager.validate(username, password)

        assertEquals(Response.LOGIN_SERVER_REJECTED_SESSION, result)
    }

    @Test
    fun `Encrypt new player returns hashed password`() {
        val username = "newbie"
        val password = "password"

        val hashedPassword = passwordManager.encrypt(username, password)

        assertNotEquals(password, hashedPassword)
    }

    @Test
    fun `Encrypt existing player returns stored hash`() {
        val username = "existing"
        val password = "password"
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        accountLoader.accountMap[username] = hashedPassword

        val newHashedPassword = passwordManager.encrypt(username, password)

        assertEquals(hashedPassword, newHashedPassword)
    }

    private class TestAccountLoader : AccountLoader {
        val accountMap = mutableMapOf<String, String>()
        var exists = true

        override fun exists(username: String): Boolean = exists

        override fun password(username: String): String? = accountMap[username]

        override suspend fun load(client: Client, username: String, passwordHash: String, displayMode: Int): SendChannel<Instruction>? = null
    }
}

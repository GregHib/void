package world.gregs.voidps.web.account

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.AccountUpdate
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.PlayerAccountUpdater
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.network.login.registration.RegistrationLimiter
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.web.api.ApiException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AccountServiceTest {

    private lateinit var loader: PlayerAccountLoader
    private lateinit var updater: PlayerAccountUpdater
    private lateinit var definitions: AccountDefinitions
    private lateinit var service: AccountService

    @BeforeEach
    fun setup() {
        Settings.load(mapOf("web.api.registration" to "true"))
        definitions = AccountDefinitions()
        loader = mockk()
        coEvery { loader.create(any(), any(), any(), any()) } answers {
            val name = firstArg<String>()
            definitions.merge(mapOf(name to AccountDefinition(name, thirdArg<String?>() ?: name, "", "hash")), emptyMap()) { false }
            RegistrationResponse.SUCCESS
        }
        updater = mockk()
        service = AccountService(loader, updater, definitions, RegistrationLimiter(2, 60_000L) { 0L })
    }

    private fun account(accountName: String, displayName: String) {
        definitions.merge(mapOf(accountName to AccountDefinition(accountName, displayName, "", "hash")), emptyMap()) { false }
    }

    private suspend fun assertField(field: String, block: suspend () -> Unit) {
        val exception = assertThrows<ApiException.Validation> { block() }
        assertEquals(field, exception.details.single().field)
    }

    @Test
    fun `Username create hashes password and normalises name`() = runTest {
        val account = service.create("Bob_1", "hunter22", null, "203.0.113.9")

        assertEquals("Bob 1", account.accountName)
        assertEquals("Bob 1", account.displayName)
        coVerify { loader.create("Bob 1", match { BCrypt.checkpw("hunter22", it) }, null, "203.0.113.9") }
    }

    @Test
    fun `Username display name must match`() = runTest {
        assertField("displayName") { service.create("Bob", "hunter22", "Bobby", "ip") }
        service.create("Bob", "hunter22", "Bob", "ip")

        coVerify(exactly = 1) { loader.create("Bob", any(), null, "ip") }
    }

    @Test
    fun `Email create requires a display name`() = runTest {
        assertField("displayName") { service.create("bob@example.com", "hunter22", null, "ip") }
        assertField("displayName") { service.create("bob@example.com", "hunter22", "Bob!", "ip") }
        val account = service.create("Bob@Example.com", "hunter22", "Bob_cat", "ip")

        assertEquals("bob@example.com", account.accountName)
        assertEquals("Bob cat", account.displayName)
        coVerify(exactly = 1) { loader.create("bob@example.com", any(), "Bob cat", "ip") }
    }

    @Test
    fun `Invalid names and passwords are rejected before hashing`() = runTest {
        assertField("name") { service.create("Bob!", "hunter22", null, "ip") }
        assertField("name") { service.create("Thirteen chars", "hunter22", null, "ip") }
        assertField("name") { service.create("bob@", "hunter22", "Bob", "ip") }
        assertField("password") { service.create("Bob", "bob", null, "ip") }
        assertField("password") { service.create("Bob", "pass word", null, "ip") }

        coVerify(exactly = 0) { loader.create(any(), any(), any(), any()) }
    }

    @Test
    fun `Registration can be disabled`() = runTest {
        Settings.load(mapOf("web.api.registration" to "false"))

        assertThrows<ApiException.Forbidden> { service.create("Bob", "hunter22", null, "ip") }
    }

    @Test
    fun `Rate limit counts well-formed requests per address`() = runTest {
        assertField("password") { service.create("Bob", "no", null, "ip") }
        assertField("password") { service.create("Bob", "no", null, "ip") }
        service.create("Bob", "hunter22", null, "ip")
        service.create("Bob2", "hunter22", null, "ip")
        val limited = assertThrows<ApiException.RateLimited> { service.create("Bob3", "hunter22", null, "ip") }
        assertEquals(60, limited.retryAfterSeconds)
        service.create("Bob3", "hunter22", null, "other")

        coVerify(exactly = 0) { loader.create("Bob3", any(), any(), "ip") }
    }

    @Test
    fun `Loader responses are mapped`() = runTest {
        coEvery { loader.create(any(), any(), any(), any()) } returns RegistrationResponse.EMAIL_IN_USE
        assertEquals("name_taken", assertThrows<ApiException.Conflict> { service.create("Bob", "hunter22", null, "ip") }.code)

        coEvery { loader.create(any(), any(), any(), any()) } returns RegistrationResponse.UNAVAILABLE
        assertThrows<ApiException.Unavailable> { service.create("Bob", "hunter22", null, "ip") }
    }

    @Test
    fun `Password is validated then hashed`() = runTest {
        account("Bob", "Bob")
        coEvery { updater.password("Bob", any()) } returns AccountUpdate.SUCCESS

        assertField("password") { service.password("Bob", "bob") }
        service.password("Bob", "newpass1")

        coVerify(exactly = 1) { updater.password("Bob", match { BCrypt.checkpw("newpass1", it) }) }
    }

    @Test
    fun `Rename normalises and maps results`() = runTest {
        account("bob@example.com", "Bobby")
        account("Bob", "Bob")
        coEvery { updater.rename("bob@example.com", "Bobby Two") } returns AccountUpdate.TAKEN
        coEvery { updater.rename("Bob", "Bad") } returns AccountUpdate.INVALID
        coEvery { updater.rename("Bob", "Gone") } returns AccountUpdate.NOT_FOUND
        coEvery { updater.rename("Bob", "Busy") } returns AccountUpdate.BUSY

        assertEquals("name_taken", assertThrows<ApiException.Conflict> { service.rename("Bob@Example.com", "Bobby__Two") }.code)
        assertField("displayName") { service.rename("Bob", "Bad") }
        assertEquals("account_not_found", assertThrows<ApiException.NotFound> { service.rename("Bob", "Gone") }.code)
        assertEquals("account_busy", assertThrows<ApiException.Conflict> { service.rename("Bob", "Busy") }.code)
    }

    @Test
    fun `Password and rename only reach accounts by account name`() = runTest {
        account("bob@example.com", "Bob Cat")
        coEvery { updater.password("bob@example.com", any()) } returns AccountUpdate.SUCCESS

        assertThrows<ApiException.NotFound> { service.password("Bob Cat", "newpass1") }
        assertThrows<ApiException.NotFound> { service.rename("Bob Cat", "Bobby") }
        service.password("Bob@Example.com", "newpass1")

        coVerify(exactly = 1) { updater.password("bob@example.com", any()) }
        coVerify(exactly = 0) { updater.rename(any(), any()) }
    }

    @Test
    fun `Unknown accounts aren't passed to the updater`() = runTest {
        assertThrows<ApiException.NotFound> { service.password("alice", "newpass1") }
        assertThrows<ApiException.NotFound> { service.rename("alice", "Alicia") }

        coVerify(exactly = 0) { updater.password(any(), any()) }
        coVerify(exactly = 0) { updater.rename(any(), any()) }
    }

    @Test
    fun `Staff accounts are forbidden`() = runTest {
        account("Mod Bob", "Mod Bob")
        coEvery { updater.password("Mod Bob", any()) } returns AccountUpdate.PROTECTED
        coEvery { updater.rename("Mod Bob", any()) } returns AccountUpdate.PROTECTED

        assertThrows<ApiException.Forbidden> { service.password("Mod Bob", "newpass1") }
        assertThrows<ApiException.Forbidden> { service.rename("Mod Bob", "Bobby") }
    }

    @Test
    fun `Account lookup is by account name only`() {
        account("bob@example.com", "Bob")
        account("Api Bob", "Bobby")

        val account = service.account("BOB@EXAMPLE.COM")
        assertNotNull(account)
        assertEquals("bob@example.com", account.accountName)
        assertEquals("Bob", account.displayName)
        assertEquals("Api Bob", service.account("Api_Bob")?.accountName)
        assertNull(service.account("bob"))
        assertNull(service.account("alice"))
    }

    @Test
    fun `Names are unavailable when used as an account or display name`() = runTest {
        account("bob@example.com", "Bob")
        account("Api Bob", "Bobby")

        assertFalse(service.available("bob"))
        assertFalse(service.available("Bob@Example.com"))
        assertFalse(service.available("api_bob"))
        assertFalse(service.available("BOBBY"))
        assertTrue(service.available("Alice"))
        assertTrue(service.available("alice@example.com"))
        assertField("name") { service.available("Bob!") }
    }
}

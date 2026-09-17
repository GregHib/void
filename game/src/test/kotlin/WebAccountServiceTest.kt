import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.AccountUpdate
import world.gregs.voidps.engine.client.PlayerAccountCreator
import world.gregs.voidps.engine.client.PlayerAccountUpdater
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.network.login.registration.RegistrationLimiter
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.web.api.AccountResult
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class WebAccountServiceTest {

    private lateinit var creator: PlayerAccountCreator
    private lateinit var updater: PlayerAccountUpdater
    private lateinit var definitions: AccountDefinitions
    private lateinit var service: WebAccountService

    @BeforeEach
    fun setup() {
        Settings.load(mapOf("web.api.registration" to "true", "server.name" to "Void", "server.revision" to "634", "world.id" to "2", "world.name" to "World 2"))
        creator = mockk()
        coEvery { creator.create(any(), any(), any(), any()) } returns RegistrationResponse.SUCCESS
        updater = mockk()
        definitions = AccountDefinitions()
        service = WebAccountService(creator, updater, definitions, RegistrationLimiter(2, 60_000L) { 0L })
    }

    @Test
    fun `Username create hashes password and normalises name`() = runTest {
        assertEquals(AccountResult.SUCCESS, service.create("Bob_1", "hunter22", null, "203.0.113.9"))

        coVerify { creator.create("Bob 1", match { BCrypt.checkpw("hunter22", it) }, null, "203.0.113.9") }
    }

    @Test
    fun `Username display name must match`() = runTest {
        assertEquals(AccountResult.INVALID_DISPLAY_NAME, service.create("Bob", "hunter22", "Bobby", "ip"))
        assertEquals(AccountResult.SUCCESS, service.create("Bob", "hunter22", "Bob", "ip"))

        coVerify(exactly = 1) { creator.create("Bob", any(), null, "ip") }
    }

    @Test
    fun `Email create requires a display name`() = runTest {
        assertEquals(AccountResult.INVALID_DISPLAY_NAME, service.create("bob@example.com", "hunter22", null, "ip"))
        assertEquals(AccountResult.INVALID_DISPLAY_NAME, service.create("bob@example.com", "hunter22", "Bob!", "ip"))
        assertEquals(AccountResult.SUCCESS, service.create("Bob@Example.com", "hunter22", "Bob_cat", "ip"))

        coVerify(exactly = 1) { creator.create("bob@example.com", any(), "Bob cat", "ip") }
    }

    @Test
    fun `Invalid names and passwords are rejected before hashing`() = runTest {
        assertEquals(AccountResult.INVALID_NAME, service.create("Bob!", "hunter22", null, "ip"))
        assertEquals(AccountResult.INVALID_NAME, service.create("Thirteen chars", "hunter22", null, "ip"))
        assertEquals(AccountResult.INVALID_NAME, service.create("bob@", "hunter22", "Bob", "ip"))
        assertEquals(AccountResult.INVALID_PASSWORD, service.create("Bob", "bob", null, "ip"))
        assertEquals(AccountResult.INVALID_PASSWORD, service.create("Bob", "pass word", null, "ip"))

        coVerify(exactly = 0) { creator.create(any(), any(), any(), any()) }
    }

    @Test
    fun `Registration can be disabled`() = runTest {
        Settings.load(mapOf("web.api.registration" to "false"))

        assertEquals(AccountResult.REFUSED, service.create("Bob", "hunter22", null, "ip"))
    }

    @Test
    fun `Rate limit counts well-formed requests per address`() = runTest {
        assertEquals(AccountResult.INVALID_PASSWORD, service.create("Bob", "no", null, "ip"))
        assertEquals(AccountResult.INVALID_PASSWORD, service.create("Bob", "no", null, "ip"))
        assertEquals(AccountResult.SUCCESS, service.create("Bob", "hunter22", null, "ip"))
        assertEquals(AccountResult.SUCCESS, service.create("Bob2", "hunter22", null, "ip"))
        assertEquals(AccountResult.RATE_LIMITED, service.create("Bob3", "hunter22", null, "ip"))
        assertEquals(AccountResult.SUCCESS, service.create("Bob3", "hunter22", null, "other"))

        coVerify(exactly = 0) { creator.create("Bob3", any(), any(), "ip") }
    }

    @Test
    fun `Creator responses are mapped`() = runTest {
        coEvery { creator.create(any(), any(), any(), any()) } returns RegistrationResponse.EMAIL_IN_USE
        assertEquals(AccountResult.NAME_TAKEN, service.create("Bob", "hunter22", null, "ip"))

        coEvery { creator.create(any(), any(), any(), any()) } returns RegistrationResponse.UNAVAILABLE
        assertEquals(AccountResult.UNAVAILABLE, service.create("Bob", "hunter22", null, "ip"))
    }

    @Test
    fun `Password is validated then hashed`() = runTest {
        coEvery { updater.password("Bob", any()) } returns AccountUpdate.SUCCESS

        assertEquals(AccountResult.INVALID_PASSWORD, service.password("Bob", "bob"))
        assertEquals(AccountResult.SUCCESS, service.password("Bob", "newpass1"))

        coVerify(exactly = 1) { updater.password("Bob", match { BCrypt.checkpw("newpass1", it) }) }
    }

    @Test
    fun `Rename normalises and maps results`() = runTest {
        coEvery { updater.rename("bob@example.com", "Bobby Two") } returns AccountUpdate.TAKEN
        coEvery { updater.rename("Bob", "Bad") } returns AccountUpdate.INVALID
        coEvery { updater.rename("Bob", "Gone") } returns AccountUpdate.NOT_FOUND

        assertEquals(AccountResult.NAME_TAKEN, service.rename("Bob@Example.com", "Bobby__Two"))
        assertEquals(AccountResult.INVALID_DISPLAY_NAME, service.rename("Bob", "Bad"))
        assertEquals(AccountResult.NOT_FOUND, service.rename("Bob", "Gone"))
    }

    @Test
    fun `Account lookup by account name or display name`() = runTest {
        definitions.merge(mapOf("bob@example.com" to AccountDefinition("bob@example.com", "Bob", "Bobby", "hash")), emptyMap()) { false }

        val byDisplay = service.account("bob")
        assertNotNull(byDisplay)
        assertEquals("bob@example.com", byDisplay.accountName)
        assertEquals("Bobby", byDisplay.previousName)
        assertEquals("Bob", service.account("BOB@EXAMPLE.COM")?.displayName)
        assertNull(service.account("alice"))
        definitions.merge(mapOf("Api Bob" to AccountDefinition("Api Bob", "Api Bob", "", "hash")), emptyMap()) { false }
        assertEquals("Api Bob", service.account("Api_Bob")?.accountName)
    }

    @Test
    fun `Status reports settings and population`() {
        val status = service.status()

        assertEquals("Void", status.name)
        assertEquals(2, status.world)
        assertEquals("World 2", status.worldName)
        assertEquals(634, status.revision)
        assertEquals(0, status.players)
    }
}

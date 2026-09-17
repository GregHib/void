package world.gregs.voidps.web.api

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import world.gregs.voidps.web.webModule
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ApiRoutesTest {

    private class StubService : AccountService {
        val accounts = mutableMapOf<String, AccountInfo>()
        var createResult = AccountResult.SUCCESS
        var passwordResult = AccountResult.SUCCESS
        var renameResult = AccountResult.SUCCESS
        var created: List<Any?>? = null
        var password: Pair<String, String>? = null
        var renamed: Pair<String, String>? = null

        override fun status() = ServerStatus("Void", 1, "World 1", 634, 3, 60)

        override fun account(name: String): AccountInfo? = accounts[name.lowercase()] ?: accounts.values.firstOrNull { it.displayName.equals(name, ignoreCase = true) }

        override suspend fun create(name: String, password: String, displayName: String?, ip: String): AccountResult {
            created = listOf(name, password, displayName, ip)
            if (createResult == AccountResult.SUCCESS) {
                accounts[name.lowercase()] = AccountInfo(name, displayName ?: name, "", false)
            }
            return createResult
        }

        override suspend fun password(name: String, password: String): AccountResult {
            this.password = name to password
            return passwordResult
        }

        override suspend fun rename(name: String, displayName: String): AccountResult {
            renamed = name to displayName
            if (renameResult == AccountResult.SUCCESS) {
                val info = accounts.getValue(name.lowercase())
                accounts[name.lowercase()] = info.copy(displayName = displayName, previousName = info.displayName)
            }
            return renameResult
        }
    }

    private val service = StubService()

    private fun api(token: String? = "secret", block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
        application {
            webModule(8080, "localhost", 43594, webclientZip = null, api = token?.let { ApiConfig(it, service) })
        }
        block(client)
    }

    private val json = """{"name":"Bob","password":"hunter22"}"""

    @Test
    fun `Requests without a token are unauthorised`() = api { client ->
        val response = client.get("/api/status")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `Requests with the wrong token are unauthorised`() = api { client ->
        val response = client.post("/api/accounts") {
            bearerAuth("wrong")
            contentType(ContentType.Application.Json)
            setBody(json)
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertNull(service.created)
    }

    @Test
    fun `Api and webclient routes are absent when disabled`() = api(token = null) { client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/api/status").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/play").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/").status)
    }

    @Test
    fun `Status`() = api { client ->
        val response = client.get("/api/status") { bearerAuth("secret") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"name":"Void","world":1,"worldName":"World 1","revision":634,"players":3,"uptime":60}""", response.bodyAsText())
    }

    @Test
    fun `Create account with forwarded address`() = api { client ->
        val response = client.post("/api/accounts") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            header("X-Forwarded-For", "203.0.113.9, 10.0.0.1")
            setBody(json)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(listOf("Bob", "hunter22", null, "203.0.113.9"), service.created)
        assertEquals("""{"accountName":"Bob","displayName":"Bob","previousName":"","online":false}""", response.bodyAsText())
    }

    @Test
    fun `Create account falls back to the connection address`() = api { client ->
        client.post("/api/accounts") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            setBody("""{"name":"bob@example.com","password":"hunter22","displayName":"Bobcat"}""")
        }

        val created = service.created!!
        assertEquals("bob@example.com", created[0])
        assertEquals("Bobcat", created[2])
        val ip = created[3] as String
        assertTrue(ip.isNotEmpty())
        assertNotEquals("203.0.113.9", ip)
    }

    @Test
    fun `Create errors are mapped to status codes`() = api { client ->
        val expected = mapOf(
            AccountResult.INVALID_NAME to (HttpStatusCode.BadRequest to "invalid_name"),
            AccountResult.INVALID_DISPLAY_NAME to (HttpStatusCode.BadRequest to "invalid_display_name"),
            AccountResult.INVALID_PASSWORD to (HttpStatusCode.BadRequest to "invalid_password"),
            AccountResult.REFUSED to (HttpStatusCode.Forbidden to "registration_disabled"),
            AccountResult.NAME_TAKEN to (HttpStatusCode.Conflict to "name_taken"),
            AccountResult.RATE_LIMITED to (HttpStatusCode.TooManyRequests to "too_many_requests"),
            AccountResult.UNAVAILABLE to (HttpStatusCode.ServiceUnavailable to "unavailable"),
        )
        for ((result, expectation) in expected) {
            service.createResult = result
            val response = client.post("/api/accounts") {
                bearerAuth("secret")
                contentType(ContentType.Application.Json)
                setBody(json)
            }
            assertEquals(expectation.first, response.status, result.name)
            assertTrue(response.bodyAsText().contains("\"error\":\"${expectation.second}\""), result.name)
        }
    }

    @Test
    fun `Malformed json is a bad request`() = api { client ->
        val response = client.post("/api/accounts") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            setBody("""{"name":""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("invalid_json"))
        assertNull(service.created)
    }

    @Test
    fun `Get account by name`() = api { client ->
        service.accounts["bob@example.com"] = AccountInfo("bob@example.com", "Bob", "Bobby", true)

        val response = client.get("/api/accounts/bob") { bearerAuth("secret") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"accountName":"bob@example.com","displayName":"Bob","previousName":"Bobby","online":true}""", response.bodyAsText())
        assertEquals(HttpStatusCode.NotFound, client.get("/api/accounts/alice") { bearerAuth("secret") }.status)
    }

    @Test
    fun `Change password`() = api { client ->
        val response = client.put("/api/accounts/bob/password") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            setBody("""{"password":"newpass1"}""")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals("bob" to "newpass1", service.password)

        service.passwordResult = AccountResult.BUSY
        val busy = client.put("/api/accounts/bob/password") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            setBody("""{"password":"newpass1"}""")
        }
        assertEquals(HttpStatusCode.Conflict, busy.status)
        assertTrue(busy.bodyAsText().contains("\"error\":\"busy\""))
    }

    @Test
    fun `Rename account`() = api { client ->
        service.accounts["bob"] = AccountInfo("bob", "Bob", "", false)

        val response = client.put("/api/accounts/bob/name") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            setBody("""{"displayName":"Bobby"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("bob" to "Bobby", service.renamed)
        assertEquals("""{"accountName":"bob","displayName":"Bobby","previousName":"Bob","online":false}""", response.bodyAsText())

        service.renameResult = AccountResult.NAME_TAKEN
        val taken = client.put("/api/accounts/bob/name") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            setBody("""{"displayName":"Alice"}""")
        }
        assertEquals(HttpStatusCode.Conflict, taken.status)
        assertTrue(taken.bodyAsText().contains("\"error\":\"name_taken\""))
    }
}

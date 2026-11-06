package world.gregs.voidps.web.api.route

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import world.gregs.voidps.web.account.AccountService
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.AccountResponse
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AccountRoutesTest {

    private val service: AccountService = mockk()

    private fun api(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
        application {
            apiPlugins(apiKey = "secret")
            routing {
                route(API_PATH) {
                    accountRoutes(service)
                }
            }
        }
        block(client)
    }

    private val json = """{"name":"Bob","password":"hunter22"}"""

    private val bob = AccountResponse("Bob", "Bob", "", false)

    private suspend fun HttpClient.create(body: String = json, key: String = "secret") = post("/api/v1/accounts") {
        bearerAuth(key)
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    @Test
    fun `Requests without a key are unauthorised`() = api { client ->
        val response = client.get("/api/v1/accounts/bob")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertTrue(response.bodyAsText().contains("\"code\":\"unauthorized\""))
    }

    @Test
    fun `Requests with the wrong key are unauthorised`() = api { client ->
        val response = client.create(key = "wrong")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertTrue(response.bodyAsText().contains("\"code\":\"unauthorized\""))
        coVerify(exactly = 0) { service.create(any(), any(), any(), any()) }
    }

    @Test
    fun `Create account with forwarded address`() = api { client ->
        coEvery { service.create("Bob", "hunter22", null, "203.0.113.9") } returns bob

        val response = client.post("/api/v1/accounts") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            header("X-Forwarded-For", "203.0.113.9, 10.0.0.1")
            setBody(json)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("""{"accountName":"Bob","displayName":"Bob","previousName":"","online":false}""", response.bodyAsText())
    }

    @Test
    fun `Create account falls back to the connection address`() = api { client ->
        var ip = ""
        coEvery { service.create("bob@example.com", "hunter22", "Bobcat", any()) } answers {
            ip = arg(3)
            bob
        }

        client.create("""{"name":"bob@example.com","password":"hunter22","displayName":"Bobcat"}""")

        assertTrue(ip.isNotEmpty())
        assertNotEquals("203.0.113.9", ip)
    }

    @Test
    fun `Service errors use the api error envelope`() = api { client ->
        val expected = listOf(
            ApiException.Validation("password", "Too short") to (HttpStatusCode.UnprocessableEntity to "validation_failed"),
            ApiException.Forbidden("Account creation is disabled.") to (HttpStatusCode.Forbidden to "forbidden"),
            ApiException.Conflict("name_taken", "Taken") to (HttpStatusCode.Conflict to "name_taken"),
            ApiException.RateLimited(3600) to (HttpStatusCode.TooManyRequests to "rate_limited"),
            ApiException.Unavailable("Down") to (HttpStatusCode.ServiceUnavailable to "unavailable"),
        )
        for ((exception, expectation) in expected) {
            coEvery { service.create(any(), any(), any(), any()) } throws exception
            val response = client.create()
            assertEquals(expectation.first, response.status, exception.code)
            assertTrue(response.bodyAsText().contains("\"code\":\"${expectation.second}\""), exception.code)
        }
    }

    @Test
    fun `Validation errors name the field`() = api { client ->
        coEvery { service.create(any(), any(), any(), any()) } throws ApiException.Validation("password", "Too short")

        val response = client.create()

        assertTrue(response.bodyAsText().contains("\"field\":\"password\""))
    }

    @Test
    fun `Rate limited creates say when to retry`() = api { client ->
        coEvery { service.create(any(), any(), any(), any()) } throws ApiException.RateLimited(3600)

        val response = client.create()

        assertEquals(HttpStatusCode.TooManyRequests, response.status)
        assertEquals("3600", response.headers[HttpHeaders.RetryAfter])
    }

    @Test
    fun `Account routes don't allow other origins`() = api { client ->
        every { service.account("bob") } returns bob

        val response = client.get("/api/v1/accounts/bob") { bearerAuth("secret") }

        assertNull(response.headers[HttpHeaders.AccessControlAllowOrigin])
    }

    @Test
    fun `Malformed json is a bad request`() = api { client ->
        val response = client.create("""{"name":""")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("bad_request"))
        coVerify(exactly = 0) { service.create(any(), any(), any(), any()) }
    }

    @Test
    fun `Missing content type is a bad request`() = api { client ->
        val response = client.post("/api/v1/accounts") {
            bearerAuth("secret")
            setBody(json)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        coVerify(exactly = 0) { service.create(any(), any(), any(), any()) }
    }

    @Test
    fun `Get account by name`() = api { client ->
        every { service.account("bob") } returns AccountResponse("bob@example.com", "Bob", "Bobby", true)
        every { service.account("alice") } returns null

        val response = client.get("/api/v1/accounts/bob") { bearerAuth("secret") }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"accountName":"bob@example.com","displayName":"Bob","previousName":"Bobby","online":true}""", response.bodyAsText())
        val missing = client.get("/api/v1/accounts/alice") { bearerAuth("secret") }
        assertEquals(HttpStatusCode.NotFound, missing.status)
        assertTrue(missing.bodyAsText().contains("\"code\":\"account_not_found\""))
    }

    @Test
    fun `Check name availability`() = api { client ->
        every { service.available("Bob") } returns false
        every { service.available("Alice") } returns true

        val taken = client.get("/api/v1/names/Bob") { bearerAuth("secret") }
        val free = client.get("/api/v1/names/Alice") { bearerAuth("secret") }

        assertEquals(HttpStatusCode.OK, taken.status)
        assertEquals("""{"name":"Bob","available":false}""", taken.bodyAsText())
        assertEquals("""{"name":"Alice","available":true}""", free.bodyAsText())
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/names/Bob").status)
    }

    @Test
    fun `Change password`() = api { client ->
        coEvery { service.password("bob", "newpass1") } returns Unit

        val response = client.put("/api/v1/accounts/bob/password") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            setBody("""{"password":"newpass1"}""")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
        coVerify { service.password("bob", "newpass1") }
    }

    @Test
    fun `Rename account`() = api { client ->
        coEvery { service.rename("bob", "Bobby") } returns AccountResponse("bob", "Bobby", "Bob", false)

        val response = client.put("/api/v1/accounts/bob/name") {
            bearerAuth("secret")
            contentType(ContentType.Application.Json)
            setBody("""{"displayName":"Bobby"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"accountName":"bob","displayName":"Bobby","previousName":"Bob","online":false}""", response.bodyAsText())
    }
}

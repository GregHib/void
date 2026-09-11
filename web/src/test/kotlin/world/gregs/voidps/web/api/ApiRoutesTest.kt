package world.gregs.voidps.web.api

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.web.api.model.AccountRole
import world.gregs.voidps.web.api.route.api
import world.gregs.voidps.web.api.route.apiPlugins

/**
 * Smoke tests for the HTTP layer — routing, authentication, serialization and the error envelope.
 * The services are fakes from [Fakes]; what is under test is the plumbing between them and the
 * wire, not any data.
 */
class ApiRoutesTest {

    @Test
    fun `public route serves json`() = api {
        val response = client.get("/api/v1/worlds")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.json()
        assertEquals(1, body["items"]!!.jsonArray.size)
        assertEquals(812, body["totalPlayers"]!!.jsonPrimitive.content.toInt())
    }

    @Test
    fun `paged route carries the pagination envelope`() = api {
        val response = client.get("/api/v1/players/Thornwake/events?pageSize=10")

        assertEquals(HttpStatusCode.OK, response.status)
        val pagination = response.json()["pagination"]!!.jsonObject
        assertEquals(10, pagination["pageSize"]!!.jsonPrimitive.content.toInt())
        assertEquals(42, pagination["total"]!!.jsonPrimitive.content.toInt())
        assertEquals(5, pagination["totalPages"]!!.jsonPrimitive.content.toInt())
        assertEquals("true", pagination["hasNext"]!!.jsonPrimitive.content)
        assertEquals("false", pagination["hasPrevious"]!!.jsonPrimitive.content)
    }

    @Test
    fun `signed out request to a protected route is rejected`() = api {
        val response = client.get("/api/v1/account")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("unauthorized", response.errorCode())
    }

    @Test
    fun `non staff session cannot reach the developer panel`() = api {
        val response = client.get("/api/v1/dev/stats") {
            header(HttpHeaders.Authorization, "Bearer ${Fakes.PLAYER_TOKEN}")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals("forbidden", response.errorCode())
    }

    @Test
    fun `staff session reaches the developer panel`() = api {
        val response = client.get("/api/v1/dev/stats") {
            header(HttpHeaders.Authorization, "Bearer ${Fakes.STAFF_TOKEN}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1284, response.json()["playersOnline"]!!.jsonPrimitive.content.toInt())
    }

    @Test
    fun `session cookie authenticates as well as a bearer token`() = api {
        val response = client.get("/api/v1/account") {
            header(HttpHeaders.Cookie, "void_session=${Fakes.PLAYER_TOKEN}")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Thornwake", response.json()["displayName"]!!.jsonPrimitive.content)
    }

    @Test
    fun `a service failure becomes its mapped status and code`() = api {
        val response = client.get("/api/v1/hiscores/skills/juggling")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("skill_not_found", response.errorCode())
    }

    @Test
    fun `an out of range parameter is rejected before the service is called`() = api {
        val response = client.get("/api/v1/exchange/items?pageSize=500")

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals("validation_failed", response.errorCode())
        val details = response.json()["error"]!!.jsonObject["details"]!!.jsonArray
        assertEquals("pageSize", details[0].jsonObject["field"]!!.jsonPrimitive.content)
    }

    @Test
    fun `login sets the session cookie`() = api {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"Thornwake","password":"hunter2"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val cookie = response.headers[HttpHeaders.SetCookie]
        assertTrue(cookie != null && cookie.startsWith("void_session=${Fakes.PLAYER_TOKEN}"), "got: $cookie")
        assertEquals(Fakes.PLAYER_TOKEN, response.json()["token"]!!.jsonPrimitive.content)
    }

    @Test
    fun `bad credentials do not reveal whether the account exists`() = api {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"Thornwake","password":"wrong"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("invalid_credentials", response.errorCode())
    }

    @Test
    fun `a malformed body is a bad request, not a server error`() = api {
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"Thornwake"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("bad_request", response.errorCode())
    }

    @Test
    fun `enum filters accept their wire spelling`() = api {
        val response = client.get("/api/v1/players/Thornwake/events?type=quest")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("quest", Fakes.lastEventType?.wire)
    }

    @Test
    fun `an unknown enum value is rejected`() = api {
        val response = client.get("/api/v1/players/Thornwake/events?type=trading")

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals("validation_failed", response.errorCode())
    }

    @Test
    fun `staff flag follows the account roles`() = api {
        val response = client.get("/api/v1/auth/session") {
            header(HttpHeaders.Authorization, "Bearer ${Fakes.STAFF_TOKEN}")
        }

        val account = response.json()["account"]!!.jsonObject
        assertEquals("true", account["staff"]!!.jsonPrimitive.content)
        assertTrue(account["roles"]!!.jsonArray.any { it.jsonPrimitive.content == AccountRole.Administrator.wire })
    }

    private fun api(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        val services = Fakes.services()
        application {
            apiPlugins(services)
            routing {
                api(services, secureCookies = false)
            }
        }
        block()
    }

    private suspend fun HttpResponse.json() = Json.parseToJsonElement(bodyAsText()).jsonObject

    private suspend fun HttpResponse.errorCode() = json()["error"]!!.jsonObject["code"]!!.jsonPrimitive.content
}

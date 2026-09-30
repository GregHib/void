package world.gregs.voidps.web.api.route

import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.web.api.ApiException
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AllowAnyOriginTest {

    @AfterEach
    fun teardown() {
        PlayerSnapshot.clear()
    }

    @Test
    fun `Responses and errors under the route allow any origin`() = testApplication {
        application {
            apiPlugins()
            routing {
                route("/public") {
                    allowAnyOrigin()
                    get("/ok") { call.respondText("ok") }
                    get("/missing") { throw ApiException.NotFound("thing", "id") }
                }
                get("/private") { call.respondText("ok") }
            }
        }

        val ok = client.get("/public/ok")
        assertEquals(HttpStatusCode.OK, ok.status)
        assertEquals("*", ok.headers[HttpHeaders.AccessControlAllowOrigin])

        val missing = client.get("/public/missing")
        assertEquals(HttpStatusCode.NotFound, missing.status)
        assertEquals("*", missing.headers[HttpHeaders.AccessControlAllowOrigin])

        assertNull(client.get("/private").headers[HttpHeaders.AccessControlAllowOrigin])
    }

    @Test
    fun `World players list sharing the players route allows any origin exactly once`() = testApplication {
        application {
            apiPlugins()
            routing {
                route(API_PATH) {
                    route("/players") {
                        allowAnyOrigin()
                        get("/search") { call.respondText("[]") }
                    }
                    worldsRoutes()
                }
            }
        }

        val response = client.get("$API_PATH/players")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf("*"), response.headers.getAll(HttpHeaders.AccessControlAllowOrigin))
    }
}

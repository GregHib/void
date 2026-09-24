package world.gregs.voidps.web.api.route

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.web.api.model.ServerInfo
import kotlin.test.assertEquals

class WorldsRoutesTest {

    @AfterEach
    fun teardown() {
        Settings.clear()
    }

    @Test
    fun `Info reports live world settings`() = testApplication {
        Settings.load(mapOf("world.members" to "true", "world.players.max" to "100", "world.experienceRate" to "1.5", "world.itemDropRate" to "2.0"))
        application {
            apiPlugins()
            routing { route(API_PATH) { worldsRoutes() } }
        }

        val response = client.get("$API_PATH/info")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("*", response.headers[HttpHeaders.AccessControlAllowOrigin])
        val info = apiJson.decodeFromString<ServerInfo>(response.bodyAsText())
        assertEquals("Online", info.status)
        assertEquals(true, info.members)
        assertEquals(0, info.players)
        assertEquals(100, info.capacity)
        assertEquals(1.5, info.xpRate)
        assertEquals(2.0, info.dropRate)
    }

    @Test
    fun `Ping responds with no content`() = testApplication {
        application {
            apiPlugins()
            routing { route(API_PATH) { worldsRoutes() } }
        }

        val response = client.get("$API_PATH/ping")

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals("*", response.headers[HttpHeaders.AccessControlAllowOrigin])
        assertEquals("no-store", response.headers[HttpHeaders.CacheControl])
    }
}

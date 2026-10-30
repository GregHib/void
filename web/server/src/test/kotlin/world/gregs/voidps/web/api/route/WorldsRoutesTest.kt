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
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.type.Tile
import world.gregs.voidps.web.api.model.PlayerLocation
import world.gregs.voidps.web.api.model.ServerInfo
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldsRoutesTest {

    @AfterEach
    fun teardown() {
        Settings.clear()
        Players.clear()
        PlayerSnapshot.clear()
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

    @Test
    fun `Players lists locations except wilderness and opted out players`() = testApplication {
        application {
            apiPlugins()
            routing { route(API_PATH) { worldsRoutes() } }
        }
        Players.add(Player(index = 1, tile = Tile(3222, 3218), accountName = "shown"))
        Players.add(Player(index = 2, tile = Tile(3100, 3600), accountName = "wildy").apply { set("in_wilderness", true) })
        Players.add(Player(index = 3, tile = Tile(3200, 3200, 1), accountName = "hidden").apply { set("world_map_hidden", true) })

        val response = client.get("$API_PATH/players")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("*", response.headers[HttpHeaders.AccessControlAllowOrigin])
        val players = apiJson.decodeFromString<List<PlayerLocation>>(response.bodyAsText())
        assertEquals(listOf(PlayerLocation("shown", 3222, 3218, 0)), players)
    }

    @Test
    fun `Players are cached between refreshes`() = testApplication {
        application {
            apiPlugins()
            routing { route(API_PATH) { worldsRoutes() } }
        }
        Players.add(Player(index = 1, tile = Tile(3222, 3218), accountName = "first"))
        val first = client.get("$API_PATH/players").bodyAsText()

        Players.add(Player(index = 2, tile = Tile(3200, 3200), accountName = "second"))
        val second = client.get("$API_PATH/players")

        assertEquals(first, second.bodyAsText())
        assertTrue(second.headers[HttpHeaders.CacheControl]!!.startsWith("public, max-age="))
    }
}

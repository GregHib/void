package world.gregs.voidps.web.api.route

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.file.FileStorage
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HiscoresRoutesTest {

    private val storage = FileStorage(File("../data/saves"))

    @Test
    fun `overall leaderboard lists real accounts`() = testApplication {
        application {
            apiPlugins()
            routing { api(storage) }
        }
        val response = client.get("/api/v1/hiscores/overall")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Ebp90"))
    }

    @Test
    fun `player profile is found case-insensitively`() = testApplication {
        application {
            apiPlugins()
            routing { api(storage) }
        }
        val response = client.get("/api/v1/players/ebp90")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("\"name\":\"Ebp90\""))
    }

    @Test
    fun `unknown skill returns 404`() = testApplication {
        application {
            apiPlugins()
            routing { api(storage) }
        }
        val response = client.get("/api/v1/hiscores/skills/not-a-skill")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}

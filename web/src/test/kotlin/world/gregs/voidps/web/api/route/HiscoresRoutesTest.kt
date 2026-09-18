package world.gregs.voidps.web.api.route

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.file.FileStorage
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HiscoresRoutesTest {

    private val storage = FileStorage(File("../engine/src/test/resources/"))

    @Test
    fun `overall leaderboard lists real accounts`() = testApplication {
        application {
            apiPlugins()
            routing { api(storage, QuestDefinitions()) }
        }
        val response = client.get("/api/v1/hiscores/overall")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Durial321"))
    }

    @Test
    fun `player profile is found case-insensitively`() = testApplication {
        val questDefinitions = QuestDefinitions()
        questDefinitions.definitions = arrayOf()
        questDefinitions.ids = emptyMap()
        application {
            apiPlugins()
            routing { api(storage, questDefinitions) }
        }
        val response = client.get("/api/v1/players/durial_321")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("\"name\":\"Durial321\""))
    }

    @Test
    fun `unknown skill returns 404`() = testApplication {
        application {
            apiPlugins()
            routing { api(storage, QuestDefinitions()) }
        }
        val response = client.get("/api/v1/hiscores/skills/not-a-skill")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}

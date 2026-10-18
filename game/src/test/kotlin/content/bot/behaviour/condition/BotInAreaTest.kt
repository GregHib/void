package content.bot.behaviour.condition

import content.bot.behaviour.requirements
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import world.gregs.config.Config
import java.io.File

class BotInAreaTest {

    @TempDir
    lateinit var dir: File

    private fun parse(area: String): List<Condition> {
        val file = dir.resolve("test.toml")
        file.writeText("setup = [\n    { area = $area },\n]\n")
        val list = mutableListOf<Pair<String, List<Map<String, Any>>>>()
        Config.fileReader(file.path) {
            while (nextPair()) {
                key()
                requirements(list)
            }
        }
        return Condition.parse(list, "test")
    }

    @Test
    fun `Parse single id`() {
        assertEquals(listOf(BotInArea("field")), parse("{ id = \"field\" }"))
    }

    @Test
    fun `Parse list of ids and tags`() {
        val condition = parse("{ ids = [\"field_a\", \"field_b\"], tags = [\"cow_field\"] }").single() as BotInArea

        assertEquals(listOf("field_a", "field_b"), condition.ids)
        assertEquals(setOf("cow_field"), condition.tags)
    }

    @Test
    fun `Parse comma separated ids`() {
        val condition = parse("{ ids = \"field_a,field_b\" }").single() as BotInArea

        assertEquals(listOf("field_a", "field_b"), condition.ids)
    }
}

package content.area.misthalin.lumbridge.castle

import WorldTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Patrol
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HansTest : WorldTest() {

    @Test
    fun `Hans resumes his patrol when idle`() {
        val hans = createNPC("hans", Tile(3219, 3222))
        assertTrue(hans.mode is Patrol)
        hans["patrol_index"] = 3

        hans.mode = EmptyMode
        tick()

        assertTrue(hans.mode is Patrol)
        assertEquals(3, hans["patrol_index", 0])
    }
}

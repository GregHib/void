package content.area.fremennik_province.lighthouse

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import java.util.Properties

class LighthouseDoorTest : WorldTest() {

    private fun skipMissing() {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = "true"
        Settings.load(properties)
    }

    @Test
    fun `Walk in and out of the lighthouse`() {
        skipMissing()
        val player = createPlayer(Tile(2509, 3634))
        val door = GameObjects.find(Tile(2509, 3636), "lighthouse_doorway")

        player.objectOption(door, "Walk-through")
        tick(4)
        assertEquals(Tile(2509, 3636), player.tile)
        assertTrue(player.containsMessage("You unlock the Lighthouse front door."))

        player.objectOption(door, "Walk-through")
        tick(4)
        assertEquals(Tile(2509, 3635), player.tile)
    }

    @Test
    fun `Can't enter the lighthouse without the quest`() {
        val player = createPlayer(Tile(2509, 3635))
        val door = GameObjects.find(Tile(2509, 3636), "lighthouse_doorway")

        player.objectOption(door, "Walk-through")
        tick(2)

        assertEquals(Tile(2509, 3635), player.tile)
        assertTrue(player.containsMessage("You can't see any way to open the door."))
    }
}

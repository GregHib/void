package content.area.misthalin.varrock.digsite

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

class SenntistenTempleTest : WorldTest() {

    private fun skipMissing() {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = "true"
        Settings.load(properties)
    }

    @Test
    fun `Winch down to the temple and switch to ancient curses`() {
        skipMissing()
        val player = createPlayer(Tile(3353, 3416))
        val winch = GameObjects.find(Tile(3352, 3417), "digsite_winch_west")

        player.objectOption(winch, "Operate")
        tick(3)
        assertEquals(Tile(3177, 5731), player.tile)

        val altar = GameObjects.find(Tile(3181, 5705), "prayer_altar_zaros_senntisten")
        player.objectOption(altar, "Pray")
        tick(40)

        assertEquals("curses", player["prayers", "normal"])
    }

    @Test
    fun `Climb the rope out of the temple`() {
        val player = createPlayer(Tile(3177, 5731))
        val rope = GameObjects.find(Tile(3177, 5730), "senntisten_temple_rope")

        player.objectOption(rope, "Climb-up")
        tick(5)

        assertEquals(Tile(3353, 3416), player.tile)
    }

    @Test
    fun `Winch is locked without the temple at senntisten`() {
        val player = createPlayer(Tile(3353, 3416))
        val winch = GameObjects.find(Tile(3352, 3417), "digsite_winch_west")

        player.objectOption(winch, "Operate")
        tick(3)

        assertEquals(Tile(3353, 3416), player.tile)
        assertTrue(player.containsMessage("You must have completed The Temple at Senntisten quest."))
    }
}

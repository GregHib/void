package content.area.misthalin.ham_hideout

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HamHideoutTest : WorldTest() {

    @Test
    fun `Cannot open locked trapdoor`() {
        val player = createPlayer(Tile(3165, 3252))
        val trapdoor = GameObjects.add("trapdoor_24_closed", Tile(3165, 3252))

        player.objectOption(trapdoor, "Open")
        tick()

        assertTrue(player.containsMessage("This trapdoor seems totally locked."))
        assertNotNull(GameObjects.findOrNull(trapdoor.tile, "trapdoor_24_closed"))
    }

    @Test
    fun `Closed trapdoor still requires lockpicking after being opened`() {
        val player = createPlayer(Tile(3165, 3252))
        val trapdoor = GameObjects.add("trapdoor_24_closed", Tile(3165, 3252))

        player.objectOption(trapdoor, "Pick-lock")
        tick(7)

        val opened = GameObjects.find(trapdoor.tile, "trapdoor_24_opened")
        player.objectOption(opened, "Close")
        tick()

        val closed = GameObjects.find(trapdoor.tile, "trapdoor_24_closed")
        player.objectOption(closed, "Open")
        tick()

        assertTrue(player.containsMessage("This trapdoor seems totally locked."))
        assertNotNull(GameObjects.findOrNull(trapdoor.tile, "trapdoor_24_closed"))
    }

    @Test
    fun `Trapdoor requires lockpicking after automatic closure`() {
        val player = createPlayer(Tile(3165, 3252))
        val trapdoor = GameObjects.add("trapdoor_24_closed", Tile(3165, 3252))

        player.objectOption(trapdoor, "Pick-lock")
        tick(7)
        tick(TimeUnit.MINUTES.toTicks(5))

        val closed = GameObjects.find(trapdoor.tile, "trapdoor_24_closed")
        player.objectOption(closed, "Open")
        tick()

        assertTrue(player.containsMessage("This trapdoor seems totally locked."))
        assertNotNull(GameObjects.findOrNull(trapdoor.tile, "trapdoor_24_closed"))
    }

    @Test
    fun `Cannot open prison door directly from inside`() {
        val player = createPlayer(Tile(3183, 9612))
        val door = GameObjects.find(Tile(3183, 9611), "door_148_closed")

        player.objectOption(door, "Open")
        tick()

        assertTrue(player.containsMessage("This door seems very secure."))
    }
}

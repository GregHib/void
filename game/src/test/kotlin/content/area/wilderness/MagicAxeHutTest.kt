package content.area.wilderness

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MagicAxeHutTest : WorldTest() {
    @Test
    fun `outside door cannot be opened normally`() {
        val door = GameObjects.add("door_68_closed", Tile(3190, 3956))
        val player = createPlayer(Tile(3190, 3955))

        player.objectOption(door, "Open")
        tick(8)

        assertEquals(Tile(3190, 3955), player.tile)
        assertTrue(player.containsMessage("The door is locked."))
    }

    @Test
    fun `inside door opens normally`() {
        val door = GameObjects.add("door_68_closed", Tile(3190, 3956))
        val player = createPlayer(Tile(3190, 3957))

        player.objectOption(door, "Open")
        tick(8)

        assertEquals(Tile(3189, 3956), player.tile)
        assertTrue(player.containsMessage("You go through the door."))
    }

    @Test
    fun `inside door is already unlocked`() {
        val door = GameObjects.add("door_68_closed", Tile(3190, 3956))
        val player = createPlayer(Tile(3190, 3957))

        player.objectOption(door, "Pick-lock")
        tick(8)

        assertEquals(Tile(3189, 3956), player.tile)
        assertTrue(player.containsMessage("You go through the door."))
        assertTrue(player.containsMessage("The door is already unlocked."))
    }
}

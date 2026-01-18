package world.gregs.voidps.engine.entity.item.floor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.type.Tile

class FloorItemTrackingTest {

    private lateinit var tracking: FloorItemTracking

    @BeforeEach
    fun setup() {
        Players.clear()
        Players.add(Player(accountName = "player"))
        tracking = FloorItemTracking()
        ItemDefinitions.set(arrayOf(ItemDefinition(0)), mapOf("item" to 0))
    }

    @Test
    fun `Private items are revealed after timer`() {
        val item = FloorItems.add(Tile.EMPTY, "item", revealTicks = 10, owner = "player")
        FloorItems.run()

        repeat(10) {
            assertEquals("player", item.owner)
            tracking.run()
        }

        assertFalse(item.reveal())
        assertNull(item.owner)
        assertEquals(0, item.revealTicks)
    }

    @Test
    fun `Public items are removed after timer`() {
        val item = FloorItems.add(Tile.EMPTY, "item", disappearTicks = 10)
        FloorItems.run()

        repeat(10) {
            tracking.run()
        }

        assertFalse(FloorItems.at(Tile.EMPTY).contains(item))
    }

    @Test
    fun `Removal timer isn't counting down while reveal timer is active`() {
        val item = FloorItems.add(Tile.EMPTY, "item", revealTicks = 10, disappearTicks = 10, owner = "player")
        FloorItems.run()

        repeat(10) {
            assertEquals("player", item.owner)
            tracking.run()
        }

        repeat(5) {
            assertNull(item.owner)
            tracking.run()
        }

        assertEquals(0, item.revealTicks)
        assertEquals(5, item.disappearTicks)
        assertFalse(item.reveal())
        assertFalse(item.remove())
    }

    @Test
    fun `Public items revealed and removed after timers`() {
        val item = FloorItems.add(Tile.EMPTY, "item", revealTicks = 10, disappearTicks = 10, owner = "player")
        FloorItems.run()

        repeat(10) {
            tracking.run()
        }
        assertTrue(FloorItems.at(Tile.EMPTY).contains(item))
        assertNull(item.owner)
        assertEquals(0, item.revealTicks)
        assertEquals(10, item.disappearTicks)
        repeat(10) {
            tracking.run()
        }
        assertFalse(FloorItems.at(Tile.EMPTY).contains(item))
        assertEquals(0, item.disappearTicks)
    }

}

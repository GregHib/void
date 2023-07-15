package world.gregs.voidps.engine.entity.item.floor

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.type.Tile

class FloorItemTrackingTest {

    private lateinit var items: FloorItems
    private lateinit var players: Players
    private lateinit var batches: ZoneBatchUpdates
    private lateinit var tracking: FloorItemTracking

    @BeforeEach
    fun setup() {
        players = mockk(relaxed = true)
        batches = mockk(relaxed = true)
        items = FloorItems(batches, mockk(relaxed = true), mockk(relaxed = true))
        tracking = FloorItemTracking(items, players, batches)
    }

    @Test
    fun `Private items are revealed after timer`() {
        val item = FloorItem(Tile.EMPTY, "item", revealTicks = 10, owner = "player")
        item.def = ItemDefinition.EMPTY
        items.add(item)

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
        val item = FloorItem(Tile.EMPTY, "item", disappearTicks = 10, owner = null)
        item.def = ItemDefinition.EMPTY
        items.add(item)

        repeat(10) {
            tracking.run()
        }

        assertFalse(items[Tile.EMPTY].contains(item))
    }

    @Test
    fun `Public items revealed and removed after timers`() {
        val item = FloorItem(Tile.EMPTY, "item", revealTicks = 10, disappearTicks = 10, owner = "player")
        item.def = ItemDefinition.EMPTY
        items.add(item)

        repeat(10) {
            tracking.run()
        }
        assertTrue(items[Tile.EMPTY].contains(item))
        assertNull(item.owner)
        assertEquals(0, item.revealTicks)
        assertEquals(10, item.disappearTicks)
        repeat(10) {
            tracking.run()
        }
        assertFalse(items[Tile.EMPTY].contains(item))
        assertEquals(0, item.disappearTicks)
    }
}
package world.gregs.voidps.engine.entity.item.floor

import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.type.Tile

class FloorItemTrackingTest {

    private lateinit var items: FloorItems
    private lateinit var tracking: FloorItemTracking

    @BeforeEach
    fun setup() {
        Players.clear()
        Players.add(Player(accountName = "player"))
        items = FloorItems(mockk(relaxed = true))
        tracking = FloorItemTracking(items)
        startKoin {
            modules(
                module {
                    single { ItemDefinitions(arrayOf(ItemDefinition(0))).apply { ids = mapOf("item" to 0) } }
                },
            )
        }
    }

    @Test
    fun `Private items are revealed after timer`() {
        val item = items.add(Tile.EMPTY, "item", revealTicks = 10, owner = "player")
        items.run()

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
        val item = items.add(Tile.EMPTY, "item", disappearTicks = 10)
        items.run()

        repeat(10) {
            tracking.run()
        }

        assertFalse(items[Tile.EMPTY].contains(item))
    }

    @Test
    fun `Removal timer isn't counting down while reveal timer is active`() {
        val item = items.add(Tile.EMPTY, "item", revealTicks = 10, disappearTicks = 10, owner = "player")
        items.run()

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
        val item = items.add(Tile.EMPTY, "item", revealTicks = 10, disappearTicks = 10, owner = "player")
        items.run()

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

    @AfterEach
    fun teardown() {
        stopKoin()
    }
}

package world.gregs.voidps.engine.entity.item.floor

import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemAddition
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemRemoval
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class FloorItemsTest {

    private lateinit var items: FloorItems

    @BeforeEach
    fun setup() {
        mockkObject(ZoneBatchUpdates)
        items = FloorItems()
        val definitions = arrayOf(
            ItemDefinition.EMPTY,
            ItemDefinition(1, cost = 10),
            ItemDefinition(2, stackable = 1),
            ItemDefinition(3, cost = 10),
            ItemDefinition(4, cost = 5),
        )
        ItemDefinitions.init(definitions).apply {
            ids = mapOf("item" to 1, "stackable" to 2, "equal_item" to 3, "cheap_item" to 4)
        }
    }

    @Test
    fun `Add floor items`() {
        val first = items.add(Tile.EMPTY, "item")
        val second = items.add(Tile(10, 10, 1), "item", owner = "player")
        items.run()

        assertEquals(items[Tile.EMPTY].first(), first)
        assertEquals(items[Tile(10, 10, 1)].first(), second)
        assertTrue(items[Tile(100, 100)].isEmpty())
        verify {
            ZoneBatchUpdates.add(Zone.EMPTY, FloorItemAddition(tile = 0, id = 1, amount = 1, owner = null))
            ZoneBatchUpdates.add(Zone(1, 1, 1), FloorItemAddition(tile = 268599306, id = 1, amount = 1, owner = "player"))
        }
    }

    @Test
    fun `Add floor items in order`() {
        val first = items.add(Tile.EMPTY, "item1")
        val second = items.add(Tile.EMPTY, "item2")
        items.run()

        val items = items[Tile.EMPTY]
        assertEquals(items[0], first)
        assertEquals(items[1], second)
    }

    @Test
    fun `Adding two private stackable items combines them`() {
        items.add(Tile.EMPTY, "stackable", owner = "player", disappearTicks = 5, revealTicks = 5)
        items.run()
        items.add(Tile.EMPTY, "stackable", owner = "player", disappearTicks = 10, revealTicks = 10)
        items.run()

        val floorItem = items[Tile.EMPTY].first()
        assertEquals(floorItem.id, "stackable")
        assertEquals(floorItem.amount, 2)
        assertEquals(floorItem.disappearTicks, 5)
        assertEquals(floorItem.revealTicks, 5)
        assertEquals(floorItem.owner, "player")
        verify {
            ZoneBatchUpdates.add(
                Zone.EMPTY,
                FloorItemUpdate(
                    tile = 0,
                    id = 2,
                    stack = 1,
                    combined = 2,
                    owner = "player",
                ),
            )
        }
    }

    @Test
    fun `Don't combine non-stackable items`() {
        val first = items.add(Tile.EMPTY, "item", owner = "player")
        val second = items.add(Tile.EMPTY, "item", owner = "player")
        items.run()
        val items = items[Tile.EMPTY]
        assertEquals(first, items[0])
        assertEquals(second, items[1])
    }

    @Test
    fun `Don't combine two overflowing two private stacks`() {
        val first = items.add(Tile.EMPTY, "item", Int.MAX_VALUE - 10, owner = "player")
        val second = items.add(Tile.EMPTY, "item", 20, owner = "player")
        items.run()

        val items = items[Tile.EMPTY]
        assertEquals(first, items[0])
        assertEquals(second, items[1])
    }

    @Test
    fun `Public items aren't combined`() {
        val first = items.add(Tile.EMPTY, "item", disappearTicks = 5, revealTicks = -1)
        val second = items.add(Tile.EMPTY, "item", owner = "player", disappearTicks = 10, revealTicks = 10)
        items.run()

        val items = items[Tile.EMPTY]
        assertEquals(first, items[0])
        assertEquals(second, items[1])
    }

    @Test
    fun `Remove floor item`() {
        val first = items.add(Tile.EMPTY, "item1")
        val second = items.add(Tile.EMPTY, "item1")
        items.run()

        assertTrue(items.remove(first))
        items.run()
        val items = items[Tile.EMPTY]
        assertFalse(items.contains(first))
        assertTrue(items.contains(second))
        verify {
            ZoneBatchUpdates.add(Zone.EMPTY, FloorItemRemoval(tile = 0, id = -1, owner = null))
        }
    }

    @Test
    fun `Remove lowest value item when limit exceeded`() {
        repeat(128) {
            items.add(Tile.EMPTY, if (it == 25) "cheap_item" else "item")
        }
        items.run()

        val item = items.add(Tile.EMPTY, "item", owner = "player")
        items.run()

        val items = items[Tile.EMPTY]
        assertTrue(items.none { it.def.cost == 5 })
        assertEquals(item, items[127])
    }

    @Test
    fun `Equal value items are unstacked when limit exceeded`() {
        repeat(128 * 2) {
            items.add(Tile.EMPTY, if (it >= 128) "equal_item" else "item")
        }

        val items = items[Tile.EMPTY]
        assertTrue(items.none { it.id == "item" })
    }

    @Test
    fun `Clear sends batch update`() {
        items.add(Tile.EMPTY, "item")
        items.run()

        items.clear()
        items.run()

        val items = items[Tile.EMPTY]
        assertTrue(items.isEmpty())
        verify {
            ZoneBatchUpdates.add(Zone.EMPTY, FloorItemRemoval(tile = 0, id = 1, owner = null))
        }
    }

    @AfterEach
    fun teardown() {
        unmockkObject(ZoneBatchUpdates)
    }
}

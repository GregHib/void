package world.gregs.voidps.engine.entity.item.floor

import io.mockk.mockk
import io.mockk.verify
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
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemAddition
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemRemoval
import world.gregs.voidps.network.login.protocol.encode.zone.FloorItemUpdate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class FloorItemsTest {

    private lateinit var items: FloorItems
    private lateinit var batches: ZoneBatchUpdates

    @BeforeEach
    fun setup() {
        batches = mockk(relaxed = true)
        items = FloorItems(batches, mockk(relaxed = true))
        startKoin {
            modules(module {
                single {
                    val definitions = arrayOf(
                        ItemDefinition.EMPTY,
                        ItemDefinition(1, cost = 10),
                        ItemDefinition(2, stackable = 1),
                        ItemDefinition(3, cost = 10),
                        ItemDefinition(4, cost = 5)
                    )
                    ItemDefinitions(definitions).apply {
                        ids = mapOf("item" to 1, "stackable" to 2, "equal_item" to 3, "cheap_item" to 4)
                    }
                }
            })
        }
    }

    @Test
    fun `Add floor items`() {
        val first = floorItem("item", Tile.EMPTY)
        items.add(first)
        val second = floorItem("item", Tile(10, 10, 1), owner = "player")
        items.add(second)
        items.run()

        assertEquals(items[Tile.EMPTY].first(), first)
        assertEquals(items[Tile(10, 10, 1)].first(), second)
        assertTrue(items[Tile(100, 100)].isEmpty())
        verify {
            batches.add(Zone.EMPTY, FloorItemAddition(tile = 0, id = 1, amount = 1, owner = null))
            batches.add(Zone(1, 1, 1), FloorItemAddition(tile = 268599306, id = 1, amount = 1, owner = "player"))
        }
    }

    @Test
    fun `Add floor items in order`() {
        val first = floorItem("item1", Tile.EMPTY)
        items.add(first)
        val second = floorItem("item2", Tile.EMPTY)
        items.add(second)
        items.run()

        val items = items[Tile.EMPTY]
        assertEquals(items[0], first)
        assertEquals(items[1], second)
    }

    @Test
    fun `Adding two private stackable items combines them`() {
        val first = floorItem("stackable", Tile.EMPTY, owner = "player", disappear = 5, reveal = 5)
        items.add(first)
        val second = floorItem("stackable", Tile.EMPTY, owner = "player", disappear = 10, reveal = 10)
        items.add(second)
        items.run()

        val floorItem = items[Tile.EMPTY].first()
        assertEquals(floorItem.id, "stackable")
        assertEquals(floorItem.amount, 2)
        assertEquals(floorItem.disappearTicks, 5)
        assertEquals(floorItem.revealTicks, 5)
        assertEquals(floorItem.owner, "player")
        verify {
            batches.add(Zone.EMPTY, FloorItemUpdate(
                tile = 0,
                id = 2,
                stack = 1,
                combined = 2,
                owner = "player"
            ))
        }
    }

    @Test
    fun `Don't combine non-stackable items`() {
        val first = floorItem("item", Tile.EMPTY, owner = "player")
        items.add(first)
        val second = floorItem("item", Tile.EMPTY, owner = "player")
        items.add(second)
        items.run()
        val items = items[Tile.EMPTY]
        assertEquals(first, items[0])
        assertEquals(second, items[1])
    }

    @Test
    fun `Don't combine two overflowing two private stacks`() {
        val first = floorItem("item", Tile.EMPTY, Int.MAX_VALUE - 10, owner = "player")
        items.add(first)
        val second = floorItem("item", Tile.EMPTY, 20, owner = "player")
        items.add(second)
        items.run()

        val items = items[Tile.EMPTY]
        assertEquals(first, items[0])
        assertEquals(second, items[1])
    }

    @Test
    fun `Public items aren't combined`() {
        val first = floorItem("item", Tile.EMPTY, owner = null, disappear = 5, reveal = -1)
        items.add(first)
        val second = floorItem("item", Tile.EMPTY, owner = "player", disappear = 10, reveal = 10)
        items.add(second)
        items.run()

        val items = items[Tile.EMPTY]
        assertEquals(first, items[0])
        assertEquals(second, items[1])
    }

    @Test
    fun `Remove floor item`() {
        val first = floorItem("item1", Tile.EMPTY)
        items.add(first)
        val second = floorItem("item1", Tile.EMPTY)
        items.add(second)

        assertTrue(items.remove(first))
        items.run()
        val items = items[Tile.EMPTY]
        assertFalse(items.contains(first))
        assertTrue(items.contains(second))
        verify {
            batches.add(Zone.EMPTY, FloorItemRemoval(tile = 0, id = -1, owner = null))
        }
    }

    @Test
    fun `Remove lowest value item when limit exceeded`() {
        repeat(128) {
            val item = floorItem(if (it == 25) "cheap_item" else "item", Tile.EMPTY)
            items.add(item)
        }

        val item = floorItem("item", Tile.EMPTY, owner = "player")
        items.add(item)
        items.run()

        val items = items[Tile.EMPTY]
        assertTrue(items.none { it.def.cost == 5 })
        assertEquals(item, items[127])
    }

    @Test
    fun `Equal value items are unstacked when limit exceeded`() {
        repeat(128 * 2) {
            val item = floorItem(if (it >= 128) "equal_item" else "item", Tile.EMPTY)
            items.add(item)
        }

        val items = items[Tile.EMPTY]
        assertTrue(items.none { it.id == "item" })
    }

    @Test
    fun `Clear sends batch update`() {
        val first = floorItem("item", Tile.EMPTY)
        items.add(first)

        items.clear()
        items.run()

        val items = items[Tile.EMPTY]
        assertTrue(items.isEmpty())
        verify {
            batches.add(Zone.EMPTY, FloorItemRemoval(tile = 0, id = 1, owner = null))
        }
    }

    private fun floorItem(id: String, tile: Tile, amount: Int = 1, disappear: Int = -1, reveal: Int = -1, charges: Int = 0, owner: String? = null): FloorItem {
        return FloorItem(tile, id, amount, disappear, reveal, charges, owner)
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }
}
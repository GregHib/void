package world.gregs.voidps.engine.entity.item.floor

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.network.encode.chunk.FloorItemAddition
import world.gregs.voidps.network.encode.chunk.FloorItemRemoval
import world.gregs.voidps.network.encode.chunk.FloorItemUpdate

class FloorItemStorageTest {

    private lateinit var items: FloorItemStorage
    private lateinit var batches: ChunkBatchUpdates

    @BeforeEach
    fun setup() {
        batches = mockk(relaxed = true)
        items = FloorItemStorage(batches, mockk())
    }

    @Test
    fun `Add floor items`() {
        val first = floorItem("item", Tile.EMPTY)
        items.add(first)
        val second = floorItem("item", Tile(10, 10))
        items.add(second)

        assertEquals(items.get(Tile.EMPTY).first(), first)
        assertEquals(items.get(Tile(10, 10)).first(), second)
        assertTrue(items.get(Tile(100, 100)).isEmpty())
        verify {
            batches.add(Chunk.EMPTY, any<FloorItemAddition>())
        }
    }

    @Test
    fun `Add floor items in order`() {
        val first = floorItem("item1", Tile.EMPTY)
        items.add(first)
        val second = floorItem("item2", Tile.EMPTY)
        items.add(second)

        val items = items.get(Tile.EMPTY)
        assertEquals(items[0], first)
        assertEquals(items[1], second)
    }

    @Test
    fun `Adding two private stackable items combines them`() {
        val first = floorItem("item", Tile.EMPTY, owner = 123, disappear = 5, reveal = 5)
        first.def = ItemDefinition(stackable = 1)
        items.add(first)
        val second = floorItem("item", Tile.EMPTY, owner = 123, disappear = 10, reveal = 10)
        second.def = ItemDefinition(stackable = 1)
        items.add(second)

        val item = items.get(Tile.EMPTY).first()
        assertEquals(item.id, "item")
        assertEquals(item.amount, 2)
        assertEquals(item.disappearTimer, 5)
        assertEquals(item.revealTimer, 5)
        assertEquals(item.owner, 123)
        verify {
            batches.add(Chunk.EMPTY, FloorItemUpdate(
                id = -1,
                tileOffset = 0,
                stack = 1,
                combined = 2,
                owner = 123
            ))
        }
    }

    @Test
    fun `Don't combine non-stackable items`() {
        val first = floorItem("item", Tile.EMPTY, owner = 123)
        items.add(first)
        val second = floorItem("item", Tile.EMPTY, owner = 123)
        items.add(second)
        val items = items.get(Tile.EMPTY)
        assertEquals(first, items[0])
        assertEquals(second, items[1])
    }

    @Test
    fun `Don't combine two overflowing two private stacks`() {
        val first = floorItem("item", Tile.EMPTY, Int.MAX_VALUE - 10, owner = 123)
        items.add(first)
        val second = floorItem("item", Tile.EMPTY, 20, owner = 123)
        items.add(second)

        val items = items.get(Tile.EMPTY)
        assertEquals(first, items[0])
        assertEquals(second, items[1])
    }

    @Test
    fun `Public items aren't combined`() {
        val first = floorItem("item", Tile.EMPTY, owner = 0, disappear = 5, reveal = -1)
        items.add(first)
        val second = floorItem("item", Tile.EMPTY, owner = 123, disappear = 10, reveal = 10)
        items.add(second)

        val items = items.get(Tile.EMPTY)
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
        val items = items.get(Tile.EMPTY)
        assertFalse(items.contains(first))
        assertTrue(items.contains(second))
        verify {
            batches.add(Chunk.EMPTY, FloorItemRemoval(-1, 0, 0))
        }
    }

    @Test
    fun `Remove lowest value item when limit exceeded`() {
        repeat(128) {
            val item = floorItem("item", Tile.EMPTY)
            item.def = ItemDefinition(cost = if (it == 25) 5 else 10)
            items.add(item)
        }

        val item = floorItem("item", Tile.EMPTY, owner = 123)
        item.def = ItemDefinition(cost = 10)
        items.add(item)

        val items = items.get(Tile.EMPTY)
        assertTrue(items.none { it.def.cost == 5 })
        assertEquals(item, items[127])
    }

    @Test
    fun `Equal value items are unstacked when limit exceeded`() {
        repeat(128 * 2) {
            val item = floorItem(if (it >= 128) "equal_item" else "item", Tile.EMPTY)
            item.def = ItemDefinition(cost = 10)
            items.add(item)
        }

        val items = items.get(Tile.EMPTY)
        assertTrue(items.none { it.id == "item" })
    }

    @Test
    fun `Clear sends batch update`() {
        val first = floorItem("item", Tile.EMPTY)
        items.add(first)

        items.clear()

        val items = items.get(Tile.EMPTY)
        assertTrue(items.isEmpty())
        verify {
            batches.add(Chunk.EMPTY, FloorItemRemoval(-1, 0, 0))
        }
    }

    private fun floorItem(id: String, tile: Tile, amount: Int = 1, disappear: Int = -1, reveal: Int = -1, owner: Int = 0): FloorItem {
        val item = FloorItem(id, tile, amount, disappear, reveal, owner)
        item.def = ItemDefinition.EMPTY
        return item
    }
}
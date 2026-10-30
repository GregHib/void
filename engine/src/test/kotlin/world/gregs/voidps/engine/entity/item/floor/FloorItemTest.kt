package world.gregs.voidps.engine.entity.item.floor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.type.Tile

class FloorItemTest {

    @BeforeEach
    fun setup() {
        ItemDefinitions.set(arrayOf(ItemDefinition(0), ItemDefinition(1, stackable = 1)), mapOf("item" to 0, "stackable" to 1))
    }

    @Test
    fun `Merge one item with another`() {
        val first = FloorItem(Tile.EMPTY, "stackable", 100)
        val second = FloorItem(Tile.EMPTY, "stackable", 150)
        assertTrue(first.merge(second))
        assertEquals(250, first.amount)
    }

    @Test
    fun `Max int amounts won't merge`() {
        val first = FloorItem(Tile.EMPTY, "stackable", 100)
        val second = FloorItem(Tile.EMPTY, "item", Int.MAX_VALUE - 99)
        assertFalse(first.merge(second))
        assertEquals(100, first.amount)
    }

    @Test
    fun `Private items count down to reveal`() {
        val item = FloorItem(Tile.EMPTY, "item", revealTicks = 2, disappearTicks = 3, owner = "player")
        assertEquals(2, item.lifecycle)
        assertEquals(FloorItem.NONE, item.tick())
        assertEquals(FloorItem.REVEAL, item.tick())
        assertEquals(-3, item.lifecycle)
    }

    @Test
    fun `Public items count up to removal`() {
        val item = FloorItem(Tile.EMPTY, "item", disappearTicks = 2)
        assertEquals(-2, item.lifecycle)
        assertEquals(FloorItem.NONE, item.tick())
        assertEquals(FloorItem.REMOVE, item.tick())
    }

    @Test
    fun `Don't reveal public items`() {
        val item = FloorItem(Tile.EMPTY, "item", 100, revealTicks = 10)
        assertTrue(item.lifecycle <= 0)
    }

    @Test
    fun `Remove private or public items`() {
        val first = FloorItem(Tile.EMPTY, "item", 100, disappearTicks = FloorItems.IMMEDIATE, owner = "player")
        assertEquals(FloorItem.REMOVE, first.tick())
        val second = FloorItem(Tile.EMPTY, "item", 100, disappearTicks = FloorItems.IMMEDIATE, owner = null)
        assertEquals(FloorItem.REMOVE, second.tick())
    }

    @Test
    fun `Never remove item`() {
        val item = FloorItem(Tile.EMPTY, "item", 100, disappearTicks = FloorItems.NEVER, owner = "player")
        assertEquals(0, item.lifecycle)
        assertEquals(FloorItem.NONE, item.tick())
    }

    @Test
    fun `Never reveal item`() {
        val item = FloorItem(Tile.EMPTY, "item", 100, revealTicks = FloorItems.NEVER, owner = "player")
        assertEquals(0, item.lifecycle)
        assertEquals(FloorItem.NONE, item.tick())
    }

    @Test
    fun `Reset restarts lifecycle`() {
        val item = FloorItem(Tile.EMPTY, "item", revealTicks = 5, disappearTicks = 5, owner = "player")
        item.tick()
        item.reset(revealTicks = 10, disappearTicks = 20)
        assertEquals(10, item.lifecycle)
        assertEquals(20, item.disappearTicks)
    }
}

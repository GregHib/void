package world.gregs.voidps.engine.entity.item.floor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.map.Tile

class FloorItemTest {

    @Test
    fun `Merge one item with another`() {
        val first = FloorItem(Tile.EMPTY, "item", 100)
        first.def = ItemDefinition(stackable = 1)
        val second = FloorItem(Tile.EMPTY, "item", 150)
        assertTrue(first.merge(second))
        assertEquals(250, first.amount)
    }

    @Test
    fun `Max int amounts won't merge`() {
        val first = FloorItem(Tile.EMPTY, "item", 100)
        first.def = ItemDefinition(stackable = 1)
        val second = FloorItem(Tile.EMPTY, "item", Int.MAX_VALUE - 99)
        assertFalse(first.merge(second))
        assertEquals(100, first.amount)
    }

    @Test
    fun `Don't re-reveal public items`() {
        val first = FloorItem(Tile.EMPTY, "item", 100, revealTicks = FloorItems.IMMEDIATE, owner = "player")
        assertFalse(first.reveal())
        val second = FloorItem(Tile.EMPTY, "item", 100, revealTicks = FloorItems.IMMEDIATE)
        assertFalse(second.reveal())
    }

    @Test
    fun `Remove private or public items`() {
        val first = FloorItem(Tile.EMPTY, "item", 100, disappearTicks = FloorItems.IMMEDIATE, owner = "player")
        assertTrue(first.remove())
        val second = FloorItem(Tile.EMPTY, "item", 100, disappearTicks = FloorItems.IMMEDIATE, owner = null)
        assertTrue(second.remove())
    }

    @Test
    fun `Never remove item`() {
        val first = FloorItem(Tile.EMPTY, "item", 100, disappearTicks = FloorItems.NEVER, owner = "player")
        assertFalse(first.remove())
    }

    @Test
    fun `Never reveal item`() {
        val first = FloorItem(Tile.EMPTY, "item", 100, revealTicks = FloorItems.NEVER, owner = "player")
        assertFalse(first.reveal())
    }

}
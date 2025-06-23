package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ItemDropTest {

    @Test
    fun `Item drop defaults`() {
        val drop = ItemDrop(id = "item")
        assertEquals(1..1, drop.amount)
        assertEquals(1, drop.chance)
        assertNull(drop.predicate)
    }

    @ParameterizedTest
    @ValueSource(strings = ["nothing", "", "  "])
    fun `Nothing drops are converted to empty items`(id: String) {
        val drop = ItemDrop(id = id)
        val item = drop.toItem()
        assertTrue(item.isEmpty())
    }

    @Test
    fun `Converted to item`() {
        val drop = ItemDrop(
            id = "bones",
            amount = 1..5,
        )
        val item = drop.toItem()
        assertFalse(item.isEmpty())
        assertEquals("bones", item.id)
        assertTrue(item.amount in 1..5)
    }
}

package world.gregs.voidps.engine.entity.item.drop

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.NoRestrictions
import world.gregs.voidps.engine.inv.stack.ItemDependentStack

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
package world.gregs.voidps.world.activity.bank

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ContainerData
import world.gregs.voidps.engine.entity.item.Item

internal class BankSortTest {

    @Test
    fun `Sort moves empty indices to back`() {
        // Given
        val container = items(null, TYPE_1 to 1, null, TYPE_2 to 2, null, TYPE_3 to 3, null)
        // When
        container.sort()
        // Then
        assertEquals(items(TYPE_1 to 1, TYPE_2 to 2, TYPE_3 to 3, null, null, null, null), container)
    }

    @Test
    fun `Sort doesn't change order of items`() {
        // Given
        val container = items(TYPE_3 to 3, TYPE_2 to 2, TYPE_1 to 1)
        // When
        container.sort()
        // Then
        assertEquals(items(TYPE_3 to 3, TYPE_2 to 2, TYPE_1 to 1), container)
    }

    private fun items(vararg items: Pair<String, Int>?) = Container(
        data = ContainerData(items.map { Item(it?.first ?: "", it?.second ?: 0, def = ItemDefinition.EMPTY) }.toTypedArray())
    )

    companion object {
        private const val TYPE_1 = "type_1"
        private const val TYPE_2 = "type_2"
        private const val TYPE_3 = "type_3"
    }
}
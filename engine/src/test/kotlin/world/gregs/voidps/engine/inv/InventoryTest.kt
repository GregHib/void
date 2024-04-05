package world.gregs.voidps.engine.inv

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.inv.remove.DefaultItemRemovalChecker
import world.gregs.voidps.engine.inv.remove.ItemRemovalChecker
import world.gregs.voidps.engine.inv.remove.ShopItemRemovalChecker
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.DependentOnItem
import world.gregs.voidps.engine.inv.stack.ItemStackingRule
import world.gregs.voidps.engine.inv.stack.NeverStack

internal class InventoryTest {
    private lateinit var inventory: Inventory
    private lateinit var items: Array<Item>
    private lateinit var minimumAmounts: IntArray
    private lateinit var events: EventDispatcher

    @BeforeEach
    fun setup() {
        events = mockk(relaxed = true)
        items = Array(10) { Item("", 0) }
        minimumAmounts = IntArray(10)
        inventory = inventory()
    }

    private fun inventory(
        id: String = "123",
        items: Array<Item> = this.items,
        stackRule: ItemStackingRule = AlwaysStack,
        removalCheck: ItemRemovalChecker = DefaultItemRemovalChecker
    ): Inventory = spyk(
        Inventory(
            data = items,
            id = id,
            stackRule = stackRule,
            removalCheck = removalCheck
        ).apply {
            transaction.changes.bind(events)
        }
    )

    @Test
    fun `Stackable true if always stack mode`() {
        // Given
        val id = "1"
        inventory = inventory(
            items = emptyArray(),
            stackRule = AlwaysStack
        )
        // When
        val stackable = inventory.stackable(id)
        // Then
        assertTrue(stackable)
    }

    @Test
    fun `Stackable false if never stack mode`() {
        // Given
        val id = "1"
        inventory = inventory(
            items = emptyArray(),
            stackRule = NeverStack
        )
        // When
        val stackable = inventory.stackable(id)
        // Then
        assertFalse(stackable)
    }

    @Test
    fun `Stackable true if normal stack mode and item stacks`() {
        // Given
        val id = "1"
        val definitions: ItemDefinitions = mockk(relaxed = true)
        inventory = inventory(
            items = emptyArray(),
            stackRule = DependentOnItem(definitions)
        )
        every { definitions.get(id) } returns ItemDefinition(stackable = 1)
        // When
        val stackable = inventory.stackable(id)
        // Then
        assertTrue(stackable)
    }

    @Test
    fun `Stackable false if normal stack mode and non-stackable item`() {
        // Given
        val id = "1"
        val definitions: ItemDefinitions = mockk(relaxed = true)
        inventory = inventory(
            items = emptyArray(),
            stackRule = DependentOnItem(definitions)
        )
        every { definitions.get(id) } returns ItemDefinition(stackable = 0)
        // When
        val stackable = inventory.stackable(id)
        // Then
        assertFalse(stackable)
    }

    @Test
    fun `Spaces counts number of empty items`() {
        // Given
        inventory = inventory(
            removalCheck = ShopItemRemovalChecker
        )
        inventory.transaction {
            repeat(8) {
                set(it, Item("item"))
            }
        }
        // When
        val spaces = inventory.spaces
        // Then
        assertEquals(2, spaces)
    }

    @Test
    fun `Bounds checks over maximum`() {
        // Given
        val index = items.size + 1
        // When
        val within = inventory.inBounds(index)
        // Then
        assertFalse(within)
    }

    @Test
    fun `Bounds checks under minimum`() {
        // Given
        val index = -1
        // When
        val within = inventory.inBounds(index)
        // Then
        assertFalse(within)
    }

    @Test
    fun `Inventory is empty`() {
        assertTrue(inventory.isEmpty())
        items[4] = Item("123", 10)
        assertFalse(inventory.isEmpty())
    }

    @Test
    fun `Get inventory item by index`() {
        // Given
        val index = 1
        val id = "100"
        items[index] = Item(id, 0)
        // When
        val item = inventory[index].id
        // Then
        assertEquals(id, item)
    }

    @Test
    fun `Get inventory item out of index`() {
        // Given
        val index = -2
        // When
        val item = inventory.getOrNull(index)?.id
        // Then
        assertNull(item)
    }

    @Test
    fun `Get inventory amount out of index`() {
        // Given
        val index = -2
        // When
        val count = inventory.getOrNull(index)?.amount
        // Then
        assertNull(count)
    }

    @Test
    fun `Get inventory amount by index`() {
        // Given
        val index = 1
        val amount = 100
        items[index] = Item("", amount)
        // When
        val count = inventory[index].amount
        // Then
        assertEquals(amount, count)
    }

    @Test
    fun `Get all inventory items`() {
        // Given
        items[1] = Item("2", 2)
        items[3] = Item("4", 4)
        // When
        val items = inventory.items
        // Then
        assertArrayEquals(this.items, items)
    }

    @Test
    fun `Get count of all item amounts`() {
        // Given
        every { inventory.stackable("2") } returns true
        items[1] = Item("2", 2)
        items[2] = Item("3", 1)
        items[3] = Item("2", 4)
        items[4] = Item("2", -1)
        // When
        val amounts = inventory.count("2")
        // Then
        assertEquals(6, amounts)
    }

    @Test
    fun `Contains an amount of non-stackable items`() {
        // Given
        every { inventory.stackable(any()) } returns false
        items[0] = Item("not_stackable", 1)
        items[1] = Item("not_stackable", 1)
        items[2] = Item("not_stackable", 1)
        // When
        val contains = inventory.contains("not_stackable", 2)
        // Then
        assertTrue(contains)
    }
}
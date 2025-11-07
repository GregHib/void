package world.gregs.voidps.engine.inv

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.remove.DefaultItemAmountBounds
import world.gregs.voidps.engine.inv.remove.ItemAmountBounds
import world.gregs.voidps.engine.inv.remove.ShopItemAmountBounds
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.engine.inv.stack.ItemStackingRule
import world.gregs.voidps.engine.inv.stack.NeverStack

internal class InventoryTest {
    private lateinit var inventory: Inventory
    private lateinit var items: Array<Item>
    private lateinit var minimumAmounts: IntArray
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        items = Array(10) { Item("", 0) }
        minimumAmounts = IntArray(10)
        inventory = inventory()
    }

    private fun inventory(
        id: String = "123",
        items: Array<Item> = this.items,
        stackRule: ItemStackingRule = AlwaysStack,
        amountBounds: ItemAmountBounds = DefaultItemAmountBounds,
    ): Inventory = spyk(
        Inventory(
            data = items,
            id = id,
            stackRule = stackRule,
            amountBounds = amountBounds,
        ).apply {
            transaction.changes.bind(player)
        },
    )

    @Test
    fun `Stackable true if always stack mode`() {
        // Given
        val id = "1"
        inventory = inventory(
            items = emptyArray(),
            stackRule = AlwaysStack,
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
            stackRule = NeverStack,
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
            stackRule = ItemDependentStack(definitions),
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
            stackRule = ItemDependentStack(definitions),
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
            amountBounds = ShopItemAmountBounds,
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
    fun `Inventory is full`() {
        assertFalse(inventory.isFull())
        for (i in items.indices) {
            items[i] = Item("123", 1)
        }
        assertTrue(inventory.isFull())
    }

    @Test
    fun `Index of first item`() {
        items[4] = Item("123", 1)
        items[5] = Item("123", 1)
        assertEquals(4, inventory.indexOf("123"))
    }

    @Test
    fun `Index of of non-existent item`() {
        assertEquals(-1, inventory.indexOf(""))
        assertEquals(-1, inventory.indexOf("123"))
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
    fun `Count non existing item`() {
        // Given
        every { inventory.stackable("2") } returns true
        // Then
        assertEquals(0, inventory.count("2"))
        assertEquals(0, inventory.count("3"))
    }

    @Test
    fun `Count multiples of an amount`() {
        // Given
        every { inventory.stackable(any()) } returns true
        items[0] = Item("stackable", 16)
        // Then
        assertEquals(5, inventory.count("stackable", 3))
        assertEquals(3, inventory.count("stackable", 5))
    }

    @Test
    fun `Contains an amount of non-stackable items`() {
        // Given
        every { inventory.stackable(any()) } returns false
        items[0] = Item("not_stackable", 1)
        items[1] = Item("not_stackable", 1)
        items[2] = Item("not_stackable", 1)
        // Then
        assertTrue(inventory.contains("not_stackable", 2))
        assertTrue(inventory.contains("not_stackable", 3))
        assertFalse(inventory.contains("not_stackable", 4))
    }

    @Test
    fun `Doesn't contain non-existing items`() {
        // Given
        every { inventory.stackable(any()) } returns false
        every { inventory.stackable("stackable") } returns true
        // Then
        assertFalse(inventory.contains("stackable", 1))
        assertFalse(inventory.contains("non-stackable", 1))
    }

    @Test
    fun `Contains stackable items`() {
        // Given
        every { inventory.stackable(any()) } returns true
        items[0] = Item("stackable", 15)
        items[1] = Item("stackable", 1)
        // Then
        assertTrue(inventory.contains("stackable", 15))
        // Then
        assertFalse(inventory.contains("stackable", 16))
    }
}

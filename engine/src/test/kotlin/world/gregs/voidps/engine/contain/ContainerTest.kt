package world.gregs.voidps.engine.contain

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.contain.remove.DefaultItemRemovalChecker
import world.gregs.voidps.engine.contain.remove.ItemRemovalChecker
import world.gregs.voidps.engine.contain.remove.ShopItemRemovalChecker
import world.gregs.voidps.engine.contain.stack.AlwaysStack
import world.gregs.voidps.engine.contain.stack.DependentOnItem
import world.gregs.voidps.engine.contain.stack.ItemStackingRule
import world.gregs.voidps.engine.contain.stack.NeverStack
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events

internal class ContainerTest {
    private lateinit var container: Container
    private lateinit var items: Array<Item>
    private lateinit var minimumAmounts: IntArray
    private lateinit var events: Events

    @BeforeEach
    fun setup() {
        events = mockk(relaxed = true)
        items = Array(10) { Item("", 0, def = ItemDefinition.EMPTY) }
        minimumAmounts = IntArray(10)
        container = container()
    }

    private fun container(
        id: String = "123",
        items: Array<Item> = this.items,
        stackRule: ItemStackingRule = AlwaysStack,
        removalCheck: ItemRemovalChecker = DefaultItemRemovalChecker
    ): Container = spyk(
        Container(
            data = ContainerData(items),
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
        container = container(
            items = emptyArray(),
            stackRule = AlwaysStack
        )
        // When
        val stackable = container.stackable(id)
        // Then
        assertTrue(stackable)
    }

    @Test
    fun `Stackable false if never stack mode`() {
        // Given
        val id = "1"
        container = container(
            items = emptyArray(),
            stackRule = NeverStack
        )
        // When
        val stackable = container.stackable(id)
        // Then
        assertFalse(stackable)
    }

    @Test
    fun `Stackable true if normal stack mode and item stacks`() {
        // Given
        val id = "1"
        val definitions: ItemDefinitions = mockk(relaxed = true)
        container = container(
            items = emptyArray(),
            stackRule = DependentOnItem(definitions)
        )
        every { definitions.get(id) } returns ItemDefinition(stackable = 1)
        // When
        val stackable = container.stackable(id)
        // Then
        assertTrue(stackable)
    }

    @Test
    fun `Stackable false if normal stack mode and non-stackable item`() {
        // Given
        val id = "1"
        val definitions: ItemDefinitions = mockk(relaxed = true)
        container = container(
            items = emptyArray(),
            stackRule = DependentOnItem(definitions)
        )
        every { definitions.get(id) } returns ItemDefinition(stackable = 0)
        // When
        val stackable = container.stackable(id)
        // Then
        assertFalse(stackable)
    }

    @Test
    fun `Spaces counts number of empty items`() {
        // Given
        container = container(
            removalCheck = ShopItemRemovalChecker
        )
        container.transaction {
            repeat(8) {
                set(it, Item("item", def = ItemDefinition.EMPTY))
            }
        }
        // When
        val spaces = container.spaces
        // Then
        assertEquals(2, spaces)
    }

    @Test
    fun `Bounds checks over maximum`() {
        // Given
        val index = items.size + 1
        // When
        val within = container.inBounds(index)
        // Then
        assertFalse(within)
    }

    @Test
    fun `Bounds checks under minimum`() {
        // Given
        val index = -1
        // When
        val within = container.inBounds(index)
        // Then
        assertFalse(within)
    }

    @Test
    fun `Container is empty`() {
        assertTrue(container.isEmpty())
        items[4] = Item("123", 10, def = ItemDefinition.EMPTY)
        assertFalse(container.isEmpty())
    }

    @Test
    fun `Get container item by index`() {
        // Given
        val index = 1
        val id = "100"
        items[index] = Item(id, 0, def = ItemDefinition.EMPTY)
        // When
        val item = container[index].id
        // Then
        assertEquals(id, item)
    }

    @Test
    fun `Get container item out of index`() {
        // Given
        val index = -2
        // When
        val item = container.getOrNull(index)?.id
        // Then
        assertNull(item)
    }

    @Test
    fun `Get container amount out of index`() {
        // Given
        val index = -2
        // When
        val count = container.getOrNull(index)?.amount
        // Then
        assertNull(count)
    }

    @Test
    fun `Get container amount by index`() {
        // Given
        val index = 1
        val amount = 100
        items[index] = Item("", amount, def = ItemDefinition.EMPTY)
        // When
        val count = container[index].amount
        // Then
        assertEquals(amount, count)
    }

    @Test
    fun `Get all container items`() {
        // Given
        items[1] = Item("2", 2, def = ItemDefinition.EMPTY)
        items[3] = Item("4", 4, def = ItemDefinition.EMPTY)
        // When
        val items = container.items
        // Then
        assertArrayEquals(this.items, items)
    }

    @Test
    fun `Get count of all item amounts`() {
        // Given
        every { container.stackable("2") } returns true
        items[1] = Item("2", 2, def = ItemDefinition.EMPTY)
        items[2] = Item("3", 1, def = ItemDefinition.EMPTY)
        items[3] = Item("2", 4, def = ItemDefinition.EMPTY)
        items[4] = Item("2", -1, def = ItemDefinition.EMPTY)
        // When
        val amounts = container.count("2")
        // Then
        assertEquals(6, amounts)
    }

    @Test
    fun `Contains an amount of non-stackable items`() {
        // Given
        every { container.stackable(any()) } returns false
        items[0] = Item("not_stackable", 1, def = ItemDefinition.EMPTY)
        items[1] = Item("not_stackable", 1, def = ItemDefinition.EMPTY)
        items[2] = Item("not_stackable", 1, def = ItemDefinition.EMPTY)
        // When
        val contains = container.contains("not_stackable", 2)
        // Then
        assertTrue(contains)
    }
}
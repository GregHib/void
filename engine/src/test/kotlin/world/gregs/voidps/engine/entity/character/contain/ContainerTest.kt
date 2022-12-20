package world.gregs.voidps.engine.entity.character.contain

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.remove.DefaultItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.remove.ItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.remove.ShopItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.DependentOnItem
import world.gregs.voidps.engine.entity.character.contain.stack.ItemStackingRule
import world.gregs.voidps.engine.entity.character.contain.stack.NeverStack
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events

internal class ContainerTest {
    private lateinit var container: Container
    private lateinit var definitions: ItemDefinitions
    private lateinit var items: Array<Item>
    private lateinit var minimumAmounts: IntArray
    private lateinit var events: Events

    @BeforeEach
    fun setup() {
        definitions = mockk(relaxed = true)
        events = mockk(relaxed = true)
        every { definitions.size } returns 100
        every { definitions.contains(any()) } returns true
        every { definitions.get(any<Int>()) } returns ItemDefinition.EMPTY
        every { definitions.get(any<String>()) } returns ItemDefinition.EMPTY
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
            events = mutableSetOf(this@ContainerTest.events),
            removalCheck = removalCheck
        ).apply {
            this.definitions = this@ContainerTest.definitions
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
        every { definitions.get(id) } returns ItemDefinition(stackable = 0)
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
        every { definitions.get(id) } returns ItemDefinition(stackable = 1)
        // When
        val stackable = container.stackable(id)
        // Then
        assertFalse(stackable)
    }

    @Test
    fun `Stackable true if normal stack mode and item stacks`() {
        // Given
        val id = "1"
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
    fun `Stackable false if normal stack mode and item unstackable`() {
        // Given
        val id = "1"
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
    fun `Spaces counts number of amounts equal to min stack`() {
        // Given
        items[1] = Item("", -1, def = ItemDefinition.EMPTY)
        items[4] = Item("", -1, def = ItemDefinition.EMPTY)
        items[5] = Item("", -2, def = ItemDefinition.EMPTY)
        container = container(
            items = items,
            removalCheck = ShopItemRemovalChecker
        )
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
    fun `Valid checks index values match expected`() {
        // Given
        val index = 1
        val id = "2"
        val amount = 1
        items[index] = Item(id, amount, def = ItemDefinition.EMPTY)
        // When
        val valid = container.isValid(index, id, amount)
        // Then
        assertTrue(valid)
        verify { container.inBounds(index) }
    }

    @Test
    fun `Valid id equals exactly regardless of amount`() {
        // Given
        val index = 1
        val id = "2"
        val amount = -100
        items[index] = Item(id, amount, def = ItemDefinition.EMPTY)
        // When
        val valid = container.isValidId(index, id)
        // Then
        assertTrue(valid)
        verify { container.inBounds(index) }
    }

    @Test
    fun `Invalid id`() {
        // Given
        val index = 1
        val id = "2"
        val amount = -100
        items[index] = Item("3", amount, def = ItemDefinition.EMPTY)
        // When
        val valid = container.isValidId(index, id)
        // Then
        assertFalse(valid)
    }

    @Test
    fun `Valid amount equals exactly regardless of id`() {
        // Given
        val index = 1
        val id = "-100"
        val amount = 100
        items[index] = Item(id, amount, def = ItemDefinition.EMPTY)
        // When
        val valid = container.isValidAmount(index, amount)
        // Then
        assertTrue(valid)
        verify { container.inBounds(index) }
    }

    @Test
    fun `Invalid amount`() {
        // Given
        val index = 1
        val id = "-100"
        val amount = 100
        items[index] = Item(id, amount + 1, def = ItemDefinition.EMPTY)
        // When
        val valid = container.isValidAmount(index, amount)
        // Then
        assertFalse(valid)
    }

    @Test
    fun `Valid checks index id matches expected`() {
        // Given
        val index = 1
        val id = "2"
        val amount = 1
        items[index] = Item(id, amount, def = ItemDefinition.EMPTY)
        // When
        val valid = container.isValid(index, "3", amount)
        // Then
        assertFalse(valid)
        verify { container.inBounds(index) }
    }

    @Test
    fun `Valid checks index amount matches expected`() {
        // Given
        val index = 1
        val id = "2"
        val amount = 1
        items[index] = Item(id, amount, def = ItemDefinition.EMPTY)
        // When
        val valid = container.isValid(index, id, 2)
        // Then
        assertFalse(valid)
        verify { container.inBounds(index) }
    }

    @Test
    fun `Valid input checks id is valid`() {
        // When
        val valid = container.isValidInput("", 2)
        // Then
        assertFalse(valid)
    }

    @Test
    fun `Valid input checks id is real`() {
        // Given
        every { definitions.contains("not_real") } returns false
        // When
        val valid = container.isValidInput("not_real", 2)
        // Then
        assertFalse(valid)
    }

    @Test
    fun `Valid input checks values match expected`() {
        // When
        val valid = container.isValidInput("1", 1)
        // Then
        assertTrue(valid)
    }

    @Test
    fun `Valid input checks restrictions`() {
        // Given
        container.itemRule = object : ItemRestrictionRule {
            override fun restricted(id: String): Boolean {
                return true
            }
        }
        // When
        val valid = container.isValidInput("1", 1)
        // Then
        assertFalse(valid)
    }

    /*
        ADD INDEX
     */
    @Test
    fun `Adding zero amount at index fails`() {
        // Given
        val index = 1
        // When
        assertFalse(container.add(index, "1", 0))
        // Then
        assertEquals(ContainerResult.Invalid, container.result)
    }

    @Test
    fun `Adding at out of bounds index fails`() {
        // Given
        val index = items.size + 1
        // When
        assertFalse(container.add(index, "1", 1))
        // Then
        assertEquals(ContainerResult.Invalid, container.result)
    }

    @Test
    fun `Adding at index with different id fails`() {
        // Given
        val index = 0
        items[index] = Item("5", 0, def = ItemDefinition.EMPTY)
        // When
        assertFalse(container.add(index, "1", 1))
        // Then
        assertEquals(ContainerResult.WrongType, container.result)
    }

    @Test
    fun `Adding at free index with id isn't wrong type`() {
        // Given
        val index = 0
        items[index] = Item("", 0, def = ItemDefinition.EMPTY)
        // When
        assertTrue(container.add(index, "1", 1))
        // Then
        assertNotEquals(ContainerResult.WrongType, container.result)
    }

    @Test
    fun `Adding over integer max at index overflows`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, Int.MAX_VALUE, def = ItemDefinition.EMPTY)
        // When
        assertFalse(container.add(index, id, 1))
        // Then
        assertEquals(ContainerResult.Overflow, container.result)
    }

    @Test
    fun `Coerce over integer max at index success`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, Int.MAX_VALUE, def = ItemDefinition.EMPTY)
        // When
        assertTrue(container.add(index, id, 1, coerce = true))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.set(index, id, Int.MAX_VALUE)
        }
    }

    @Test
    fun `Adding more than one unstackable item at index adds normally`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, 1, def = ItemDefinition.EMPTY)
        every { container.stackable(id) } returns false
        every { container.add(id, any()) } returns true
        // When
        assertTrue(container.add(index, id, 3))
        // Then
        verify {
            container.add(id, 3)
        }
    }

    @Test
    fun `Adding one unstackable item at a free index is successful`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item("", 0, def = ItemDefinition.EMPTY)
        every { container.stackable(id) } returns false
        // When
        assertTrue(container.add(index, id, 1))
        // Then
        assertEquals(ContainerResult.Success, container.result)
    }

    @Test
    fun `Adding multiple stackable items at free index is successful`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item("", 0, def = ItemDefinition.EMPTY)
        every { container.stackable(id) } returns true
        // When
        assertTrue(container.add(index, id, 2))
        // Then
        assertEquals(ContainerResult.Success, container.result)
    }

    @Test
    fun `Adding stackable items to index with same items combines stacks`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, 1, def = ItemDefinition.EMPTY)
        every { container.stackable(id) } returns true
        // When
        assertTrue(container.add(index, id, 1))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify { container.set(index, id, 2) }
    }

    /*
        ADD ANYWHERE
     */
    @Test
    fun `Adding zero amount fails`() {
        // When
        assertFalse(container.add("1", 0))
        // Then
        assertEquals(ContainerResult.Invalid, container.result)
    }

    @Test
    fun `Adding existing stackable item over integer max overflows`() {
        // Given
        val id = "1"
        val index = 0
        items[index] = Item(id, Int.MAX_VALUE, def = ItemDefinition.EMPTY)
        every { container.stackable(id) } returns true
        // When
        assertFalse(container.add(id, 1))
        // Then
        assertEquals(ContainerResult.Overflow, container.result)
    }

    @Test
    fun `Adding existing stackable item combines stacks`() {
        // Given
        val id = "1"
        val index = 0
        items[index] = Item(id, 1, def = ItemDefinition.EMPTY)
        every { container.stackable(id) } returns true
        // When
        assertTrue(container.add(id, 1))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify { container.set(index, id, 2) }
    }

    @Test
    fun `Adding non-existent stackable item with no free spaces fails`() {
        // Given
        val id = "1"
        every { container.freeIndex() } returns -1
        every { container.stackable(id) } returns true
        // When
        assertFalse(container.add(id, 1))
        // Then
        assertEquals(ContainerResult.Full, container.result)
    }

    @Test
    fun `Adding non-existent stackable item adds to free space`() {
        // Given
        val id = "1"
        val index = 2
        val amount = 1
        every { container.freeIndex() } returns index
        every { container.stackable(id) } returns true
        // When
        assertTrue(container.add(id, amount))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify { container.set(index, id, amount) }
    }

    @Test
    fun `Adding unstackable item with not enough spaces fails`() {
        // Given
        val id = "1"
        val amount = 2
        every { container.spaces } returns amount - 1
        every { container.stackable(id) } returns false
        // When
        assertFalse(container.add(id, amount))
        // Then
        assertEquals(ContainerResult.Full, container.result)
    }

    @Test
    fun `Coerce unstackable item overflow to remaining spaces`() {
        // Given
        val id = "1"
        val amount = 2
        every { container.spaces } returns amount - 1
        every { container.stackable(id) } returns false
        // When
        assertTrue(container.add(id, amount, coerce = true))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.set(0, id, 1, false)
        }
    }

    @Test
    fun `Adding unstackable item with enough spaces is successful`() {
        // Given
        val id = "1"
        val amount = 2
        items[0] = Item("", 1, def = ItemDefinition.EMPTY)
        every { container.stackable(id) } returns false
        // When
        assertTrue(container.add(id, amount))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.set(1, id, 1, false)
            container.set(2, id, 1, false)
        }
    }

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

    private fun items(vararg items: Pair<String, Int>?) = container(
        items = items.map { Item(it?.first ?: "", it?.second ?: 0, def = ItemDefinition.EMPTY) }.toTypedArray()
    )

    @Test
    fun `Listeners notified of updates`() {
        // When
        container.set(2, "123", 2)
        // Then
        verify {
            events.emit(ContainerUpdate(
                container = "123",
                updates = listOf(ItemChanged("123", 2, Item("", 0, def = ItemDefinition.EMPTY), Item("123", 2, def = ItemDefinition.EMPTY), false))
            ))
        }
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
        val item = container.getItemId(index)
        // Then
        assertEquals(id, item)
    }

    @Test
    fun `Get container item out of index`() {
        // Given
        val index = -2
        // When
        val item = container.getItemId(index)
        // Then
        assertEquals("", item)
    }

    @Test
    fun `Get container amount by index`() {
        // Given
        val index = 1
        val amount = 100
        items[index] = Item("", amount, def = ItemDefinition.EMPTY)
        // When
        val count = container.getAmount(index)
        // Then
        assertEquals(amount, count)
    }

    @Test
    fun `Get container amount out of index`() {
        // Given
        val index = -2
        // When
        val count = container.getAmount(index)
        // Then
        assertEquals(0, count)
    }

    @Test
    fun `Get all container items`() {
        // Given
        items[1] = Item("2", 2, def = ItemDefinition.EMPTY)
        items[3] = Item("4", 4, def = ItemDefinition.EMPTY)
        // When
        val items = container.getItems()
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
        val amounts = container.getCount("2")
        // Then
        assertEquals(6L, amounts)
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

    companion object {

        private const val TYPE_1 = "type_1"
        private const val TYPE_2 = "type_2"
        private const val TYPE_3 = "type_3"
    }
}
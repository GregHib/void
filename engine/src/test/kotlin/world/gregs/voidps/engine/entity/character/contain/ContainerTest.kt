package world.gregs.voidps.engine.entity.character.contain

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.cache.definition.data.ItemDefinition
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
        items = Array(10) { Item("", 0) }
        minimumAmounts = IntArray(10)
        container = container()
    }

    private fun container(
        id: Int = 123,
        secondary: Boolean = false,
        name: String = "test",
        capacity: Int = 10,
        items: Array<Item> = this.items,
        stackMode: StackMode = StackMode.Always,
        minimumAmounts: IntArray = this.minimumAmounts
    ): Container = spyk(
        Container(
            items = items
        ).apply {
            this.id = id
            this.name = name
            this.capacity = capacity
            this.definitions = this@ContainerTest.definitions
            this.secondary = secondary
            this.stackMode = stackMode
            this.events.add(this@ContainerTest.events)
            this.minimumAmounts = minimumAmounts
        }
    )

    @Test
    fun `Stackable true if always stack mode`() {
        // Given
        val id = "1"
        container = container(
            items = emptyArray(),
            stackMode = StackMode.Always,
            capacity = 10
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
            stackMode = StackMode.Never,
            capacity = 10
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
            stackMode = StackMode.Normal,
            capacity = 10
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
            stackMode = StackMode.Normal,
            capacity = 10
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
        items[1] = Item("", -1)
        items[4] = Item("", -1)
        items[5] = Item("", -2)
        container = container(
            items = items,
            minimumAmounts = IntArray(10) { -1 }
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
        items[index] = Item(id, amount)
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
        items[index] = Item(id, amount)
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
        items[index] = Item("3", amount)
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
        items[index] = Item(id, amount)
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
        items[index] = Item(id, amount + 1)
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
        items[index] = Item(id, amount)
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
        items[index] = Item(id, amount)
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
        every { definitions.getId("not_real") } returns -1
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
    fun `Valid input checks predicate`() {
        // Given
        container.predicate = { _, _ -> false }
        // When
        val valid = container.isValidInput("1", 1)
        // Then
        assertFalse(valid)
    }

    /*
        INSERT INDEX
     */
    @Test
    fun `Insert single unstackable`() {
        // Given
        val index = 0
        val id = "1"
        every { container.stackable(any<String>()) } returns false
        // When
        assertTrue(container.insert(index, id, 1))
        // Then
        assertEquals(ContainerResult.Success, container.result)
    }

    @Test
    fun `Insert multiple unstackable`() {
        // Given
        val index = 0
        val id = "1"
        every { container.stackable(any<String>()) } returns false
        // When
        assertFalse(container.insert(index, id, 2))
        // Then
        assertEquals(ContainerResult.Unstackable, container.result)
    }

    @Test
    fun `Insert into full container`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, 1)
        for (i in 1 until items.size) {
            items[i] = Item("2", 1)
        }
        every { container.stackable(any<String>()) } returns true
        // When
        assertFalse(container.insert(index, id, 1))
        // Then
        assertEquals(ContainerResult.Full, container.result)
    }

    @Test
    fun `Insert shifts other items forward`() {
        // Given
        val index = 1
        val id = "1"
        repeat(items.size - 1) {
            items[it] = Item(it.toString(), 1)
        }
        every { container.stackable(any<String>()) } returns true
        // When
        assertTrue(container.insert(index, id, 2))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        assertArrayEquals(arrayOf(Item("0", 1), Item("1", 2), Item("1", 1), Item("2", 1), Item("3", 1), Item("4", 1), Item("5", 1), Item("6", 1), Item("7", 1), Item("8", 1)), items)
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
        items[index] = Item("5", 0)
        // When
        assertFalse(container.add(index, "1", 1))
        // Then
        assertEquals(ContainerResult.WrongType, container.result)
    }

    @Test
    fun `Adding at free index with id isn't wrong type`() {
        // Given
        val index = 0
        items[index] = Item("", 0)
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
        items[index] = Item(id, Int.MAX_VALUE)
        // When
        assertFalse(container.add(index, id, 1))
        // Then
        assertEquals(ContainerResult.Overflow, container.result)
    }

    @Test
    fun `Adding more than one unstackable item at index adds normally`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, 1)
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
        items[index] = Item("", 0)
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
        items[index] = Item("", 0)
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
        items[index] = Item(id, 1)
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
        items[index] = Item(id, Int.MAX_VALUE)
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
        items[index] = Item(id, 1)
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
    fun `Adding unstackable item with enough spaces is successful`() {
        // Given
        val id = "1"
        val amount = 2
        items[0] = Item("", 1)
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

    /*
        REMOVE AT INDEX
     */
    @Test
    fun `Removing zero amount at index fails`() {
        // Given
        val index = 1
        // When
        assertFalse(container.remove(index, "1", 0))
        // Then
        assertEquals(ContainerResult.Invalid, container.result)
    }

    @Test
    fun `Removing at out of bounds index fails`() {
        // Given
        val index = items.size + 1
        // When
        assertFalse(container.remove(index, "1", 1))
        // Then
        assertEquals(ContainerResult.Invalid, container.result)
    }

    @Test
    fun `Removing at index with different id fails`() {
        // Given
        val index = 0
        items[index] = Item("5", 0)
        // When
        assertFalse(container.remove(index, "1", 1))
        // Then
        assertEquals(ContainerResult.WrongType, container.result)
    }

    @Test
    fun `Removing more than one unstackable at index removes normally`() {
        // Given
        val index = 0
        val otherIndex = 1
        val id = "1"
        items[index] = Item(id, 1)
        items[otherIndex] = Item(id, 1)
        every { container.stackable(any<String>()) } returns false
        every { container.remove(id, any()) } returns true
        // When
        assertTrue(container.remove(index, id, 2))
        verify {
            container.remove(id, 2)
        }
    }

    @Test
    fun `Removing over integer max at index underflows`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, -10)// Should be impossible
        every { container.stackable(any<String>()) } returns true
        // When
        assertFalse(container.remove(index, id, Int.MAX_VALUE))
        // Then
        assertEquals(ContainerResult.Deficient, container.result)
    }

    @Test
    fun `Removing more than exists at index underflows`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, 1)
        every { container.stackable(any<String>()) } returns true
        // When
        assertFalse(container.remove(index, id, 2))
        // Then
        assertEquals(ContainerResult.Deficient, container.result)
    }

    @Test
    fun `Removing exact amount that exists at index clears slot`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, 2)
        every { container.stackable(any<String>()) } returns true
        // When
        assertTrue(container.remove(index, id, 2))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify { container.clear(index) }
    }

    @Test
    fun `Removing more than one unstackable item at index fails`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, 3)// More than one unstackable should be impossible
        every { container.stackable(id) } returns false
        // When
        assertFalse(container.remove(index, id, 1))
        // Then
        assertEquals(ContainerResult.Unstackable, container.result)
    }

    @Test
    fun `Removing one unstackable item at index is successful`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, 1)
        every { container.stackable(id) } returns false
        // When
        assertTrue(container.remove(index, id, 1))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify { container.clear(index) }
    }

    @Test
    fun `Removing multiple stackable items at index reduces stack`() {
        // Given
        val index = 0
        val id = "1"
        items[index] = Item(id, 4)
        every { container.stackable(id) } returns true
        // When
        assertTrue(container.remove(index, id, 2))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify { container.set(index, id, 2) }
    }

    /*
        REMOVE ANYWHERE
     */

    @Test
    fun `Removing zero amount fails`() {
        // When
        assertFalse(container.remove("1", 0))
        // Then
        assertEquals(ContainerResult.Invalid, container.result)
    }

    @Test
    fun `Removing non-existent item fails`() {
        // Given
        val id = "1"
        // When
        assertFalse(container.remove(id, 1))
        // Then
        assertEquals(ContainerResult.Deficient, container.result)
    }

    @Test
    fun `Removing existing stackable item over integer max underflows`() {
        // Given
        val id = "1"
        val index = 0
        items[index] = Item(id, 1)
        every { container.stackable(id) } returns true
        // When
        assertFalse(container.remove(id, Int.MAX_VALUE))
        // Then
        assertEquals(ContainerResult.Deficient, container.result)
    }

    @Test
    fun `Removing more stackable items than exists underflows`() {
        // Given
        val id = "1"
        val index = 0
        items[index] = Item(id, 1)
        every { container.stackable(id) } returns true
        // When
        assertFalse(container.remove(id, 2))
        // Then
        assertEquals(ContainerResult.Deficient, container.result)
    }

    @Test
    fun `Removing exact number of existing stackable items clears slot`() {
        // Given
        val id = "1"
        val index = 0
        items[index] = Item(id, 2)
        every { container.stackable(id) } returns true
        // When
        assertTrue(container.remove(id, 2))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify { container.clear(index) }
    }

    @Test
    fun `Removing a number existing stackable items reduces stack`() {
        // Given
        val id = "1"
        val index = 0
        items[index] = Item(id, 4)
        every { container.stackable(id) } returns true
        // When
        assertTrue(container.remove(id, 2))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify { container.set(index, id, 2) }
    }

    @Test
    fun `Removing more unstackable items than exists fails`() {
        // Given
        val id = "1"
        items[0] = Item(id, 0)
        items[2] = Item(id, 0)
        every { container.stackable(id) } returns false
        // When
        assertFalse(container.remove(id, 3))
        // Then
        assertEquals(ContainerResult.Deficient, container.result)
    }

    @Test
    fun `Removing exact number of unstackable items`() {
        // Given
        val id = "1"
        items[0] = Item(id, 0)
        items[2] = Item(id, 0)
        every { container.stackable(id) } returns false
        // When
        assertTrue(container.remove(id, 2))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.set(0, "", 0, false)
            container.set(2, "", 0, false)
        }
    }

    @Test
    fun `Removing less than existing unstackable items leaves remaining`() {
        // Given
        val id = "1"
        items[0] = Item(id, 1)
        items[2] = Item(id, 1)
        items[4] = Item(id, 1)
        every { container.stackable(id) } returns false
        // When
        assertTrue(container.remove(id, 2))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.clear(0, false)
            container.clear(2, false)
        }
        verify(exactly = 0) {
            container.clear(4)
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
        items = items.map { Item(it?.first ?: "", it?.second ?: 0) }.toTypedArray()
    )

    @Test
    fun `Move item from index in one container to index in another container`() {
        // Given
        val id = "1"
        val amount = 2
        val index = 3
        val otherIndex = 4
        val other = container(
            items = Array(10) { Item("", 0) }
        )
        every { container.remove(index, id, amount, moved = true) } returns true
        every { other.add(otherIndex, id, amount, moved = true) } returns true
        // When
        assertTrue(container.move(other, id, amount, index = index, targetIndex = otherIndex))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.remove(index, id, amount, moved = true)
            other.add(otherIndex, id, amount, moved = true)
        }
    }

    @Test
    fun `Move item from index in one container to insert at index in another container`() {
        // Given
        val id = "1"
        val amount = 2
        val index = 3
        val otherIndex = 4
        val other = container(
            items = Array(10) { Item("", 0) }
        )
        every { container.remove(index, id, amount, moved = true) } returns true
        every { other.insert(otherIndex, id, amount, moved = true) } returns true
        // When
        assertTrue(container.move(other, id, amount, index = index, targetIndex = otherIndex, insert = true))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.remove(index, id, amount, moved = true)
            other.insert(otherIndex, id, amount, moved = true)
        }
    }

    @Test
    fun `Move item from index in one container to anywhere in another container`() {
        // Given
        val id = "1"
        val amount = 2
        val index = 3
        val other = container(
            items = Array(10) { Item("", 0) }
        )
        every { container.remove(index, id, amount, moved = true) } returns true
        every { other.add(id, amount, moved = true) } returns true
        // When
        assertTrue(container.move(other, id, amount, index = index))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.remove(index, id, amount, moved = true)
            other.add(id, amount, moved = true)
        }
    }

    @Test
    fun `Move item from anywhere in one container to index in another container`() {
        // Given
        val id = "1"
        val amount = 2
        val otherIndex = 4
        val other = container(
            items = Array(10) { Item("", 0) }
        )
        every { container.remove(id, amount, moved = true) } returns true
        every { other.add(otherIndex, id, amount, moved = true) } returns true
        // When
        assertTrue(container.move(other, id, amount, targetIndex = otherIndex))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.remove(id, amount, moved = true)
            other.add(otherIndex, id, amount, moved = true)
        }
    }

    @Test
    fun `Move item from anywhere in one container to anywhere in another container`() {
        // Given
        val id = "1"
        val amount = 2
        val other = container(
            items = Array(10) { Item("", 0) }
        )
        every { container.remove(id, amount, moved = true) } returns true
        every { other.add(id, amount, moved = true) } returns true
        // When
        assertTrue(container.move(other, id, amount, index = null, targetIndex = null))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.remove(id, amount, moved = true)
            other.add(id, amount, moved = true)
        }
    }

    @Test
    fun `Move item from one container to a different item id in another container`() {
        // Given
        val id = "1"
        val amount = 2
        val newId = "3"
        val other = container(
            items = Array(10) { Item("", 0) }
        )
        every { definitions.get(newId) } returns ItemDefinition()
        every { container.remove(id, amount, moved = true) } returns true
        every { other.add(id, amount, moved = true) } returns true
        // When
        assertTrue(container.move(other, id, amount, index = null, targetIndex = null, targetId = newId))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        verify {
            container.remove(id, amount, moved = true)
            other.add(newId, amount, moved = true)
        }
    }

    @Test
    fun `Move item from one container fails deletion`() {
        // Given
        val id = "1"
        val amount = 2
        val other = container(
            items = Array(10) { Item("", 0) }
        )
        // When
        assertFalse(container.move(other, id, amount))
        // Then
        assertEquals(ContainerResult.Deficient, container.result)
    }

    @Test
    fun `Move item from one container fails addition reverts deletion`() {
        // Given
        val id = "1"
        val amount = 2
        val other = container(
            items = Array(1) { Item("", 0) }
        )
        every { container.stackable(any<String>()) } returns true
        every { other.stackable(any<String>()) } returns false
        items[0] = Item(id, amount)
        // When
        assertFalse(container.move(other, id, amount, index = null, targetIndex = null))
        // Then
        assertEquals(ContainerResult.Full, container.result)
        verify {
            container.remove(id, amount, moved = true)
            other.add(id, amount, moved = true)
            container.add(id, amount, moved = true)
        }
    }

    @Test
    fun `Swap two indices`() {
        // Given
        val firstIndex = 1
        val secondIndex = 3
        val first = Item("2", 3)
        val second = Item("4", 5)
        items[firstIndex] = first
        items[secondIndex] = second
        // When
        val result = container.swap(firstIndex, secondIndex)
        // Then
        assertTrue(result)
        assertEquals(second, items[firstIndex])
        assertEquals(first, items[secondIndex])
    }

    @Test
    fun `Swap index in one container with index in another`() {
        // Given
        val otherItems = Array(10) { Item("", 0) }
        val other = container(
            items = otherItems
        )
        val firstIndex = 1
        val secondIndex = 3
        val first = Item("2", 3)
        val second = Item("4", 5)
        items[firstIndex] = first
        otherItems[secondIndex] = second
        every { other.isValidOrEmpty(first, firstIndex) } returns true
        every { container.isValidOrEmpty(second, secondIndex) } returns true
        // When
        val result = container.swap(firstIndex, other, secondIndex)
        // Then
        assertTrue(result)
        assertEquals(second, items[firstIndex])
        assertEquals(first, otherItems[secondIndex])
    }

    @Test
    fun `Swap empty slot with item in another container`() {
        // Given
        val otherItems = Array(10) { Item("", 0) }
        val other = container(
            items = otherItems
        )
        val firstIndex = 1
        val secondIndex = 3
        val first = Item("2", 3)
        items[firstIndex] = first
        every { other.isValidOrEmpty(first, firstIndex) } returns true
        every { container.isValidOrEmpty(any(), secondIndex) } returns true
        // When
        val result = container.swap(firstIndex, other, secondIndex)
        // Then
        assertTrue(result)
        assertEquals(Item("", 0), items[firstIndex])
        assertEquals(first, otherItems[secondIndex])
    }

    @Test
    fun `Swap checks indices aren't out of bounds`() {
        // When
        val result = container.swap(0, items.size + 1)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Listeners notified of updates`() {
        // When
        container.set(2, "123", 2)
        // Then
        verify {
            events.emit(ContainerUpdate(
                containerId = 123,
                secondary = false,
                updates = listOf(ItemChanged("test", 2, Item("", 0), Item("123", 2), false))
            ))
        }
    }

    @Test
    fun `Listeners notified multiple changes`() {
        container.secondary = true
        // When
        container.swap(2, 3)
        // Then
        verify {
            events.emit(ContainerUpdate(
                containerId = 123,
                secondary = true,
                updates = listOf(
                    ItemChanged("test", 2, Item("", 0), Item("", 0), true),
                    ItemChanged("test", 3, Item("", 0), Item("", 0), true)
                )
            ))
        }
    }

    @Test
    fun `Container is empty`() {
        assertTrue(container.isEmpty())
        items[4] = Item("123", 10)
        assertFalse(container.isEmpty())
    }

    @Test
    fun `Get container item by index`() {
        // Given
        val index = 1
        val id = "100"
        items[index] = Item(id, 0)
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
        items[index] = Item("", amount)
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
        items[1] = Item("2", 2)
        items[3] = Item("4", 4)
        // When
        val items = container.getItems()
        // Then
        assertArrayEquals(this.items, items)
    }

    @Test
    fun `Get count of all item amounts`() {
        // Given
        every { container.stackable("2") } returns true
        items[1] = Item("2", 2)
        items[2] = Item("3", 1)
        items[3] = Item("2", 4)
        items[4] = Item("2", -1)
        // When
        val amounts = container.getCount("2")
        // Then
        assertEquals(6L, amounts)
    }

    @Test
    fun `Move all items from one container to another`() {
        // Given
        repeat(4) {
            items[it * 2] = Item((it + 1).toString(), it + 1)
        }
        val other = container(
            items = Array(10) { Item("", 0) }
        )
        every { container.stackable(any<String>()) } returns true
        every { other.stackable(any<String>()) } returns true
        // When
        assertTrue(container.moveAll(other))
        // Then
        assertEquals(ContainerResult.Success, container.result)
        assertArrayEquals(arrayOf(Item("4", 4), Item("3", 3), Item("2", 2), Item("1", 1), Item.EMPTY, Item.EMPTY, Item.EMPTY, Item.EMPTY, Item.EMPTY, Item.EMPTY), other.getItems())
    }

    @Test
    fun `Move all partial to other container`() {
        // Given
        repeat(4) {
            items[it * 2] = Item((it + 1).toString(), it + 1)
        }
        val other = container(
            items = Array(2) { Item("", 0) }
        )
        every { container.stackable(any<String>()) } returns true
        every { other.stackable(any<String>()) } returns true
        // When
        assertFalse(container.moveAll(other))
        // Then
        assertEquals(ContainerResult.Full, container.result)
        assertArrayEquals(arrayOf(Item("4", 4), Item("3", 3)), other.getItems())
    }

    @TestFactory
    fun `Move more than max amount overflows cleanly`() = mapOf(
        1000 to Int.MAX_VALUE,
        Int.MAX_VALUE to 1000,
        Int.MAX_VALUE - 1000 to 2000,
        2000 to Int.MAX_VALUE - 1000
    ).flatMap { (from, to) ->
        (0 until 2).map {
            val indexed = it == 0
            dynamicTest("Move $from to $to indexed: $indexed") {
                // Given
                val index = 0
                val id = "1"
                items[index] = Item(id, from)

                val other = container(
                    items = Array(1) { Item("", 0) }
                )
                other.set(index, id, to)
                every { container.stackable(any<String>()) } returns true
                every { other.stackable(any<String>()) } returns true
                // When
                if (indexed) {
                    assertFalse(container.move(other, id, from))
                } else {
                    assertFalse(container.move(other, id, from))
                }
                // Then
                assertEquals(ContainerResult.Full, container.result)
                assertArrayEquals(arrayOf(Item(id, Int.MAX_VALUE)), other.getItems())
                assertArrayEquals(arrayOf(Item(id, 1000), Item.EMPTY, Item.EMPTY, Item.EMPTY, Item.EMPTY, Item.EMPTY, Item.EMPTY, Item.EMPTY, Item.EMPTY, Item.EMPTY), items)
            }
        }
    }

    companion object {

        private const val TYPE_1 = "type_1"
        private const val TYPE_2 = "type_2"
        private const val TYPE_3 = "type_3"
    }
}
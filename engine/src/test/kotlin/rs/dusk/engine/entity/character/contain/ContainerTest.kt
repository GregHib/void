package rs.dusk.engine.entity.character.contain

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.cache.definition.decoder.ItemDecoder

internal class ContainerTest {
    private lateinit var container: Container
    private lateinit var decoder: ItemDecoder
    private lateinit var items: IntArray
    private lateinit var amounts: IntArray

    @BeforeEach
    fun setup() {
        decoder = mockk(relaxed = true)
        every { decoder.size } returns 100
        items = IntArray(10) { -1 }
        amounts = IntArray(10) { 0 }
        container = spyk(
            Container(
                decoder = decoder,
                items = items,
                amounts = amounts,
                minimumStack = 0
            )
        )
    }

    @Test
    fun `Stackable true if always stack mode`() {
        // Given
        val id = 1
        container = Container(
            decoder = decoder,
            stackMode = StackMode.Always,
            capacity = 10
        )
        every { decoder.get(id) } returns ItemDefinition(stackable = 0)
        // When
        val stackable = container.stackable(id)
        // Then
        assertTrue(stackable)
    }

    @Test
    fun `Stackable false if never stack mode`() {
        // Given
        val id = 1
        container = Container(
            decoder = decoder,
            stackMode = StackMode.Never,
            capacity = 10
        )
        every { decoder.get(id) } returns ItemDefinition(stackable = 1)
        // When
        val stackable = container.stackable(id)
        // Then
        assertFalse(stackable)
    }

    @Test
    fun `Stackable true if normal stack mode and item stacks`() {
        // Given
        val id = 1
        container = Container(
            decoder = decoder,
            stackMode = StackMode.Normal,
            capacity = 10
        )
        every { decoder.get(id) } returns ItemDefinition(stackable = 1)
        // When
        val stackable = container.stackable(id)
        // Then
        assertTrue(stackable)
    }

    @Test
    fun `Stackable false if normal stack mode and item unstackable`() {
        // Given
        val id = 1
        container = Container(
            decoder = decoder,
            stackMode = StackMode.Normal,
            capacity = 10
        )
        every { decoder.get(id) } returns ItemDefinition(stackable = 0)
        // When
        val stackable = container.stackable(id)
        // Then
        assertFalse(stackable)
    }

    @Test
    fun `Spaces counts number of amounts equal to min stack`() {
        // Given
        amounts[1] = -1
        amounts[4] = -1
        amounts[5] = -2
        container = Container(
            decoder = decoder,
            items = IntArray(10),
            amounts = amounts,
            minimumStack = -1
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
        val id = 2
        val amount = 1
        items[index] = id
        amounts[index] = amount
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
        val id = 2
        val amount = -100
        items[index] = id
        amounts[index] = amount
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
        val id = 2
        val amount = -100
        items[index] = id + 1
        amounts[index] = amount
        // When
        val valid = container.isValidId(index, id)
        // Then
        assertFalse(valid)
    }

    @Test
    fun `Valid amount equals exactly regardless of id`() {
        // Given
        val index = 1
        val id = -100
        val amount = 100
        items[index] = id
        amounts[index] = amount
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
        val id = -100
        val amount = 100
        items[index] = id
        amounts[index] = amount + 1
        // When
        val valid = container.isValidAmount(index, amount)
        // Then
        assertFalse(valid)
    }

    @Test
    fun `Valid checks index id matches expected`() {
        // Given
        val index = 1
        val id = 2
        val amount = 1
        items[index] = id
        amounts[index] = amount
        // When
        val valid = container.isValid(index, 3, amount)
        // Then
        assertFalse(valid)
        verify { container.inBounds(index) }
    }

    @Test
    fun `Valid checks index amount matches expected`() {
        // Given
        val index = 1
        val id = 2
        val amount = 1
        items[index] = id
        amounts[index] = amount
        // When
        val valid = container.isValid(index, id, 2)
        // Then
        assertFalse(valid)
        verify { container.inBounds(index) }
    }

    @Test
    fun `Valid input checks id is positive`() {
        // When
        val valid = container.isValidInput(-1, 2)
        // Then
        assertFalse(valid)
    }

    @Test
    fun `Valid input checks id is less than definitions`() {
        // Given
        every { decoder.size } returns 15000
        // When
        val valid = container.isValidInput(20000, 2)
        // Then
        assertFalse(valid)
    }

    @Test
    fun `Valid input checks amount is positive`() {
        // Given
        every { container.minimumStack } returns -1
        // When
        val valid = container.isValidInput(1, 0)
        // Then
        assertFalse(valid)
    }

    @Test
    fun `Valid input checks values match expected`() {
        // When
        val valid = container.isValidInput(1, 1)
        // Then
        assertTrue(valid)
    }

    /*
        ADD INDEX
     */
    @Test
    fun `Adding zero amount at index fails`() {
        // Given
        val index = 1
        // When
        val result = container.add(index, 1, 0)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Invalid, result)
    }

    @Test
    fun `Adding at out of bounds index fails`() {
        // Given
        val index = items.size + 1
        // When
        val result = container.add(index, 1, 1)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Invalid, result)
    }

    @Test
    fun `Adding at index with different id fails`() {
        // Given
        val index = 0
        items[index] = 5
        // When
        val result = container.add(index, 1, 1)
        // Then
        assertEquals(ContainerResult.Addition.Failure.WrongType, result)
    }

    @Test
    fun `Adding at free index with id isn't wrong type`() {
        // Given
        val index = 0
        items[index] = -1
        // When
        val result = container.add(index, 1, 1)
        // Then
        assertNotEquals(ContainerResult.Addition.Failure.WrongType, result)
    }

    @Test
    fun `Adding over integer max at index overflows`() {
        // Given
        val index = 0
        val id = 1
        items[index] = id
        amounts[index] = Int.MAX_VALUE
        // When
        val result = container.add(index, id, 1)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Overflow, result)
    }

    @Test
    fun `Adding more than one unstackable item at index fails`() {
        // Given
        val index = 0
        val id = 1
        items[index] = id
        amounts[index] = 1
        every { container.stackable(id) } returns false
        // When
        val result = container.add(index, id, 3)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Unstackable, result)
    }

    @Test
    fun `Adding one unstackable item at a free index is successful`() {
        // Given
        val index = 0
        val id = 1
        items[index] = -1
        amounts[index] = 0
        every { container.stackable(id) } returns false
        // When
        val result = container.add(index, id, 1)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
    }

    @Test
    fun `Adding multiple stackable items at free index is successful`() {
        // Given
        val index = 0
        val id = 1
        items[index] = -1
        amounts[index] = 0
        every { container.stackable(id) } returns true
        // When
        val result = container.add(index, id, 2)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
    }

    @Test
    fun `Adding stackable items to index with same items combines stacks`() {
        // Given
        val index = 0
        val id = 1
        items[index] = id
        amounts[index] = 1
        every { container.stackable(id) } returns true
        // When
        val result = container.add(index, id, 1)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
        verify { container.set(index, id, 2) }
    }

    /*
        ADD ANYWHERE
     */
    @Test
    fun `Adding zero amount fails`() {
        // When
        val result = container.add(1, 0)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Invalid, result)
    }

    @Test
    fun `Adding existing stackable item over integer max overflows`() {
        // Given
        val id = 1
        val index = 0
        items[index] = id
        amounts[index] = Int.MAX_VALUE
        every { container.stackable(id) } returns true
        // When
        val result = container.add(id, 1)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Overflow, result)
    }

    @Test
    fun `Adding existing stackable item combines stacks`() {
        // Given
        val id = 1
        val index = 0
        items[index] = id
        amounts[index] = 1
        every { container.stackable(id) } returns true
        // When
        val result = container.add(id, 1)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
        verify { container.set(index, id, 2) }
    }

    @Test
    fun `Adding non-existent stackable item with no free spaces fails`() {
        // Given
        val id = 1
        every { container.freeIndex() } returns -1
        every { container.stackable(id) } returns true
        // When
        val result = container.add(id, 1)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Full, result)
    }

    @Test
    fun `Adding non-existent stackable item adds to free space`() {
        // Given
        val id = 1
        val index = 2
        val amount = 1
        every { container.freeIndex() } returns index
        every { container.stackable(id) } returns true
        // When
        val result = container.add(id, amount)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
        verify { container.set(index, id, amount) }
    }

    @Test
    fun `Adding unstackable item with not enough spaces fails`() {
        // Given
        val id = 1
        val amount = 2
        every { container.spaces } returns amount - 1
        every { container.stackable(id) } returns false
        // When
        val result = container.add(id, amount)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Full, result)
    }

    @Test
    fun `Adding unstackable item with enough spaces is successful`() {
        // Given
        val id = 1
        val amount = 2
        amounts[0] = 1
        every { container.stackable(id) } returns false
        // When
        val result = container.add(id, amount)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
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
        val result = container.remove(index, 1, 0)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Invalid, result)
    }

    @Test
    fun `Removing at out of bounds index fails`() {
        // Given
        val index = items.size + 1
        // When
        val result = container.remove(index, 1, 1)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Invalid, result)
    }

    @Test
    fun `Removing at index with different id fails`() {
        // Given
        val index = 0
        items[index] = 5
        // When
        val result = container.remove(index, 1, 1)
        // Then
        assertEquals(ContainerResult.Removal.Failure.WrongType, result)
    }

    @Test
    fun `Removing over integer max at index underflows`() {
        // Given
        val index = 0
        val id = 1
        items[index] = id
        amounts[index] = 1
        // When
        val result = container.remove(index, id, Int.MAX_VALUE)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Underflow, result)
    }

    @Test
    fun `Removing more than exists at index underflows`() {
        // Given
        val index = 0
        val id = 1
        items[index] = id
        amounts[index] = 1
        // When
        val result = container.remove(index, id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Underflow, result)
    }

    @Test
    fun `Removing exact amount that exists at index clears slot`() {
        // Given
        val index = 0
        val id = 1
        items[index] = id
        amounts[index] = 2
        // When
        val result = container.remove(index, id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Removed, result)
        verify { container.clear(index) }
    }

    @Test
    fun `Removing more than one unstackable item at index fails`() {
        // Given
        val index = 0
        val id = 1
        items[index] = id
        amounts[index] = 3// Should be impossible
        every { container.stackable(id) } returns false
        // When
        val result = container.remove(index, id, 1)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Unstackable, result)
    }

    @Test
    fun `Removing one unstackable item at index is successful`() {
        // Given
        val index = 0
        val id = 1
        items[index] = id
        amounts[index] = 1
        every { container.stackable(id) } returns false
        // When
        val result = container.remove(index, id, 1)
        // Then
        assertEquals(ContainerResult.Removal.Removed, result)
        verify { container.clear(index) }
    }

    @Test
    fun `Removing multiple stackable items at index reduces stack`() {
        // Given
        val index = 0
        val id = 1
        items[index] = id
        amounts[index] = 4
        every { container.stackable(id) } returns true
        // When
        val result = container.remove(index, id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Removed, result)
        verify { container.set(index, id, 2) }
    }

    /*
        REMOVE ANYWHERE
     */

    @Test
    fun `Removing zero amount fails`() {
        // When
        val result = container.remove(1, 0)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Invalid, result)
    }

    @Test
    fun `Removing non-existent item fails`() {
        // Given
        val id = 1
        // When
        val result = container.remove(id, 1)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Deficient, result)
    }

    @Test
    fun `Removing existing stackable item over integer max underflows`() {
        // Given
        val id = 1
        val index = 0
        items[index] = id
        amounts[index] = 1
        every { container.stackable(id) } returns true
        // When
        val result = container.remove(id, Int.MAX_VALUE)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Underflow, result)
    }

    @Test
    fun `Removing more stackable items than exists underflows`() {
        // Given
        val id = 1
        val index = 0
        items[index] = id
        amounts[index] = 1
        every { container.stackable(id) } returns true
        // When
        val result = container.remove(id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Underflow, result)
    }

    @Test
    fun `Removing exact number of existing stackable items clears slot`() {
        // Given
        val id = 1
        val index = 0
        items[index] = id
        amounts[index] = 2
        every { container.stackable(id) } returns true
        // When
        val result = container.remove(id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Removed, result)
        verify { container.clear(index) }
    }

    @Test
    fun `Removing a number existing stackable items reduces stack`() {
        // Given
        val id = 1
        val index = 0
        items[index] = id
        amounts[index] = 4
        every { container.stackable(id) } returns true
        // When
        val result = container.remove(id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Removed, result)
        verify { container.set(index, id, 2) }
    }

    @Test
    fun `Removing more unstackable items than exists fails`() {
        // Given
        val id = 1
        items[0] = id
        items[2] = id
        every { container.stackable(id) } returns false
        // When
        val result = container.remove(id, 3)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Deficient, result)
    }

    @Test
    fun `Removing exact number of unstackable items`() {
        // Given
        val id = 1
        items[0] = id
        items[2] = id
        every { container.stackable(id) } returns false
        // When
        val result = container.remove(id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Removed, result)
        verify {
            container.set(0, -1, 0, false)
            container.set(2, -1, 0, false)
        }
    }

    @Test
    fun `Removing less than existing unstackable items leaves remaining`() {
        // Given
        val id = 1
        items[0] = id
        items[2] = id
        items[4] = id
        every { container.stackable(id) } returns false
        // When
        val result = container.remove(id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Removed, result)
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

    private fun items(vararg items: Pair<Int, Int>?) = Container(
        decoder = decoder,
        items = items.map { it?.first ?: -1 }.toIntArray(),
        amounts = items.map { it?.second ?: 0 }.toIntArray(),
        minimumStack = 0
    )

    @Test
    fun `Move item from index in one container to index in another container`() {
        // Given
        val id = 1
        val amount = 2
        val index = 3
        val otherIndex = 4
        val other = spyk(
            Container(
                decoder = decoder,
                capacity = 10
            )
        )
        every { container.remove(index, id, amount) } returns ContainerResult.Removal.Removed
        every { other.add(otherIndex, id, amount) } returns ContainerResult.Addition.Added
        // When
        val result = container.move(other, id, amount, index = index, targetIndex = otherIndex)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
        verify {
            container.remove(index, id, amount)
            other.add(otherIndex, id, amount)
        }
    }

    @Test
    fun `Move item from index in one container to anywhere in another container`() {
        // Given
        val id = 1
        val amount = 2
        val index = 3
        val other = spyk(
            Container(
                decoder = decoder,
                capacity = 10
            )
        )
        every { container.remove(index, id, amount) } returns ContainerResult.Removal.Removed
        every { other.add(id, amount) } returns ContainerResult.Addition.Added
        // When
        val result = container.move(other, id, amount, index = index)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
        verify {
            container.remove(index, id, amount)
            other.add(id, amount)
        }
    }

    @Test
    fun `Move item from anywhere in one container to index in another container`() {
        // Given
        val id = 1
        val amount = 2
        val otherIndex = 4
        val other = spyk(
            Container(
                decoder = decoder,
                capacity = 10
            )
        )
        every { container.remove(id, amount) } returns ContainerResult.Removal.Removed
        every { other.add(otherIndex, id, amount) } returns ContainerResult.Addition.Added
        // When
        val result = container.move(other, id, amount, targetIndex = otherIndex)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
        verify {
            container.remove(id, amount)
            other.add(otherIndex, id, amount)
        }
    }

    @Test
    fun `Move item from anywhere in one container to anywhere in another container`() {
        // Given
        val id = 1
        val amount = 2
        val other = spyk(
            Container(
                decoder = decoder,
                capacity = 10
            )
        )
        every { container.remove(id, amount) } returns ContainerResult.Removal.Removed
        every { other.add(id, amount) } returns ContainerResult.Addition.Added
        // When
        val result = container.move(other, id, amount, index = null, targetIndex = null)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
        verify {
            container.remove(id, amount)
            other.add(id, amount)
        }
    }

    @Test
    fun `Move item from one container fails deletion`() {
        // Given
        val id = 1
        val amount = 2
        val other = spyk(
            Container(
                decoder = decoder,
                capacity = 10
            )
        )
        every { container.remove(id, amount) } returns ContainerResult.Removal.Failure.Deficient
        // When
        val result = container.move(other, id, amount)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Deficient, result)
    }

    @Test
    fun `Move item from one container fails addition reverts deletion`() {
        // Given
        val id = 1
        val amount = 2
        val other = spyk(
            Container(
                decoder = decoder,
                capacity = 10
            )
        )
        every { container.remove(id, amount) } returns ContainerResult.Removal.Removed
        every { container.add(id, amount) } returns ContainerResult.Addition.Added
        every { other.add(id, amount) } returns ContainerResult.Addition.Failure.Full
        // When
        val result = container.move(other, id, amount, index = null, targetIndex = null)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Full, result)
        verify {
            container.remove(id, amount)
            other.add(id, amount)
            container.add(id, amount)
        }
    }

    @Test
    fun `Switch two indices`() {
        // Given
        val firstIndex = 1
        val secondIndex = 3
        items[firstIndex] = 2
        amounts[firstIndex] = 3
        items[secondIndex] = 4
        amounts[secondIndex] = 5
        // When
        val result = container.switch(firstIndex, secondIndex)
        // Then
        assertTrue(result)
        verify {
            container.set(firstIndex, 4, 5, false)
            container.set(secondIndex, 2, 3, false)
        }
    }

    @Test
    fun `Switch index in one container with index in another`() {
        // Given
        val otherItems = IntArray(10) { -1 }
        val otherAmounts = IntArray(10) { 0 }
        val other = spyk(
            Container(
                decoder = decoder,
                items = otherItems,
                amounts = otherAmounts
            )
        )
        val firstIndex = 1
        val secondIndex = 3
        items[firstIndex] = 2
        amounts[firstIndex] = 3
        otherItems[secondIndex] = 4
        otherAmounts[secondIndex] = 5
        // When
        val result = container.switch(firstIndex, other, secondIndex)
        // Then
        assertTrue(result)
        verify {
            container.set(firstIndex, 4, 5)
            other.set(secondIndex, 2, 3)
        }
    }

    @Test
    fun `Switch checks indices aren't out of bounds`() {
        // When
        val result = container.switch(0, items.size + 1)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Listeners notified of updates`() {
        // Given
        var captured: List<Triple<Int, Int, Int>>? = null
        container.listeners.add {
            captured = it.toList()
        }
        // When
        container.set(2, 123, 2)
        // Then
        assertEquals(listOf(Triple(2, 123, 2)), captured)
    }

    @Test
    fun `Listeners notified multiple changes`() {
        // Given
        var captured: List<Triple<Int, Int, Int>>? = null
        container.listeners.add {
            captured = it.toList()
        }
        // When
        container.switch(2, 3)
        // Then
        assertEquals(
            listOf(
                Triple(2, -1, 0),
                Triple(3, -1, 0)
            ), captured
        )
    }

    @Test
    fun `Get container item by index`() {
        // Given
        val index = 1
        val id = 100
        items[index] = id
        // When
        val item = container.getItem(index)
        // Then
        assertEquals(id, item)
    }

    @Test
    fun `Get container item out of index`() {
        // Given
        val index = -2
        // When
        val item = container.getItem(index)
        // Then
        assertEquals(-1, item)
    }

    @Test
    fun `Get container amount by index`() {
        // Given
        val index = 1
        val amount = 100
        amounts[index] = amount
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
        items[1] = 2
        items[3] = 4
        // When
        val items = container.getItems()
        // Then
        assertArrayEquals(this.items, items)
    }

    @Test
    fun `Get all container amounts`() {
        // Given
        amounts[1] = 2
        amounts[3] = 4
        // When
        val amounts = container.getAmounts()
        // Then
        assertArrayEquals(this.amounts, amounts)
    }

    companion object {

        private const val TYPE_1 = 115
        private const val TYPE_2 = 215
        private const val TYPE_3 = 315
    }
}
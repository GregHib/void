package rs.dusk.engine.model.entity.character.contain

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
    lateinit var container: Container
    lateinit var decoder: ItemDecoder

    @BeforeEach
    fun setup() {
        decoder = mockk(relaxed = true)
        every { decoder.size } returns 100
        container = spyk(
            Container(
                decoder = decoder,
                capacity = 10,
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
        every { decoder.getSafe(id) } returns ItemDefinition(stackable = 0)
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
        every { decoder.getSafe(id) } returns ItemDefinition(stackable = 1)
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
        every { decoder.getSafe(id) } returns ItemDefinition(stackable = 1)
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
        every { decoder.getSafe(id) } returns ItemDefinition(stackable = 0)
        // When
        val stackable = container.stackable(id)
        // Then
        assertFalse(stackable)
    }

    @Test
    fun `Spaces counts number of amounts equal to min stack`() {
        // Given
        container = Container(
            decoder = decoder,
            items = IntArray(10),
            amounts = IntArray(10) { 0 },
            minimumStack = -1
        )
        container.amounts[1] = -1
        container.amounts[4] = -1
        container.amounts[5] = -2
        // When
        val spaces = container.spaces
        // Then
        assertEquals(2, spaces)
    }

    @Test
    fun `Bounds checks over maximum`() {
        // Given
        val index = container.items.size + 1
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
        container.items[index] = id
        container.amounts[index] = amount
        // When
        val valid = container.isValid(index, id, amount)
        // Then
        assertTrue(valid)
        verify { container.inBounds(index) }
    }

    @Test
    fun `Valid checks index id matches expected`() {
        // Given
        val index = 1
        val id = 2
        val amount = 1
        container.items[index] = id
        container.amounts[index] = amount
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
        container.items[index] = id
        container.amounts[index] = amount
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
        val index = container.items.size + 1
        // When
        val result = container.add(index, 1, 1)
        // Then
        assertEquals(ContainerResult.Addition.Failure.Invalid, result)
    }

    @Test
    fun `Adding at index with different id fails`() {
        // Given
        val index = 0
        container.items[index] = 5
        // When
        val result = container.add(index, 1, 1)
        // Then
        assertEquals(ContainerResult.Addition.Failure.WrongType, result)
    }

    @Test
    fun `Adding at free index with id isn't wrong type`() {
        // Given
        val index = 0
        container.items[index] = -1
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
        container.items[index] = id
        container.amounts[index] = Int.MAX_VALUE
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
        container.items[index] = id
        container.amounts[index] = 1
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
        container.items[index] = -1
        container.amounts[index] = 0
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
        container.items[index] = -1
        container.amounts[index] = 0
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
        container.items[index] = id
        container.amounts[index] = 1
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
        container.items[index] = id
        container.amounts[index] = Int.MAX_VALUE
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
        container.items[index] = id
        container.amounts[index] = 1
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
        container.amounts[0] = 1
        every { container.stackable(id) } returns false
        // When
        val result = container.add(id, amount)
        // Then
        assertEquals(ContainerResult.Addition.Added, result)
        verify {
            container.set(1, id, 1)
            container.set(2, id, 1)
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
        val index = container.items.size + 1
        // When
        val result = container.remove(index, 1, 1)
        // Then
        assertEquals(ContainerResult.Removal.Failure.Invalid, result)
    }

    @Test
    fun `Removing at index with different id fails`() {
        // Given
        val index = 0
        container.items[index] = 5
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
        container.items[index] = id
        container.amounts[index] = 1
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
        container.items[index] = id
        container.amounts[index] = 1
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
        container.items[index] = id
        container.amounts[index] = 2
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
        container.items[index] = id
        container.amounts[index] = 3// Should be impossible
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
        container.items[index] = id
        container.amounts[index] = 1
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
        container.items[index] = id
        container.amounts[index] = 4
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
        container.items[index] = id
        container.amounts[index] = 1
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
        container.items[index] = id
        container.amounts[index] = 1
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
        container.items[index] = id
        container.amounts[index] = 2
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
        container.items[index] = id
        container.amounts[index] = 4
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
        container.items[0] = id
        container.items[2] = id
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
        container.items[0] = id
        container.items[2] = id
        every { container.stackable(id) } returns false
        // When
        val result = container.remove(id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Removed, result)
        verify {
            container.clear(0)
            container.clear(2)
        }
    }

    @Test
    fun `Removing less than existing unstackable items leaves remaining`() {
        // Given
        val id = 1
        container.items[0] = id
        container.items[2] = id
        container.items[4] = id
        every { container.stackable(id) } returns false
        // When
        val result = container.remove(id, 2)
        // Then
        assertEquals(ContainerResult.Removal.Removed, result)
        verify {
            container.clear(0)
            container.clear(2)
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
        container.items[firstIndex] = 2
        container.amounts[firstIndex] = 3
        container.items[secondIndex] = 4
        container.amounts[secondIndex] = 5
        // When
        val result = container.switch(firstIndex, secondIndex)
        // Then
        assertTrue(result)
        verify {
            container.set(firstIndex, 4, 5)
            container.set(secondIndex, 2, 3)
        }
    }

    @Test
    fun `Switch index in one container with index in another`() {
        // Given
        val other = spyk(
            Container(
                decoder = decoder,
                capacity = 10
            )
        )
        val firstIndex = 1
        val secondIndex = 3
        container.items[firstIndex] = 2
        container.amounts[firstIndex] = 3
        other.items[secondIndex] = 4
        other.amounts[secondIndex] = 5
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
        val result = container.switch(0, container.items.size + 1)
        // Then
        assertFalse(result)
    }

    companion object {

        private const val TYPE_1 = 115
        private const val TYPE_2 = 215
        private const val TYPE_3 = 315
    }
}
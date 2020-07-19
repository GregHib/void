package rs.dusk.engine.task

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


internal class TaskIteratorTest {

    @Test
    fun `Has next in list`() {
        val task1: Task = mockk()
        val list = mutableListOf(task1)
        val iterator = TaskIterator(list)
        val result = iterator.hasNext()
        assertTrue(result)
    }

    @Test
    fun `Doesn't have next in list`() {
        val list = mutableListOf<Task>()
        val iterator = TaskIterator(list)
        val result = iterator.hasNext()
        assertFalse(result)
    }

    @Test
    fun `Next value`() {
        val task1: Task = mockk()
        val list = mutableListOf(task1)
        val iterator = TaskIterator(list)
        val result = iterator.next()
        assertEquals(task1, result)
    }

    @Test
    fun `Next throws out of bounds when empty`() {
        val list = mutableListOf<Task>()
        val iterator = TaskIterator(list)
        assertThrows<IndexOutOfBoundsException> {
            iterator.next()
        }
    }

    @Test
    fun `Remove throws oob when empty`() {
        val list = mutableListOf<Task>()
        val iterator = TaskIterator(list)
        assertThrows<IndexOutOfBoundsException> {
            iterator.remove()
        }
    }

    @Test
    fun `Remove decreases index`() {
        val task1: Task = mockk()
        val list = mutableListOf(task1)
        val iterator = TaskIterator(list)
        iterator.remove()
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `Remove current`() {
        val task1: Task = mockk()
        val task2: Task = mockk()
        val list = mutableListOf(task1, task2)
        val iterator = TaskIterator(list)
        iterator.next()
        iterator.remove()
        val result = iterator.next()
        assertEquals(task2, result)
    }

    @Test
    fun `Remove first`() {
        val task1: Task = mockk()
        val task2: Task = mockk()
        val list = mutableListOf(task1, task2)
        val iterator = TaskIterator(list)
        iterator.remove()
        assertEquals(mutableListOf(task2), list)
    }

    @Test
    fun `Remove last`() {
        val task1: Task = mockk()
        val task2: Task = mockk()
        val list = mutableListOf(task1, task2)
        val iterator = TaskIterator(list)
        iterator.next()
        iterator.next()
        iterator.remove()
        assertEquals(mutableListOf(task1), list)
    }

    @Test
    fun `List can be appended mid iteration`() {
        val task1: Task = mockk()
        val task2: Task = mockk()
        val list = mutableListOf(task1)
        val iterator = TaskIterator(list)
        assertEquals(task1, iterator.next())
        iterator.remove()
        list.add(task2)
        assertTrue(iterator.hasNext())
        assertEquals(task2, iterator.next())
    }
}
package world.gregs.voidps.engine.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.entity.character.IndexAllocator

internal class IndexAllocatorTest {

    lateinit var allocator: IndexAllocator

    @BeforeEach
    fun setup() {
        allocator = IndexAllocator(10)
    }

    @Test
    fun `Obtain empty allocator`() {
        // When
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(1, value)
    }

    @Test
    fun `Obtain full allocator`() {
        // Given
        allocator.cap = 10
        // When
        val value = allocator.obtain()
        // Then
        assertNull(value)
    }

    @Test
    fun `Obtain partial allocator`() {
        // Given
        allocator.cap = 5
        // When
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(5, value)
    }

    @Test
    fun `Obtain reused allocation`() {
        // Given
        allocator.free.add(5)
        // When
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(5, value)
    }

    @Test
    fun `Releasing allocation`() {
        // Given
        allocator.cap = 5
        allocator.release(4)
        // When
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(4, value)
    }

    @Test
    fun `Unable to release indices out of bounds`() {
        // Given
        allocator.cap = 5
        // Then
        assertThrows<IllegalArgumentException> {
            allocator.release(6)
        }
    }

    @Test
    fun `Clear allocator`() {
        // Given
        allocator.cap = 5
        allocator.release(3)
        // When
        allocator.clear()
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(1, value)
    }
}
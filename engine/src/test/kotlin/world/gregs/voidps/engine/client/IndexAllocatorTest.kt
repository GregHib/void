package world.gregs.voidps.engine.client

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.entity.character.IndexAllocator

internal class IndexAllocatorTest {

    private lateinit var allocator: IndexAllocator

    @BeforeEach
    fun setup() {
        allocator = IndexAllocator(10)
    }

    @Test
    fun `Obtain empty allocator`() {
        // When
        val first = allocator.obtain()
        val second = allocator.obtain()
        // Then
        assertNotNull(first)
        assertEquals(1, first)
        assertNotNull(second)
        assertEquals(2, second)
    }

    @Test
    fun `Release allocator`() {
        // Given
        allocator = IndexAllocator(5)
        repeat(4) {
            allocator.obtain()
        }
        // When
        allocator.release(3)
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(5, value)
    }

    @Test
    fun `Unable to release indices out of bounds`() {
        // Given
        allocator = IndexAllocator(5)
        // Then
        assertThrows<IllegalArgumentException> {
            allocator.release(6)
        }
    }

    @Test
    fun `Clear allocator`() {
        // Given
        allocator = IndexAllocator(5)
        repeat(3) {
            allocator.obtain()
        }
        allocator.release(3)
        allocator.clear()
        // When
        val value = allocator.obtain()
        // Then
        assertNotNull(value)
        assertEquals(1, value)
    }
}